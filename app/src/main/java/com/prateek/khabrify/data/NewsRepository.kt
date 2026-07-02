package com.prateek.khabrify.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prateek.khabrify.data.mapper.toArticle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import androidx.core.content.edit
import com.google.firebase.firestore.FieldValue

class NewsRepository @Inject constructor(
    private val api: GNewsApiService,
    private val dao: ArticleDao,
    private val notificationDao: NotificationDao,
    @ApplicationContext private val context: Context
) {
    // Initialize Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    // Fetching live news
    suspend fun getBreakingNews(
        category: String,
        language: String,
        country: String? = null,
        fromDate: String? = null,
        page: Int = 1
    ): List<Article> {
        val response = api.getTopHeadlines(category = category, language = language, country = country, page = page)
        return response.articles.map { it.toArticle() }
    }

    suspend fun searchNews(
        query: String,
        language: String,
        country: String? = null,
        fromDate: String? = null,
        page: Int = 1
    ): List<Article> {
        val response = api.searchArticles(query = query, language = language, country = country, page = page)
        return response.articles.map { it.toArticle() }
    }

    // --- HYBRID OFFLINE-FIRST BOOKMARK LOGIC ---

    fun getAllSavedArticle() = dao.getSavedArticles()

    suspend fun saveArticlesToOffline(article: Article) {
        // 1. Instant UI update: Save to Room Database locally
        dao.saveArticle(article)

        // 2. Silent Cloud Backup: Send to Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Firestore document IDs cannot contain slashes ('/').
            // We use the URL's hashCode as a unique, safe Document ID!
            val safeDocId = article.url.hashCode().toString()

            db.collection("users").document(userId)
                .collection("savedArticles").document(safeDocId)
                .set(article) // Backs up the entire article object to the cloud
        }
    }

    suspend fun deleteArticlesFromOffline(article: Article) {
        dao.deleteArticle(article)
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val safeDocId = article.url.hashCode().toString()
            db.collection("users").document(userId)
                .collection("savedArticles").document(safeDocId)
                .delete()
        }
    }

    suspend fun clearLocalDatabase() {
        dao.clearAllSavedArticles()
    }

    // -------Notifications--------

    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun insertNotification(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }
    suspend fun markNotificationAsReadByUrl(url: String) {
        notificationDao.markAsReadByUrl(url)
    }

    suspend fun deleteAllNotifications() {
        notificationDao.deleteAllNotifications()
    }

    suspend fun deleteNotificationById(id: Int) {
        notificationDao.deleteNotificationById(id)
    }

    fun trackArticleClick(url: String) {
        val prefs = context.getSharedPreferences("khabrify_read_history", Context.MODE_PRIVATE)
        val safeDocId = url.hashCode().toString()
        if (!prefs.getBoolean(safeDocId, false)) {
            prefs.edit { putBoolean(safeDocId, true) }
            val uid = auth.currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .update("articlesRead", FieldValue.increment(1))
            }
        }
    }
}