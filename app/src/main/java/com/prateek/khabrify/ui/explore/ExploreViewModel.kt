package com.prateek.khabrify.ui.explore

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prateek.khabrify.data.Article
import com.prateek.khabrify.data.NewsRepository
import com.prateek.khabrify.data.UserProfile
import com.prateek.khabrify.ui.auth.UserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

// 1. Create a State class to hold all UI information
data class ExploreUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: NewsRepository
): ViewModel() {
    // This holds the articles specifically for the Explore screen
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    // Remember the active filters
    private var currentCategory = "general"
    private var currentLanguage = "en"
    // Track the current search query so we know which endpoint to refresh
    private var currentQuery = ""
    private var currentFromDate: String? = null
    private var currentPage = 1
    private var isLastPage = false
    private var currentCountry: String? = "in"
    private var lastSyncedProfileCountry: String? = null
    private var lastSyncedProfileLanguage: String? = null
    private var isInitialized = false

    // Replace your current sync function with this updated one
    fun syncProfilePreferences(profileCountry: String, profileLanguage: String) {
        // Did the user ACTUALLY change their profile in the database?
        val didProfileChangeInDb = lastSyncedProfileCountry != profileCountry ||
                lastSyncedProfileLanguage != profileLanguage

        // Only override the active filters if it's the very first load
        // OR if the user went to the Profile screen and changed their permanent DB settings
        if (!isInitialized || didProfileChangeInDb) {

            // Save these new profile settings into memory so we don't trigger this again
            lastSyncedProfileCountry = profileCountry
            lastSyncedProfileLanguage = profileLanguage

            // Update the active search parameters
            currentCountry = profileCountry
            currentLanguage = profileLanguage
            isInitialized = true

            // Fetch the news!
            fetchSuggestedArticles()
        }
    }

    private fun fetchSuggestedArticles(isLoadMore: Boolean = false) {
        if (isLoadMore && (isLastPage || currentPage > 4)) return
        viewModelScope.launch {
            if (!isLoadMore) {
                currentPage = 1
                isLastPage = false
                _uiState.update { it.copy(isLoading = true, errorMessage = null, articles = emptyList()) }
            } else {
                // If we are just loading more, don't clear the list, just show loading
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            try {
                val rawResponse = repository.getBreakingNews(
                    category = currentCategory,
                    language = currentLanguage,
                    country = currentCountry,
                    fromDate = currentFromDate,
                    page = currentPage // Pass the page!
                )
                // Forcefully drop any old articles locally!
                val response = if (currentFromDate != null) {
                    rawResponse.filter { article ->
                        // Assuming Article data class has a 'publishedAt' string field.
                        // ISO 8601 strings can be compared alphabetically!
                        article.publishedAt!! >= currentFromDate!!
                    }
                } else {
                    rawResponse
                }
                if (response.isEmpty()) {
                    isLastPage = true
                    // Turn off loading spinner even if empty
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    currentPage++
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // Append if loading more, otherwise it's just the new response
                            articles = if (isLoadMore) it.articles + response else response
                        )
                    }
                }
            } catch (e: Exception) {
                // 5. On error, turn off loading and pass the error message
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load news: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
    fun searchArticles(query: String, isLoadMore: Boolean = false) {
        val cleanQuery = query.trim()
        currentQuery = cleanQuery

        if (cleanQuery.isBlank()) {
            fetchSuggestedArticles(isLoadMore = false)
            return
        }

        if (isLoadMore && (isLastPage || currentPage > 4)) return // Stop fetching if we reached the end

        viewModelScope.launch {
            if (!isLoadMore) {
                currentPage = 1
                isLastPage = false
                _uiState.update { it.copy(isLoading = true, errorMessage = null, articles = emptyList()) }
            } else {
                // ADD THIS ELSE BLOCK! It turns on the bottom spinner when loading next page of a search
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            try {
                val rawResponse = repository.searchNews(
                    query = cleanQuery,
                    language = currentLanguage,
                    country = currentCountry,
                    fromDate = currentFromDate,
                    page = currentPage // Pass the page!
                )
                // Forcefully drop any old articles locally!
                val response = if (currentFromDate != null) {
                    rawResponse.filter { article ->
                        article.publishedAt!! >= currentFromDate!!
                    }
                } else {
                    rawResponse
                }

                if (response.isEmpty()) {
                    isLastPage = true
                    // Turn off loading spinner even if empty
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    currentPage++
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // Append if loading more
                            articles = if (isLoadMore) it.articles + response else response
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Search failed: ${e.localizedMessage}") }
            }
        }
    }

    fun updateCountry(countryCode: String?) {
        currentCountry = countryCode
        if (currentQuery.isNotBlank()) {
            searchArticles(currentQuery)
        } else {
            fetchSuggestedArticles()
        }
    }

    fun loadMoreNews() {
        if (_uiState.value.isLoading) return // Don't trigger if already loading

        if (currentQuery.isNotBlank()) {
            searchArticles(currentQuery, isLoadMore = true)
        } else {
            fetchSuggestedArticles(isLoadMore = true)
        }
    }

    // 4. Update Category Filter
    fun updateCategory(newCategory: String) {
        // GNews expects lowercase categories (e.g., "business")
        currentCategory = newCategory.lowercase()
        currentQuery = "" // Reset search when clicking a new category
        fetchSuggestedArticles()
    }

    // 5. Update Language Filter
    fun updateLanguage(langCode: String) {
        currentLanguage = langCode
        // If they are searching, re-run the search in the new language.
        // Otherwise, re-run top headlines in the new language.
        if (currentQuery.isNotBlank()) {
            searchArticles(currentQuery)
        } else {
            fetchSuggestedArticles()
        }
    }

    // Create the function for the UI to call
    fun updateDateFilter(daysAgo: Int?) {
        // If daysAgo is null, they selected "Any Time". Otherwise, calculate the string.
        currentFromDate = if (daysAgo != null) getIso8601Date(daysAgo) else null

        if (currentQuery.isNotBlank()) {
            searchArticles(currentQuery)
        } else {
            fetchSuggestedArticles()
        }
    }
    fun resetFilters(defaultCountry: String, defaultLanguage: String) {
        currentCategory = "general"
        currentQuery = ""
        currentFromDate = null

        // Reset back to the user's synced profile preferences
        currentCountry = defaultCountry
        currentLanguage = defaultLanguage

        fetchSuggestedArticles()
    }
}

fun getIso8601Date(daysAgo: Int): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(calendar.time)
}
