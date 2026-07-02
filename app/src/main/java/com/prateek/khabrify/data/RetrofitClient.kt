package com.prateek.khabrify.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://gnews.io/api/v4/"

    // Set up the Logger (Only prints data, doesn't change it)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }
    // Attach the logger to an OkHttp web client
    private val client = OkHttpClient.Builder().apply {
        this.addInterceptor(loggingInterceptor)
    }.build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())  // 🔥 MUST HAVE
        .build()

    // Build Retrofit using Moshi to translate the JSON
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Exposing API Service interface so the Repository can use it
    val apiService: GNewsApiService by lazy {
        retrofit.create(GNewsApiService::class.java)
    }
}