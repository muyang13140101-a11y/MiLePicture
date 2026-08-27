package com.milepicture.app.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // 默认通过 USB 的 ADB reverse 隧道直连电脑后端（需先执行 adb reverse tcp:3000 tcp:3000）
    // 备选方案：改为电脑局域网 WiFi IP，如 http://192.168.1.5:3000/（需关闭 Windows 防火墙或添加入站规则）
    var BASE_URL = "http://127.0.0.1:3000/"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private var retrofit: Retrofit? = null

    fun getService(): MiLePictureApiService {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(MiLePictureApiService::class.java)
    }

    fun updateBaseUrl(newUrl: String) {
        BASE_URL = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        retrofit = null
    }
}
