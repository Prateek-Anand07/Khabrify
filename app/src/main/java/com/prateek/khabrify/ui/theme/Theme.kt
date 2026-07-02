package com.prateek.khabrify.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}
private val DarkColorScheme = darkColorScheme(
    primary = KhabrifyNavy,
    secondary = KhabrifyRed,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = LightOnPrimary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = KhabrifyNavy,
    secondary = KhabrifyRed,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnPrimary,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun KhabrifyTheme(
    appThemeSetting: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appThemeSetting) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Set status bar colors nicely based on the calculated theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}