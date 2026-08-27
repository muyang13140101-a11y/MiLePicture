package com.milepicture.app.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * 商业级壁纸一键设置工具 (支持：设为桌面壁纸 / 设为锁屏壁纸 / 同时设置)
 */
object WallpaperHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    enum class TargetScreen {
        HOME, LOCK, BOTH
    }

    suspend fun setWallpaper(
        context: Context,
        imageUrl: String,
        target: TargetScreen,
        onProgress: (Boolean) -> Unit,
        onResult: (Boolean, String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            onProgress(true)
            try {
                val req = Request.Builder().url(imageUrl).build()
                val res = client.newCall(req).execute()
                if (!res.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onProgress(false)
                        onResult(false, "壁纸下载失败: HTTP ${res.code}")
                    }
                    return@withContext
                }

                val inputStream: InputStream = res.body?.byteStream() ?: throw Exception("图片流为空")
                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream) ?: throw Exception("图片解码失败")

                val wallpaperManager = WallpaperManager.getInstance(context)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    when (target) {
                        TargetScreen.HOME -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        TargetScreen.LOCK -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        TargetScreen.BOTH -> {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        }
                    }
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }

                withContext(Dispatchers.Main) {
                    onProgress(false)
                    onResult(true, "🎉 设为壁纸成功！")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onProgress(false)
                    onResult(false, "设置壁纸失败: ${e.localizedMessage ?: "未知异常"}")
                }
            }
        }
    }
}
