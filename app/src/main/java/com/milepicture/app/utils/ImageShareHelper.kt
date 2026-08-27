package com.milepicture.app.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * 全版本安卓高兼容图片分享工具 (Android 7.0 - Android 15+)
 * 采用 FileProvider + 安全临时沙盒，支持直接将高清图片分享给微信、QQ、系统分享面板及各社交 App
 */
object ImageShareHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun shareImage(
        context: Context,
        imageUrl: String,
        title: String,
        landingUrl: String,
        onProgress: (Boolean) -> Unit,
        onResult: (Boolean, String?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            onProgress(true)
            try {
                // 1. 创建缓存分享目录
                val shareDir = File(context.cacheDir, "share_images")
                if (!shareDir.exists()) shareDir.mkdirs()

                val cleanTitle = title.replace(Regex("[^a-zA-Z0-9_\\u4e00-\\u9fa5-]"), "_").take(20).ifBlank { "artwork" }
                val tempFile = File(shareDir, "MiLeShare_${cleanTitle}_${System.currentTimeMillis()}.jpg")

                // 2. 抓取图片流写入临时文件
                val req = Request.Builder().url(imageUrl).build()
                val res = client.newCall(req).execute()
                if (!res.isSuccessful || res.body == null) {
                    withContext(Dispatchers.Main) {
                        onProgress(false)
                        onResult(false, "图片下载失败: HTTP ${res.code}")
                    }
                    return@withContext
                }

                FileOutputStream(tempFile).use { output ->
                    res.body!!.byteStream().copyTo(output)
                    output.flush()
                }

                // 3. 通过 FileProvider 获得 content:// 安全 URI (适配 Android 7.0+ 至 Android 15+)
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )

                // 4. 构建全系统标准图片分享 Intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, "✨ 分享自 MiLePicture 高清图库：$title\n$landingUrl")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    onProgress(false)
                    val chooser = Intent.createChooser(shareIntent, "分享图片到")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                    onResult(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onProgress(false)
                    onResult(false, "分享失败: ${e.localizedMessage ?: "未知错误"}")
                }
            }
        }
    }
}
