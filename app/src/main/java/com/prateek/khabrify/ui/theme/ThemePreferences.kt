package com.prateek.khabrify.ui.theme

import android.content.Context
import androidx.core.content.edit

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Get the saved theme, defaulting to SYSTEM on first install
    fun getSavedTheme(): AppTheme {
        val savedThemeName = prefs.getString("app_theme", AppTheme.SYSTEM.name)
        return try {
            AppTheme.valueOf(savedThemeName ?: AppTheme.SYSTEM.name)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    // Save the new theme
    fun saveTheme(theme: AppTheme) {
        prefs.edit { putString("app_theme", theme.name) }
    }
}