package com.prateek.khabrify.ui.home

import android.text.format.DateUtils
import android.widget.Toast
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.prateek.khabrify.data.Article
import com.prateek.khabrify.ui.theme.KhabrifyNavy
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeFeedContent(
    state: HomeUiState,
    savedArticles: List<Article>,
    onCategorySelected: (String) -> Unit,
    onArticleClick: (Article) -> Unit,
    onToggleBookmark: (Article, Boolean) -> Unit,
    onLoadMore: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Auto looping carousel
        // Only show and calculate if we actually have articles!
        if (state.trendingArticles.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TRENDING NOW",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val pagerState = rememberPagerState(pageCount = { state.trendingArticles.size })

                        LaunchedEffect(pagerState.currentPage) {
                            delay(3000.milliseconds)
                            val nextPage = (pagerState.currentPage + 1) % state.trendingArticles.size
                            pagerState.animateScrollToPage(nextPage)
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            TrendingFeatureCard(
                                article = state.trendingArticles[page],
                                onArticleClick = { clickedArticle ->
                                    onArticleClick(clickedArticle)
                                },
                            )
                        }

                        // Dot indicators
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 3.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(state.trendingArticles.size) { iteration ->
                                val color = if(pagerState.currentPage == iteration)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val categories = listOf("General", "Technology", "Business", "Sports", "Entertainment", "Nation", "World", "Science", "Health")
                items(categories) { category ->

                    // 1. Check if this specific chip is currently the active one
                    val isSelected = state.selectedCategory.equals(category, ignoreCase = true)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                // 2. If already selected, just show a Toast (No API call!)
                                Toast.makeText(context, "$category is already selected", Toast.LENGTH_SHORT).show()
                            } else {
                                // 3. If it's a new category, call the ViewModel
                                onCategorySelected(category.lowercase())
                            }
                        },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KhabrifyNavy,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // 3. Category Articles Section
        if (state.isCategoryLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KhabrifyNavy)
                }
            }
        } else {
            itemsIndexed(state.categoryArticles) { index, article ->
                val isSaved = savedArticles.any { it.url == article.url }
                ArticleListCard(
                    article = article,
                    isSaved = isSaved,
                    onArticleClick = {
                        onArticleClick(article)
                    },
                    onSaveClick = {
                        onToggleBookmark(article, isSaved)
                    }
                )

                // Make sure we aren't already loading before triggering next page
                if (index == state.categoryArticles.lastIndex && !state.isPaginationLoading) {
                    LaunchedEffect(Unit) {
                        onLoadMore()
                    }
                }
            }
        }

        // 4. Pagination Spinner Section
        if (state.isPaginationLoading && state.categoryArticles.isNotEmpty()) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    color = KhabrifyNavy
                )
            }
        }
    }
}

fun formatTimeAgo(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val pastTime = format.parse(dateString)?.time ?: return ""
        val nowTime = System.currentTimeMillis()
        DateUtils.getRelativeTimeSpanString(
            pastTime,
            nowTime,
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun ArticleListCard(
    article: Article,
    isSaved: Boolean, // Add this
    onArticleClick: (Article) -> Unit,
    onSaveClick: () -> Unit
) {
    val timeAgo = formatTimeAgo(article.publishedAt)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp)
            .clickable { onArticleClick(article) }
    ) {
        AsyncImage(
            model = article.image,
            contentDescription = article.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = article.title ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSaveClick,
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = (-4).dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) Color.Red else Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${article.sourceName ?: "Unknown"} • $timeAgo",
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}


@Composable
fun TrendingFeatureCard(article: Article, onArticleClick: (Article) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(200.dp)
            .clickable { onArticleClick(article) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            AsyncImage(
                model = article.image,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(0.5f),
                                Color.Black.copy(0.9f) // very dark at bottom
                            ),
                            startY = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = article.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = article.sourceName ?: "",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}