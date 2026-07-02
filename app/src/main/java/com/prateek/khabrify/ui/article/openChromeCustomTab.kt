package com.prateek.khabrify.ui.article

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.graphics.toArgb
import com.prateek.khabrify.ui.theme.KhabrifyNavy
import androidx.core.net.toUri

fun openChromeCustomTab(context: Context, url: String) {
    try {
        // 1. Set up the colors to match Khabrify's theme
        val colorInt = KhabrifyNavy.toArgb()
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(colorInt)
            .build()

        // 2. Build the Custom Tab Intent
        val builder = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(defaultColors)
            // Adds a native back button arrow instead of an 'X'
            .setShareState(CustomTabsIntent.SHARE_STATE_ON)
            .setShowTitle(true)

        val customTabsIntent = builder.build()

        // 3. Launch it!
        customTabsIntent.launchUrl(context, url.toUri())

    } catch (e: Exception) {
        // Fallback just in case the URL is broken or no browser is installed
        Toast.makeText(context, "Could not open article", Toast.LENGTH_SHORT).show()
    }
}