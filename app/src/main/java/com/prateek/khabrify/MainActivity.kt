package com.prateek.khabrify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.prateek.khabrify.ui.KhabrifyApp
import com.prateek.khabrify.ui.home.HomeViewModel
import com.prateek.khabrify.ui.theme.AppTheme
import com.prateek.khabrify.ui.theme.KhabrifyTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.prateek.khabrify.data.NewsRepository
import com.prateek.khabrify.ui.theme.ThemePreferences
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repository: NewsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleNotificationIntent(intent)

        setContent {
            val context = LocalContext.current
            val themePreferences = remember { ThemePreferences(context) }

            // Load saved theme instead of default SYSTEM
            var currentTheme by remember {
                mutableStateOf(themePreferences.getSavedTheme())
            }

            KhabrifyTheme(appThemeSetting = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val homeViewModel: HomeViewModel = hiltViewModel()

                    KhabrifyApp(
                        homeViewModel = homeViewModel,
                        currentTheme = currentTheme,
                        onThemeChange = { selectedTheme ->
                            currentTheme = selectedTheme
                            themePreferences.saveTheme(selectedTheme)
                        }
                    )
                }
            }
        }
    }

    // 2. ADD THIS: Catch the notification click if the app was already running in the background
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    // The master helper function that does both jobs!
    private fun handleNotificationIntent(intent: Intent?) {
        val articleUrl = intent?.getStringExtra("url")
        if (!articleUrl.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    // Mark the in-app notification dot as read
                    repository.markNotificationAsReadByUrl(articleUrl)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error updating database", e)
                }
            }
            repository.trackArticleClick(articleUrl)
            openArticleInBrowser(articleUrl)
        }
    }

    // The function that actually opens the web browser to the news link
    private fun openArticleInBrowser(url: String) {
        if (url.isNotEmpty()) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(browserIntent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error opening article URL", e)
            }
        }
    }
}