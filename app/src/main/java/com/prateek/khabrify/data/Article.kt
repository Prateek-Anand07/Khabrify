package com.prateek.khabrify.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
@Entity(tableName = "saved_articles")
data class Article(
    @PrimaryKey
    val url: String = "",
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
    val image: String? = null,
    val publishedAt: String? = null,
    val sourceName: String? = null,
    val category: String = ""
)

@Keep
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val articlesRead: Int = 0,
    val profilePicUrl: String = "",
    val avatarColor: String = "0D1B2A",
    val country: String = "in",
    val language: String = "en",
    val notificationsEnabled: Boolean = false
)

@Keep
@Entity(tableName = "notifications_table")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val body: String,
    val url: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
