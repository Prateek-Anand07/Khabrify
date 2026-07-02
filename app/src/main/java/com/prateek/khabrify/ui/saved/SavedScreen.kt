package com.prateek.khabrify.ui.saved

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.prateek.khabrify.data.Article
import com.prateek.khabrify.ui.home.formatTimeAgo
import com.prateek.khabrify.ui.theme.KhabrifyNavy
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.prateek.khabrify.ui.article.openChromeCustomTab
import com.prateek.khabrify.ui.theme.KhabrifyRed

@Composable
fun SavedScreen(
    savedArticles: List<Article>,
    onArticleClick: (Article) -> Unit,
    onDeleteClick: (Article) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Screen Header ("Saved Stories" & Badge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saved News",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Custom Badge for Article Count
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${savedArticles.size} articles",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. The List of Saved Articles
        if (savedArticles.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bookmark,
                    contentDescription = "No Saved Articles",
                    modifier = Modifier.size(64.dp),
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No saved articles yet",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the bookmark icon on any news story to save it for later.",
                    color = Color.Gray.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedArticles) { article ->
                    SavedArticleCard(
                        article = article,
                        onClick = {
                            onArticleClick(article)
                        },
                        onDeleteClick = { onDeleteClick(article) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedArticleCard(
    article: Article,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Create a state variable to control the dialog visibility
    var showDeleteDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Image
            AsyncImage(
                model = article.image,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Right Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Category & Time
                Text(
                    text = formatTimeAgo(article.publishedAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = article.title ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Source and Delete Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.sourceName ?: "Unknown Publisher",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Remove from saved",
                            tint = KhabrifyRed
                        )
                    }
                }
            }
        }
    }

    // 3. The Alert Dialog (This only appears when showDeleteDialog is true)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false }, // Closes if user taps outside the box
            title = {
                Text(
                    text = "Delete Article",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = "Are you sure you want to remove this article from your saved news?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false // Close the dialog
                        onDeleteClick()          // Actually delete the article
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error) // Red text for destructive action
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false } // Just close the dialog
                ) {
                    Text("Cancel", color = KhabrifyNavy)
                }
            }
        )
    }
}

