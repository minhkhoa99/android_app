package com.example.musicfilemanager.api

import com.example.musicfilemanager.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit API Client
 * Singleton object to manage API instances
 */
object ApiClient {

    /**
     * Base URL from AppConfig
     * Có thể thay đổi trong AppConfig.kt
     */
    private val BASE_URL = AppConfig.API_BASE_URL

    /**
     * OkHttp client with logging interceptor
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (AppConfig.DEBUG_MODE) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(AppConfig.API_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(AppConfig.API_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.API_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Genre API Service instance
     */
    val genreService: GenreApiService by lazy {
        retrofit.create(GenreApiService::class.java)
    }

    /**
     * Music API Service instance (port 3005)
     */
    val musicService: MusicApiService by lazy {
        val musicRetrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3005/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        musicRetrofit.create(MusicApiService::class.java)
    }

    /**
     * Update base URL if needed (optional)
     */
    fun updateBaseUrl(newBaseUrl: String): GenreApiService {
        val newRetrofit = Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return newRetrofit.create(GenreApiService::class.java)
    }
}

