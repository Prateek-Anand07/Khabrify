package com.prateek.khabrify.ui.home

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prateek.khabrify.BuildConfig
import com.prateek.khabrify.data.Article
import com.prateek.khabrify.data.NewsRepository
import com.prateek.khabrify.ui.auth.UserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isInitialLoading: Boolean = true,     // Full screen
    val isCategoryLoading: Boolean = false,   // Below filters
    val isPaginationLoading: Boolean = false, // Bottom of list
    val trendingArticles: List<Article> = emptyList(),
    val categoryArticles: List<Article> = emptyList(),
    val errorMessage: String? = null,
    val selectedCategory: String = "general"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsRepository
): ViewModel() {
    // The Internal State (Mutable: We can change this inside the ViewModel)
    private val _uiState = MutableStateFlow(HomeUiState())
    // The Public State (Immutable: The UI can only READ this, never change it directly)
    val uiState: StateFlow<HomeUiState>  = _uiState.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false

    private var userCountry: String = "in"
    private var userLanguage: String = "en"
    private var isInitialized = false

    // Update this function to be called from the Home Screen UI
    fun syncProfilePreferences(country: String, language: String) {
        if (this.userCountry != country || this.userLanguage != language || !isInitialized) {
            this.userCountry = country
            this.userLanguage = language
            this.isInitialized = true

            // Re-fetch everything with the new settings
            fetchInitialNews()
        }
    }


    private fun fetchInitialNews() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isInitialLoading = true, errorMessage = null)
            }
            try {
                // asking repository for data
                var articles = repository.getBreakingNews("general", language = userLanguage, country = userCountry, page = currentPage)
                if (articles.isEmpty() && (userLanguage != "en" || userCountry != "in")) {
                    articles = repository.getBreakingNews("general", language = "en", page = 1)
                }
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        trendingArticles = articles.take(5),
                        categoryArticles = articles.drop(5)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isInitialLoading = false, errorMessage = "Failed to load news ${e.localizedMessage}")
                }
            }
        }
    }

    fun fetchCategoryNews(category: String, isLoadMore: Boolean = false) {
        // Safe check to prevent page overruns or double requests
        if (isLoadMore && (isLastPage || currentPage > 3)) return
        if ((_uiState.value.isCategoryLoading || _uiState.value.isPaginationLoading) && isLoadMore) return

        viewModelScope.launch {
            if (!isLoadMore) {
                currentPage = 1
                isLastPage = false
                _uiState.update { it.copy(isCategoryLoading = true, errorMessage = null, selectedCategory = category, categoryArticles = emptyList()) }
            } else {
                _uiState.update { it.copy(isPaginationLoading = true, errorMessage = null) }
            }

            try {
                var articles = repository.getBreakingNews(category = category, language = userLanguage, country = userCountry, page = currentPage)

                if (articles.isEmpty() && !isLoadMore && (userLanguage != "en" || userCountry != "in")) {
                    articles = repository.getBreakingNews(category = category, language = "en", page = currentPage)
                }

                if (articles.isEmpty()) {
                    isLastPage = true
                    _uiState.update { it.copy(isCategoryLoading = false) }
                } else {
                    currentPage++
                    _uiState.update {
                        it.copy(
                            isCategoryLoading = false,
                            isPaginationLoading = false,
                            // Append if loading more, otherwise replace
                            categoryArticles = if (isLoadMore) it.categoryArticles + articles else articles
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCategoryLoading = false, isPaginationLoading = false, errorMessage = "Failed to load news ${e.localizedMessage}") }
            }
        }
    }
    // A helper function for the UI to call when reaching the bottom
    fun loadMoreCategoryNews() {
        fetchCategoryNews(category = _uiState.value.selectedCategory, isLoadMore = true)
    }

    // 1. Observe the database continuously
    val savedArticles: StateFlow<List<Article>> = repository.getAllSavedArticle()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    // 2. The Delete Function
    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            repository.deleteArticlesFromOffline(article)
        }
    }

    // 3. The Save Function
    fun saveArticle(article: Article) {
        viewModelScope.launch {
            repository.saveArticlesToOffline(article)
        }
    }

    // 4. Wipe Local Data on Logout
    fun clearDataOnLogout() {
        viewModelScope.launch {
            repository.clearLocalDatabase()
        }
    }

    // 5. Download Cloud Data on Login
    fun syncArticlesOnLogin() {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("savedArticles").get()
            .addOnSuccessListener { documents ->
                viewModelScope.launch {
                    // Loop through every article in the cloud backup...
                    for (document in documents) {
                        try {
                            // Convert the cloud data back into an Article object
                            val article = document.toObject(Article::class.java)
                            // Save it into the local Room database!
                            repository.saveArticlesToOffline(article)
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error syncing article: ${e.message}")
                        }
                    }
                }
            }
    }
    fun refreshNews() {
        // Access the category currently stored in your state
        val categoryToRefresh = _uiState.value.selectedCategory

        // Reset the page and trigger the fetch logic
        fetchInitialNews()
    }

    fun trackArticleClick(url: String) {
        repository.trackArticleClick(url)
    }
}