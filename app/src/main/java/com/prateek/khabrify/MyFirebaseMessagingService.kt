package com.prateek.khabrify

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.prateek.khabrify.data.NotificationDao
import com.prateek.khabrify.data.NotificationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint // REQUIRED: Tells Hilt it is allowed to inject dependencies here
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // INJECT YOUR DAO: Hilt automatically grabs this from your AppModule
    @Inject
    lateinit var notificationDao: NotificationDao

    // CREATE A SCOPE: Database operations must run on a background thread (IO)
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "New News"
        val body = remoteMessage.data["body"] ?: ""
        val articleUrl = remoteMessage.data["url"]
        serviceScope.launch {
            try {
                // 1. Save to database
                val entity = NotificationEntity(title = title, body = body, url = articleUrl)
                notificationDao.insertNotification(entity)

                // 2. Fetch the total number of unread messages
                val unreadCount = notificationDao.getUnreadCount()

                // 3. Pass the count to the notification builder
                sendNotification(title, body, articleUrl, unreadCount)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String, articleUrl: String?, unreadCount: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            articleUrl?.let { putExtra("url", it) }
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "news_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.k_icon_bg)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setNumber(unreadCount)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "News Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}