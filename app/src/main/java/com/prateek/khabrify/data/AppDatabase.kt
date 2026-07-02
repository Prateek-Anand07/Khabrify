package com.prateek.khabrify.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Add NotificationEntity to the entities array
@Database(
    entities = [Article::class, NotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Existing abstract function for articles
    abstract fun articleDao(): ArticleDao

    // 2. Add the abstract function for notifications
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "khabrify_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}