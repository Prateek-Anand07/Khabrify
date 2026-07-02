package com.prateek.khabrify.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.prateek.khabrify.data.Article
import com.prateek.khabrify.ui.theme.KhabrifyNavy

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    savedArticles: List<Article>,
    onArticleClick: (Article) -> Unit,
    onRefresh: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isInitialLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = KhabrifyNavy
                    )
            }
            state.errorMessage != null && state.trendingArticles.isEmpty() -> {
                val errorString = state.errorMessage!!.lowercase()
                if (errorString.contains("internet") ||
                    errorString.contains("network") ||
                    errorString.contains("unable to resolve host") ||
                    errorString.contains("failed to connect") ||
                    errorString.contains("timeout")
                ) {
                    // Shows light grey text in the center for internet issues
                    Text(
                        text = "No Internet",
                        color = Color.LightGray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Shows the default red text for other types of errors
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
            }
            else -> {
                // SUCCESS STATE: Wrap your content in PullToRefreshBox
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeFeedContent(
                        state = state,
                        savedArticles = savedArticles,
                        onCategorySelected = { viewModel.fetchCategoryNews(it) },
                        onArticleClick = onArticleClick,
                        onToggleBookmark = { article, currentlySaved ->
                            if (currentlySaved) {
                                viewModel.deleteArticle(article)
                                Toast.makeText(context, "Removed from Saved", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveArticle(article)
                                Toast.makeText(context, "Article Saved!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onLoadMore = { viewModel.loadMoreCategoryNews() }
                    )
                }
            }
        }
    }
}