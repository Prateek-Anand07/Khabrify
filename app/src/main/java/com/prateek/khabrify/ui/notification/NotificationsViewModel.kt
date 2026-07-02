package com.prateek.khabrify.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prateek.khabrify.data.NewsRepository
import com.prateek.khabrify.data.NotificationDao
import com.prateek.khabrify.data.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    // 1. Fetch all notifications as a continuous Flow.
    // It automatically updates the UI whenever a new push notification arrives!
    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Mark a notification as read when the user taps on it
    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }
    fun markArticleAsReadGlobally(url: String) {
        viewModelScope.launch {
            repository.markNotificationAsReadByUrl(url)
        }
    }
    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotificationById(id)
        }
    }

    fun trackArticleClick(url: String) {
        repository.trackArticleClick(url)
    }
}