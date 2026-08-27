package com.milepicture.app.utils

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageDownloadHelper {

    /**
     * 高清图片下载保存到相册
     */
    suspend fun downloadImageToGallery(
        context: Context,
        imageUrl: String,
        title: String,
        source: String,
        onProgress: (Boolean) -> Unit,
        onResult: (Boolean, String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            onProgress(true)
            try {
                val cleanTitle = title.replace(Regex("[^a-zA-Z0-9_\\u4e00-\\u9fa5-]"), "_").take(30)
                val fileName = "MiLePicture_${source}_${cleanTitle}_${System.currentTimeMillis()}.jpg"

                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                connection.doInput = true
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        onProgress(false)
                        onResult(false, "下载失败: HTTP ${connection.responseCode}")
                    }
                    return@withContext
                }

                val inputStream: InputStream = connection.inputStream

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ 使用 MediaStore API 无需申请危险权限
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MiLePicture")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }

                    val resolver = context.contentResolver
                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                    if (imageUri != null) {
                        resolver.openOutputStream(imageUri)?.use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(imageUri, contentValues, null, null)

                        withContext(Dispatchers.Main) {
                            onProgress(false)
                            onResult(true, "✨ 图片已成功保存至系统相册 (Pictures/MiLePicture)")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onProgress(false)
                            onResult(false, "创建相册文件失败")
                        }
                    }
                } else {
                    // Android 9 及以下保存到公共 Pictures 目录
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val appDir = File(picturesDir, "MiLePicture")
                    if (!appDir.exists()) appDir.mkdirs()

                    val targetFile = File(appDir, fileName)
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    withContext(Dispatchers.Main) {
                        onProgress(false)
                        onResult(true, "✨ 图片已成功保存至相册: ${targetFile.name}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onProgress(false)
                    onResult(false, "下载出错: ${e.localizedMessage ?: "网络超时"}")
                }
            }
        }
    }
}
