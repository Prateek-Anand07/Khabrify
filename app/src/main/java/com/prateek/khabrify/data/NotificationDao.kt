package com.prateek.khabrify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    // Using Flow automatically updates the UI when a new message arrives
    @Query("SELECT * FROM notifications_table ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications_table SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications_table SET isRead = 1 WHERE url = :url")
    suspend fun markAsReadByUrl(url: String)

    @Query("DELETE FROM notifications_table")
    suspend fun deleteAllNotifications()

    @Query("DELETE FROM notifications_table WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    @Query("SELECT COUNT(*) FROM notifications_table WHERE isRead = 0")
    suspend fun getUnreadCount(): Int
}