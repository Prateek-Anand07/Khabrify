package com.prateek.khabrify.data.mapper

import com.prateek.khabrify.data.ApiArticle
import com.prateek.khabrify.data.Article

fun ApiArticle.toArticle(category: String = "general"): Article {
    return Article(
        url = url,
        title = title,
        description = description,
        content = content,
        image = image,
        publishedAt = publishedAt,
        sourceName = source?.name,
        category = category
    )
}