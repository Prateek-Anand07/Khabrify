package com.prateek.khabrify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


// DAO: Data Access Object
@Dao
interface ArticleDao {

    // Saves an article. If it's already saved, replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArticle(article: Article)

    // Deletes an article when the user taps "Un-save"
    @Delete
    suspend fun deleteArticle(article: Article)

    // Grabs all saved articles for the offline "Saved" screen.
    // Using Flow means the UI updates instantly if a new article is saved!
    @Query("SELECT * from saved_articles ORDER BY publishedAt DESC")
    fun getSavedArticles(): Flow<List<Article>>

    // Checks if a specific article is saved (to highlight the bookmark icon Red)
    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE url = :articleUrl)")
    fun isArticleSaved(articleUrl: String): Flow<Boolean>

    @Query("DELETE FROM saved_articles")
    suspend fun clearAllSavedArticles()
}