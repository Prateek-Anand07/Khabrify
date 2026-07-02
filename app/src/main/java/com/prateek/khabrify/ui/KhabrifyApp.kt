package com.prateek.khabrify.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.prateek.khabrify.data.Article
import com.prateek.khabrify.ui.article.openChromeCustomTab
import com.prateek.khabrify.ui.auth.AuthScreen
import com.prateek.khabrify.ui.auth.UserViewModel
import com.prateek.khabrify.ui.explore.ExploreScreen
import com.prateek.khabrify.ui.explore.ExploreViewModel
import com.prateek.khabrify.ui.home.HomeScreen
import com.prateek.khabrify.ui.home.HomeViewModel
import com.prateek.khabrify.ui.navigation.BottomNavItem
import com.prateek.khabrify.ui.navigation.KhabrifyBottomNavigationBar
import com.prateek.khabrify.ui.notification.NotificationsScreen
import com.prateek.khabrify.ui.profile.AboutScreen
import com.prateek.khabrify.ui.profile.EditProfileScreen
import com.prateek.khabrify.ui.profile.HelpCenterScreen
import com.prateek.khabrify.ui.profile.LicenseScreen
import com.prateek.khabrify.ui.profile.ProfileScreen
import com.prateek.khabrify.ui.saved.SavedScreen
import com.prateek.khabrify.ui.theme.AppTheme
import com.prateek.khabrify.ui.theme.KhabrifyNavy

@Composable
fun KhabrifyApp(
    homeViewModel: HomeViewModel,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val context = LocalContext.current

    // 1. Get Firebase Auth instance
    val auth = FirebaseAuth.getInstance()

    // 2. Remember the current user state
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    // 3. The Router Logic
    if (currentUser == null) {
        // User is NOT logged in -> Show Login Screen
        AuthScreen(
            onLoginSuccess = {
                // 1. Download their bookmarks from the cloud to Room
                homeViewModel.syncArticlesOnLogin()

                // 2. Refresh the user state
                currentUser = auth.currentUser
            }
        )
    } else {
        // User IS logged in -> Show the Main App
        MainAppScaffold(
            homeViewModel = homeViewModel,
            onArticleClick = { article ->
                if (article.url.isNotBlank()) {
                    openChromeCustomTab(context, article.url)
                    homeViewModel.trackArticleClick(article.url)
                } else {
                    Toast.makeText(context, "Link unavailable", Toast.LENGTH_SHORT).show()
                }
            },
            onLogoutSuccess = {
                currentUser = null
            },
            currentTheme = currentTheme,
            onThemeChange = onThemeChange
        )
    }
}

@Composable
fun MainAppScaffold(
    homeViewModel: HomeViewModel,
    onArticleClick: (Article) -> Unit,
    onLogoutSuccess: () -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val bottomNavController = rememberNavController()
    val savedArticles by homeViewModel.savedArticles.collectAsState()
    val context = LocalContext.current // Used for the dummy logout toast

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isNotificationsEnabled by remember { mutableStateOf(true) }

    val screensWithBottomBar = listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Explore.route,
        BottomNavItem.Saved.route,
        BottomNavItem.Profile.route
    )

    Scaffold(
        topBar = {
            if (currentRoute != BottomNavItem.Explore.route && currentRoute != "edit_profile") {
                KhabrifyTopBar(
                    isNotificationsEnabled = isNotificationsEnabled,
                    onNotificationClick = {
                        bottomNavController.navigate("notifications")
                    }
                )
            }
        },
        bottomBar = {
            // Only show the bottom bar on main tabs, hide it on Edit Profile!
            if (currentRoute in screensWithBottomBar) {
                KhabrifyBottomNavigationBar(bottomNavController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Screen 1: Home
            composable(BottomNavItem.Home.route) {
                // 1. Get the instance of the UserViewModel (using shared scoping if needed)
                val userViewModel: UserViewModel = hiltViewModel()
                // 2. Access the property from the instance, not the class name
                val profile by userViewModel.userProfile.collectAsState()
                LaunchedEffect(profile) {
                    profile?.let {
                        homeViewModel.syncProfilePreferences(
                            country = it.country,
                            language = it.language
                        )
                    }
                }
                HomeScreen(
                    viewModel = homeViewModel,
                    savedArticles = savedArticles,
                    onArticleClick = onArticleClick,
                    onRefresh = { homeViewModel.refreshNews() }
                )
            }

            // Screen 2: Explore
            composable(BottomNavItem.Explore.route) {
                val exploreViewModel: ExploreViewModel = hiltViewModel()
                val exploreState by exploreViewModel.uiState.collectAsState()
                // 1. Get the instance of the UserViewModel (using shared scoping if needed)
                val userViewModel: UserViewModel = hiltViewModel()
                // 2. Access the property from the instance, not the class name
                val userProfile by userViewModel.userProfile.collectAsState()
                if(userProfile != null) {
                    LaunchedEffect(userProfile!!.country, userProfile!!.language) {
                        exploreViewModel.syncProfilePreferences(
                            profileCountry = userProfile!!.country,
                            profileLanguage = userProfile!!.language
                        )
                    }
                    ExploreScreen(
                        uiState = exploreState,
                        savedArticles = savedArticles,
                        userDefaultCountryCode = userProfile!!.country,
                        userDefaultLanguageCode = userProfile!!.language,
                        onArticleClick = onArticleClick,
                        onToggleBookmark = { article, isSaved ->
                            if (isSaved) homeViewModel.deleteArticle(article)
                            else homeViewModel.saveArticle(article)
                        },
                        onSearch = { query -> exploreViewModel.searchArticles(query) },
                        onCategorySelected = { category -> exploreViewModel.updateCategory(category) },
                        onLanguageSelected = { langCode -> exploreViewModel.updateLanguage(langCode) },
                        onDateSelected = { daysAgo -> exploreViewModel.updateDateFilter(daysAgo) },
                        onLoadMore = { exploreViewModel.loadMoreNews() },
                        onResetFilters = { exploreViewModel.resetFilters(
                            defaultCountry = userProfile!!.country,
                            defaultLanguage = userProfile!!.language
                        ) },
                        onCountrySelected = { countryCode -> exploreViewModel.updateCountry(countryCode) }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Screen 3: Saved
            composable(BottomNavItem.Saved.route) {
                SavedScreen(
                    savedArticles = savedArticles,
                    onArticleClick = onArticleClick,
                    onDeleteClick = { article -> homeViewModel.deleteArticle(article) }
                )
            }

            // Screen 4: Profile
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    savedArticlesCount = savedArticles.size, // Passes the dynamic number!
                    onLogoutClick = {
                        // Wipe the local Room database
                        homeViewModel.clearDataOnLogout()
                        // 1. Tell Firebase to log the user out
                        FirebaseAuth.getInstance().signOut()

                        // 2. Tell the App Router to switch screens
                        onLogoutSuccess()

                        // 3. Show a nice message
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    },
                    onEditProfileClick = {
                        // Triggers transition route to the clean edit window page
                        bottomNavController.navigate("edit_profile")
                    },
                    onHelpCenterClick = {
                        bottomNavController.navigate("help_center")
                    },
                    onAboutClick = {
                        bottomNavController.navigate("about")
                    },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }

            // Screen 5: Edit Profile (NEW ROUTE ENTRY)
            composable("edit_profile") {
                EditProfileScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }

            // Screen 6: HELP
            composable("help_center") {
                HelpCenterScreen()
            }
            // Screen 7: ABOUT
            composable("about") {
                AboutScreen(
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onNavigateToLicenses = { bottomNavController.navigate("licenses") }
                )
            }
            composable("licenses") {
                LicenseScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhabrifyTopBar(
    isNotificationsEnabled: Boolean,
    onNotificationClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Khabrify",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
            )
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = if (isNotificationsEnabled) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = Color.Unspecified,
            titleContentColor = Color.Unspecified
        )
    )
}