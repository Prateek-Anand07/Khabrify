package com.prateek.khabrify.data

import com.prateek.khabrify.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface GNewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String = "general",
        @Query("lang") language: String = "en",
        @Query("from") fromDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("country") country: String? = null,
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): GNewsResponse

    @GET("search")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("lang") language: String = "en",
        @Query("from") fromDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("country") country: String? = null,
        @Query("apikey") apiKey: String = BuildConfig.API_KEY
    ): GNewsResponse
}