package com.milepicture.app

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * 商业级 Application 基础配置
 * 初始化高性能 Coil 缓存管道 (30% 内存缓存 + 500MB 磁盘缓存 + GIF/贴纸动图解码器 + 智能极速 DNS 调度)
 * 集成 16 KB 内存分页对齐 原生 64 位 C++ 引擎 (libmile_native.so)
 */
class MiLeApplication : Application(), ImageLoaderFactory {

    companion object {
        init {
            try {
                // 加载官方 16 KB 内存分页对齐 64 位原生 C++ 引擎 (libmile_native.so)
                System.loadLibrary("mile_native")
            } catch (_: UnsatisfiedLinkError) {
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun newImageLoader(): ImageLoader {
        // 商业级智能 DNS 调度 (优先 IPv4，大幅加速国内对 Unsplash / Pexels / GIPHY 的解析与连接握手)
        val fastDns = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return try {
                    val addresses = Dns.SYSTEM.lookup(hostname)
                    // 优先选择 IPv4 地址，规避国内部分网络 IPv6 绕路高延迟
                    val ipv4List = addresses.filterIsInstance<Inet4Address>()
                    if (ipv4List.isNotEmpty()) ipv4List else addresses
                } catch (e: Exception) {
                    Dns.SYSTEM.lookup(hostname)
                }
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .dns(fastDns)
            .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory(enforceMinimumFrameDelay = true))
                }
                add(GifDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache"))
                    .maxSizeBytes(500L * 1024 * 1024) // 500 MB 高清缓存
                    .build()
            }
            .crossfade(180)
            .respectCacheHeaders(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
