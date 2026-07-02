package com.prateek.khabrify.ui.notification

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri
import com.prateek.khabrify.data.NotificationEntity
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    // Collect the database state
    val notifications by viewModel.notifications.collectAsState()
    val context = LocalContext.current
    // Clear the phone's system tray when this screen opens!
    LaunchedEffect(Unit) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll() // This instantly wipes the top notification plate
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAllNotifications() }) {
                            Text(
                                text = "Clear All",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (notifications.isEmpty()) {
                // Empty State
                Text(
                    text = "No new alerts",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // The Feed
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationItemCard(
                            notification = notification,
                            onClick = {
                                viewModel.markAsRead(notification.id)
                                notification.url?.let { urlString ->
                                    if (urlString.isNotEmpty()) {
                                        viewModel.trackArticleClick(urlString)
                                        try {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, urlString.toUri())
                                            context.startActivity(browserIntent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            },
                            onDeleteClick = {
                                viewModel.deleteNotification(notification.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: NotificationEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Format the timestamp nicely
    val formatter = SimpleDateFormat("MMM dd, hh:mm a", LocalLocale.current.platformLocale)
    val dateString = formatter.format(Date(notification.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            // Subtle highlight for unread messages
            containerColor = if (!notification.isRead)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top // Ensures icon aligns with the title
        ) {
            // Unread indicator dot
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Text Content - using weight(1f) to push the delete icon to the end
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateString,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // NEW: Individual Delete Button
            IconButton(
                onClick = onDeleteClick,
                // Adjusting padding slightly so it sits perfectly flush right
                modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete Notification",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}