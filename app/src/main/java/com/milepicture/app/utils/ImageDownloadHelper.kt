package com.milepicture.app.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * 商业级 RAW / 原生无损超清图片下载引擎
 * 采用 OkHttp 流式无重编码写入 MediaStore，100% 保持原始相片分辨率、EXIF 与原画细节，绝不二次有损压缩。
 */
object ImageDownloadHelper {

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

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
                val cleanTitle = title.replace(Regex("[^a-zA-Z0-9_\\u4e00-\\u9fa5-]"), "_").take(35).ifBlank { "artwork" }
                
                val request = Request.Builder()
                    .url(imageUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) MiLePicture/1.0")
                    .header("Accept", "image/*,*/*;q=0.8")
                    .build()

                val response = downloadClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onProgress(false)
                        onResult(false, "下载失败: HTTP ${response.code}")
                    }
                    return@withContext
                }

                val body = response.body
                if (body == null) {
                    withContext(Dispatchers.Main) {
                        onProgress(false)
                        onResult(false, "下载失败: 响应体为空")
                    }
                    return@withContext
                }

                // 智能检测原始图片真实 MIME 类型与后缀 (PNG / JPG / WEBP / TIFF)
                val contentType = response.header("Content-Type") ?: "image/jpeg"
                val extension = when {
                    contentType.contains("png", true) || imageUrl.contains(".png", true) -> "png"
                    contentType.contains("webp", true) || imageUrl.contains(".webp", true) -> "webp"
                    contentType.contains("gif", true) || imageUrl.contains(".gif", true) -> "gif"
                    contentType.contains("tiff", true) || imageUrl.contains(".tif", true) -> "tiff"
                    else -> "jpg"
                }

                val mimeType = when (extension) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    "gif" -> "image/gif"
                    "tiff" -> "image/tiff"
                    else -> "image/jpeg"
                }

                val fileName = "MiLePicture_${source}_${cleanTitle}_${System.currentTimeMillis()}.$extension"
                val inputStream: InputStream = body.byteStream()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MiLePicture")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }

                    val resolver = context.contentResolver
                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                    if (imageUri != null) {
                        resolver.openOutputStream(imageUri)?.use { outputStream ->
                            inputStream.copyTo(outputStream)
                            outputStream.flush()
                        }
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(imageUri, contentValues, null, null)

                        withContext(Dispatchers.Main) {
                            onProgress(false)
                            onResult(true, "✨ 已无损保存至系统相册 (Pictures/MiLePicture)")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onProgress(false)
                            onResult(false, "创建相册文件失败")
                        }
                    }
                } else {
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val appDir = File(picturesDir, "MiLePicture")
                    if (!appDir.exists()) appDir.mkdirs()

                    val targetFile = File(appDir, fileName)
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                        outputStream.flush()
                    }

                    withContext(Dispatchers.Main) {
                        onProgress(false)
                        onResult(true, "✨ 已无损保存至相册: ${targetFile.name}")
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
