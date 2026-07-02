package com.prateek.khabrify.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Source(
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class ApiArticle(
    @Json(name = "title") val title: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "content") val content: String?,
    @Json(name = "url") val url: String,
    @Json(name = "image") val image: String?,
    @Json(name = "publishedAt") val publishedAt: String?,
    @Json(name = "source") val source: Source?
)

@JsonClass(generateAdapter = true)
data class GNewsResponse(
    @Json(name = "totalArticles") val totalArticles: Int,
    @Json(name = "articles") val articles: List<ApiArticle>
)
