package com.example.bitirmeproje.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private const val BASE_URL = "https://translation.googleapis.com/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val newUrl = originalUrl.newBuilder().addQueryParameter("key", "AIzaSyBQyoONU_OS9NCAzxAT5xDCu8OkWEalRTk").build()
        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        chain.proceed(newRequest)
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }
}
