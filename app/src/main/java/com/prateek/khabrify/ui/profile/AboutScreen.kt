package com.prateek.khabrify.ui.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.prateek.khabrify.R

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLicenses: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- CUSTOM HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.offset(x = (-8).dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "About",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // --- 1. APP BRANDING ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.k_icon_bg),
                contentDescription = "Khabrify Logo",
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Khabrify",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Stay updated. Stay ahead!",
            style = MaterialTheme.typography.bodyLarge, // Replaces hardcoded 16.sp
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.labelLarge, // Replaces hardcoded 14.sp
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. OUR MISSION / DESCRIPTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            // FIX: Use 'surface' so it matches the bottom card exactly
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Personalized News Companion",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    // FIX: Changed from 'primary' to 'onSurface' for perfect contrast in dark mode
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Khabrify is built to keep you informed with the latest breaking news, curated topics, and global headlines tailored specifically to your preferences.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. LINKS & ACTIONS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                AboutActionRow(Icons.Outlined.StarRate, "Rate us on Play Store") {
                    Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                AboutActionRow(Icons.Outlined.Code, "Open Source Licenses", onNavigateToLicenses)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                AboutActionRow(
                    icon = ImageVector.vectorResource(id = R.drawable.github),
                    title = "View Source Code"
                ) {
                    val websiteUrl = "https://github.com/Prateek-Anand07/Khabrify"
                    val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                    context.startActivity(intent)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                AboutActionRow(
                    icon = ImageVector.vectorResource(id = R.drawable.linkedin),
                    title = "Developer Profile"
                ) {
                    val linkedinUrl = "https://www.linkedin.com/in/prateek-anand-780a3428a/"
                    val intent = Intent(Intent.ACTION_VIEW, linkedinUrl.toUri())
                    context.startActivity(intent)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                AboutActionRow(Icons.Outlined.Email, "Contact Developer") {
                    val emailAddress = "khabrify@gmail.com"
                    val subject = "[Feedback/Bug] Khabrify - [Enter short summary here]"
                    val body = """
        Hi Support Team,
        
        [Please describe your issue or suggestion here]
        
        ------------------------------
        CATEGORY: [] Bug Report  [] Suggestion/Feedback
        ------------------------------
        
        Additional Details:
        - App Version: 1.0.0
        - Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
        - Android Version: ${android.os.Build.VERSION.RELEASE}
        ------------------------------
        
        Thanks & Regards
        [Your Name]
    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:".toUri() // Only email apps should handle this
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                    }

                    try {
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "No email app installed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Made with ❤️ in India", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AboutActionRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FIX: Changed tint from 'primary' to 'onSurface' so the icons are visible in dark mode
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}