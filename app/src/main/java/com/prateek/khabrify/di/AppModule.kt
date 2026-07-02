package com.prateek.khabrify.di

import android.content.Context
import com.prateek.khabrify.data.AppDatabase
import com.prateek.khabrify.data.ArticleDao
import com.prateek.khabrify.data.GNewsApiService
import com.prateek.khabrify.data.NewsRepository
import com.prateek.khabrify.data.NotificationDao
import com.prateek.khabrify.data.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: AppDatabase): ArticleDao {
        return database.articleDao()
    }

    // ADD THIS PROVIDER: Grabs the notification DAO from the main database instance
    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideNewsApiService(): GNewsApiService {
        return RetrofitClient.apiService
    }

    @Provides
    @Singleton
    fun provideNewsRepository(
        apiService: GNewsApiService,
        articleDao: ArticleDao,
        notificationDao: NotificationDao,
        @ApplicationContext context: Context
    ): NewsRepository {
        return NewsRepository(apiService, articleDao, notificationDao, context)
    }
}