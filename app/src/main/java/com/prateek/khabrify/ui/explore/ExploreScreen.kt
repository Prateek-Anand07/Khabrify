package com.prateek.khabrify.ui.explore

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prateek.khabrify.data.Article
import com.prateek.khabrify.ui.home.ArticleListCard
import com.prateek.khabrify.ui.theme.KhabrifyNavy

@Composable
fun ExploreScreen(
    uiState: ExploreUiState,
    savedArticles: List<Article>,
    userDefaultCountryCode: String = "in",
    userDefaultLanguageCode: String = "en",
    onArticleClick: (Article) -> Unit,
    onToggleBookmark: (Article, Boolean) -> Unit,
    onSearch: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onCountrySelected: (String?) -> Unit,
    onDateSelected: (Int?) -> Unit,
    onLoadMore: () -> Unit,
    onResetFilters: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // --- FULL MAPS MOVED UP HERE ---
    val languages = mapOf(
        "ar" to "Arabic", "bn" to "Bengali", "bg" to "Bulgarian", "ca" to "Catalan",
        "zh" to "Chinese", "cs" to "Czech", "nl" to "Dutch", "en" to "English",
        "et" to "Estonian", "fi" to "Finnish", "fr" to "French", "de" to "German",
        "el" to "Greek", "gu" to "Gujarati", "he" to "Hebrew", "hi" to "Hindi",
        "hu" to "Hungarian", "id" to "Indonesian", "it" to "Italian", "ja" to "Japanese",
        "ko" to "Korean", "lv" to "Latvian", "lt" to "Lithuanian", "ml" to "Malayalam",
        "mr" to "Marathi", "no" to "Norwegian", "pl" to "Polish", "pt" to "Portuguese",
        "pa" to "Punjabi", "ro" to "Romanian", "ru" to "Russian", "sk" to "Slovak",
        "sl" to "Slovenian", "es" to "Spanish", "sv" to "Swedish", "ta" to "Tamil",
        "te" to "Telugu", "th" to "Thai", "tr" to "Turkish", "uk" to "Ukrainian",
        "vi" to "Vietnamese"
    )

    val countries = mapOf(
        "global" to "Global", "ar" to "Argentina", "au" to "Australia", "at" to "Austria",
        "bd" to "Bangladesh", "be" to "Belgium", "bw" to "Botswana", "br" to "Brazil",
        "bg" to "Bulgaria", "ca" to "Canada", "cl" to "Chile", "cn" to "China",
        "co" to "Colombia", "cu" to "Cuba", "cz" to "Czechia", "eg" to "Egypt",
        "ee" to "Estonia", "et" to "Ethiopia", "fi" to "Finland", "fr" to "France",
        "de" to "Germany", "gh" to "Ghana", "gr" to "Greece", "hk" to "Hong Kong",
        "hu" to "Hungary", "in" to "India", "id" to "Indonesia", "ie" to "Ireland",
        "il" to "Israel", "it" to "Italy", "jp" to "Japan", "ke" to "Kenya",
        "lv" to "Latvian", "lb" to "Lebanon", "lt" to "Lithuania", "my" to "Malaysia",
        "mx" to "Mexico", "ma" to "Morocco", "na" to "Namibia", "nl" to "Netherlands",
        "nz" to "New Zealand", "ng" to "Nigeria", "no" to "Norway", "pk" to "Pakistan",
        "pe" to "Peru", "ph" to "Philippines", "pl" to "Poland", "pt" to "Portugal",
        "ro" to "Romania", "ru" to "Russia", "sa" to "Saudi Arabia", "sn" to "Senegal",
        "sg" to "Singapore", "sk" to "Slovakia", "si" to "Slovenia", "za" to "South Africa",
        "kr" to "South Korea", "es" to "Spain", "se" to "Sweden", "ch" to "Switzerland",
        "tw" to "Taiwan", "tz" to "Tanzania", "th" to "Thailand", "tr" to "Turkey",
        "ug" to "Uganda", "ua" to "Ukraine", "ae" to "United Arab Emirates",
        "gb" to "United Kingdom", "us" to "United States", "ve" to "Venezuela",
        "vn" to "Vietnam", "zw" to "Zimbabwe"
    )

    // Calculate default display names based on the codes passed in
    val defaultDisplayLanguage = languages[userDefaultLanguageCode] ?: "English"
    val defaultDisplayCountry = countries[userDefaultCountryCode] ?: "India"

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Category") }
    var selectedTimeLabel by rememberSaveable { mutableStateOf("Time") }

    // 1. Keep your initial state declarations
    var selectedLanguageName by rememberSaveable { mutableStateOf(defaultDisplayLanguage) }
    var selectedCountryName by rememberSaveable { mutableStateOf(defaultDisplayCountry) }

    // 2. Add these memory trackers so the UI knows if the DB actually updated
    var lastSeenProfileLanguage by rememberSaveable { mutableStateOf(userDefaultLanguageCode) }
    var lastSeenProfileCountry by rememberSaveable { mutableStateOf(userDefaultCountryCode) }

    // 3. Update the LaunchedEffects to check the trackers before overwriting
    LaunchedEffect(userDefaultLanguageCode) {
        // ONLY forcefully update the UI chip if the actual DB profile changed
        if (userDefaultLanguageCode != lastSeenProfileLanguage) {
            selectedLanguageName = languages[userDefaultLanguageCode] ?: "English"
            lastSeenProfileLanguage = userDefaultLanguageCode // Update tracker
        }
    }

    LaunchedEffect(userDefaultCountryCode) {
        if (userDefaultCountryCode != lastSeenProfileCountry) {
            selectedCountryName = countries[userDefaultCountryCode] ?: "India"
            lastSeenProfileCountry = userDefaultCountryCode // Update tracker
        }
    }

    // Dialog states for large lists
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }

    // DIALOG RENDERERS
    if (showLanguageDialog) {
        SelectionDialog(
            title = "Select Language",
            items = languages,
            onDismiss = { showLanguageDialog = false },
            onItemSelected = { langCode ->
                val langName = languages[langCode] ?: "Language"
                if (selectedLanguageName == langName) {
                    Toast.makeText(context, "$langName is already applied", Toast.LENGTH_SHORT).show()
                } else {
                    selectedLanguageName = langName
                    onLanguageSelected(langCode)
                }
                showLanguageDialog = false
            }
        )
    }

    if (showCountryDialog) {
        SelectionDialog(
            title = "Select Location",
            items = countries,
            onDismiss = { showCountryDialog = false },
            onItemSelected = { countryCode ->
                val countryName = countries[countryCode] ?: "Country"
                val displayTargetText = if (countryCode == "global") "Global" else countryName

                if (selectedCountryName == displayTargetText) {
                    Toast.makeText(context, "$displayTargetText is already applied", Toast.LENGTH_SHORT).show()
                } else {
                    selectedCountryName = displayTargetText
                    onCountrySelected(if (countryCode == "global") null else countryCode)
                }
                showCountryDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. SEARCH BAR ---
        TextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
                if (newValue.isBlank()) {
                    onSearch("")
                }
            },
            placeholder = { Text("Search news, topics, or sources...", color = Color.Gray) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onSearch("")
                        focusManager.clearFocus()
                    }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF3F4F6),
                unfocusedContainerColor = Color(0xFFF3F4F6),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(searchQuery)
                    focusManager.clearFocus()
                }
            )
        )

        // --- 2. FILTER CHIPS ROW ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            item {
                ExploreFilterChip(
                    text = "Reset",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Filters",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    },
                    onClick = {
                        // NEW: Checks if it's already reset back to the USER'S specific default!
                        val isAlreadyDefault = searchQuery.isEmpty() &&
                                selectedCategory == "Category" &&
                                selectedTimeLabel == "Time" &&
                                selectedLanguageName == defaultDisplayLanguage &&
                                selectedCountryName == defaultDisplayCountry

                        if (!isAlreadyDefault) {
                            searchQuery = ""
                            selectedCategory = "Category"
                            selectedTimeLabel = "Time"
                            selectedLanguageName = defaultDisplayLanguage
                            selectedCountryName = defaultDisplayCountry

                            // Important: Ensure your ViewModel's onResetFilters function ALSO
                            // resets to the user DB defaults, not hardcoded India/English.
                            onResetFilters()
                        }
                        Toast.makeText(context, "Filters reset to your defaults", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Category Dropdown (Standard)
            item {
                var expanded by remember { mutableStateOf(false) }
                val categories = listOf("General", "World", "Nation", "Business", "Technology", "Entertainment", "Sports", "Science", "Health")
                Box {
                    ExploreFilterChip(
                        text = selectedCategory,
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
                        onClick = { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    val targetText = if (category == "General") "Category" else category
                                    if (selectedCategory == targetText) {
                                        expanded = false
                                        Toast.makeText(context, "$category is already applied", Toast.LENGTH_SHORT).show()
                                    } else {
                                        selectedCategory = targetText
                                        expanded = false
                                        onCategorySelected(category)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Language Dialog Trigger
            item {
                ExploreFilterChip(
                    text = selectedLanguageName,
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
                    onClick = { showLanguageDialog = true }
                )
            }

            // Location Dialog Trigger
            item {
                ExploreFilterChip(
                    text = selectedCountryName,
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
                    onClick = { showCountryDialog = true }
                )
            }

            // Time Dropdown (Standard)
            item {
                var expanded by remember { mutableStateOf(false) }
                val timeOptions = mapOf("Any Time" to null, "Past 24 Hours" to 1, "Past Week" to 7, "Past Month" to 30)

                Box {
                    ExploreFilterChip(
                        text = selectedTimeLabel,
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
                        onClick = { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timeOptions.forEach { (label, days) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    val targetText = if (label == "Any Time") "Time" else label
                                    if (selectedTimeLabel == targetText) {
                                        expanded = false
                                        Toast.makeText(context, "$label is already applied", Toast.LENGTH_SHORT).show()
                                    } else {
                                        selectedTimeLabel = targetText
                                        expanded = false
                                        onDateSelected(days)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. SECTION TITLE ---
        Text(
            text = "Suggested Articles",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. THE NEWS LIST ---
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading && uiState.articles.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = KhabrifyNavy)
                }
                uiState.errorMessage != null && uiState.articles.isEmpty() -> {
                    val errorString = uiState.errorMessage.lowercase()
                    if (errorString.contains("internet") || errorString.contains("network") ||
                        errorString.contains("host") || errorString.contains("connect") || errorString.contains("timeout")
                    ) {
                        Text(text = "No Internet", color = Color.LightGray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        Text(text = uiState.errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    }
                }
                uiState.articles.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "No Results", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No results found for \"$searchQuery\"" else "No articles found",
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Try adjusting your filters or search for something else.", color = Color.Gray.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(uiState.articles) { index, article ->
                            val isSaved = savedArticles.any { it.url == article.url }
                            ArticleListCard(
                                article = article,
                                isSaved = isSaved,
                                onArticleClick = { onArticleClick(article) },
                                onSaveClick = { onToggleBookmark(article, isSaved) }
                            )
                            if (index == uiState.articles.lastIndex && !uiState.isLoading) {
                                LaunchedEffect(Unit) { onLoadMore() }
                            }
                        }
                        if (uiState.isLoading && uiState.articles.isNotEmpty()) {
                            item {
                                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp).wrapContentWidth(Alignment.CenterHorizontally))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreFilterChip(
    text: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = text, color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                trailingIcon()
            }
        }
    }
}

// Searchable List Selection Dialog
@Composable
fun SelectionDialog(
    title: String,
    items: Map<String, String>,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = items.filter {
        it.value.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (filteredItems.isEmpty()) {
                        item {
                            Text(
                                text = "No results found",
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredItems.entries.toList()) { entry ->
                            Text(
                                text = entry.value,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onItemSelected(entry.key) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}