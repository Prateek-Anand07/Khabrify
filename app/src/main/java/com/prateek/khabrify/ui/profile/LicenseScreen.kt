package com.prateek.khabrify.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.prateek.khabrify.R
import com.prateek.khabrify.ui.theme.KhabrifyNavy

@Composable
fun LicenseScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current

    // 🛡️ THE STATIC OVERRIDE: Manually read our custom file
    val customJsonString = remember {
        try {
            context.resources
                .openRawResource(R.raw.my_licenses)
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            "" // Fallback if file isn't found
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // --- CUSTOM HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.offset(x = (-8).dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = KhabrifyNavy
                )
            }
            Text(
                text = "Open Source Licenses",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KhabrifyNavy
            )
        }

        // --- AUTOMATED LICENSE LIST ---
        // Inject the string directly!
        if (customJsonString.isNotEmpty()) {
            LibrariesContainer(
                aboutLibsJson = customJsonString,
                modifier = Modifier.weight(1f)
            )
        } else {
            // Failsafe error message
            Text(
                text = "Failed to load licenses.",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}