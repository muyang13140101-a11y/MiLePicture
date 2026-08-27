package com.milepicture.app.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // 永久云端无服务器后端（托管于 Cloudflare 全球边缘网络，7x24 小时全球高速可用，完全脱离电脑）
    var BASE_URL = "https://milepicture-api.muyang13140101.workers.dev/"

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
