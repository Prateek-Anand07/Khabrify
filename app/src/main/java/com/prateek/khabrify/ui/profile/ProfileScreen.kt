package com.prateek.khabrify.ui.profile

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import coil3.compose.SubcomposeAsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.messaging
import com.prateek.khabrify.ui.explore.SelectionDialog
import com.prateek.khabrify.ui.theme.AppTheme
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.jar.Manifest

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    savedArticlesCount: Int,
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onAboutClick: () -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUser = auth.currentUser
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Dialog state variables
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // State to hold our fetched user data
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("") }
    var articlesReadCount by remember { mutableIntStateOf(0) }
    var userPicUrl by remember { mutableStateOf("") }

    // Preferences States
    var userCountry by remember { mutableStateOf("in") }
    var userLanguage by remember { mutableStateOf("en") }

    // --- NOTIFICATION STATE ---
    var isSystemEnabled by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(false) }

    // The visual state of the toggle (Only ON if BOTH system allows it AND user wants it)
    val actualNotificationToggleState = isSystemEnabled && isNotificationsEnabled

    // Country & Language display name maps
    val countries = mapOf(
        "ar" to "Argentina", "au" to "Australia", "at" to "Austria", "bd" to "Bangladesh",
        "be" to "Belgium", "bw" to "Botswana", "br" to "Brazil", "bg" to "Bulgaria",
        "ca" to "Canada", "cl" to "Chile", "cn" to "China", "co" to "Colombia",
        "cu" to "Cuba", "cz" to "Czechia", "eg" to "Egypt", "ee" to "Estonia",
        "et" to "Ethiopia", "fi" to "Finland", "fr" to "France", "de" to "Germany",
        "gh" to "Ghana", "gr" to "Greece", "hk" to "Hong Kong", "hu" to "Hungary",
        "in" to "India", "id" to "Indonesia", "ie" to "Ireland", "il" to "Israel",
        "it" to "Italy", "jp" to "Japan", "ke" to "Kenya", "lv" to "Latvia",
        "lb" to "Lebanon", "lt" to "Lithuania", "my" to "Malaysia", "mx" to "Mexico",
        "ma" to "Morocco", "na" to "Namibia", "nl" to "Netherlands", "nz" to "New Zealand",
        "ng" to "Nigeria", "no" to "Norway", "pk" to "Pakistan", "pe" to "Peru",
        "ph" to "Philippines", "pl" to "Poland", "pt" to "Portugal", "ro" to "Romania",
        "ru" to "Russia", "sa" to "Saudi Arabia", "sn" to "Senegal", "sg" to "Singapore",
        "sk" to "Slovakia", "si" to "Slovenia", "za" to "South Africa", "kr" to "South Korea",
        "es" to "Spain", "se" to "Sweden", "ch" to "Switzerland", "tw" to "Taiwan",
        "tz" to "Tanzania", "th" to "Thailand", "tr" to "Turkey", "ug" to "Uganda",
        "ua" to "Ukraine", "ae" to "United Arab Emirates", "gb" to "United Kingdom",
        "us" to "United States", "ve" to "Venezuela", "vn" to "Vietnam", "zw" to "Zimbabwe"
    )
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

    // Helper to update Firestore
    fun updatePreference(field: String, value: Any) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .update(field, value)
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to update: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Permission Launcher for Android 13+ (POST_NOTIFICATIONS)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isSystemEnabled = true
            isNotificationsEnabled = true
            updatePreference("notificationsEnabled", true)
            Firebase.messaging.subscribeToTopic("news")
            Toast.makeText(context, "Subscribed to breaking news!", Toast.LENGTH_SHORT).show()
        } else {
            isSystemEnabled = false
            Toast.makeText(context, "Permission denied. Enable in system settings.", Toast.LENGTH_LONG).show()
        }
    }

    // Observe System Notification Permission on Resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Checks if OS level notifications are enabled (Works for all Android versions)
                isSystemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isSystemDark = isSystemInDarkTheme()
    // Determine if the UI switch should appear "ON" or "OFF"
    val isDarkThemeEnabled = when (currentTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemDark // If set to system, match the system state
    }

    // Fetch data from Firestore and listen for real-time updates
    DisposableEffect(currentUser?.uid) {
        var listener: com.google.firebase.firestore.ListenerRegistration? = null

        if (currentUser != null) {
            listener = db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        userName = snapshot.getString("name") ?: "User"
                        userEmail = snapshot.getString("email") ?: ""
                        articlesReadCount = snapshot.getLong("articlesRead")?.toInt() ?: 0
                        userPicUrl = snapshot.getString("profilePicUrl") ?: ""

                        userCountry = snapshot.getString("country") ?: "in"
                        userLanguage = snapshot.getString("language") ?: "en"

                        // ADD THIS LINE: Read the saved toggle state (default to false if not set yet)
                        isNotificationsEnabled = snapshot.getBoolean("notificationsEnabled") ?: false
                    }
                }
        }
        onDispose { listener?.remove() }
    }

    // --- DIALOG SYSTEMS ---

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    auth.signOut()
                    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                        com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                    ).build()
                    val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                    googleSignInClient.signOut()

                    onLogoutClick()
                    showLogoutDialog = false
                }) { Text("Log Out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = { Text("Send a password recovery link to $userEmail?") },
            confirmButton = {
                TextButton(onClick = {
                    if (userEmail.isNotEmpty()) {
                        auth.sendPasswordResetEmail(userEmail)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Recovery email sent successfully!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, task.exception?.localizedMessage ?: "Failed to send email", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                    showResetPasswordDialog = false
                }) { Text("Send Link", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { showResetPasswordDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCountryDialog) {
        SelectionDialog(
            title = "Select News Region",
            items = countries,
            onDismiss = { showCountryDialog = false },
            onItemSelected = { selectedCode ->
                userCountry = selectedCode
                updatePreference("country", selectedCode)
                showCountryDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        SelectionDialog(
            title = "Select Preferred Language",
            items = languages,
            onDismiss = { showLanguageDialog = false },
            onItemSelected = { selectedCode ->
                userLanguage = selectedCode
                updatePreference("language", selectedCode)
                showLanguageDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            // Using theme background instead of Color(0xFFF8F9FA)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. HEADER SECTION ---
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Swapped LightGray
                contentAlignment = Alignment.Center
            ) {
                if (userPicUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = userPicUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Profile Icon",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile Icon",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = userEmail,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) // Subdued text
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Edit Profile Details"
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 2. STATS ROW ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Outlined.Article,
                    count = articlesReadCount.toString(),
                    label = "Articles Read"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.BookmarkBorder,
                    count = savedArticlesCount.toString(),
                    label = "Saved Items"
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 3. ACCOUNT SETTINGS ---
        item {
            SectionTitle("Account Settings")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    SettingsActionRow(
                        icon = Icons.Outlined.Public,
                        title = "Country",
                        subtitle = countries[userCountry] ?: "Default (India)",
                        onClick = { showCountryDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    SettingsActionRow(
                        icon = Icons.Outlined.Translate,
                        title = "Language",
                        subtitle = languages[userLanguage] ?: "Default (English)",
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    SettingsActionRow(
                        icon = Icons.Outlined.LockReset,
                        title = "Reset Password",
                        subtitle = "Send password reset instructions email",
                        onClick = { showResetPasswordDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    SettingsToggleRow(
                        icon = Icons.Outlined.NotificationsActive,
                        title = "Push Notifications",
                        subtitle = if (!isSystemEnabled) "Blocked in system settings" else "Breaking news and updates",
                        isChecked = actualNotificationToggleState, // Use combined state
                        onCheckedChange = { isChecking ->
                            if (isChecking) {
                                // User wants to turn ON
                                if (isSystemEnabled) {
                                    isNotificationsEnabled = true
                                    updatePreference("notificationsEnabled", true)
                                    Firebase.messaging.subscribeToTopic("news")
                                    Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
                                } else {
                                    // System is disabled, ask for permission or go to settings
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        // Open App Settings for pre-Android 13
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        context.startActivity(intent)
                                        Toast.makeText(context, "Please enable notifications in settings", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                // User wants to turn OFF
                                isNotificationsEnabled = false
                                updatePreference("notificationsEnabled", false)
                                Firebase.messaging.unsubscribeFromTopic("news")
                                Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    SettingsToggleRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Theme",
                        subtitle = when (currentTheme) {
                            AppTheme.SYSTEM -> "Following system setting"
                            else -> "Reduce glare and eye strain"
                        },
                        isChecked = isDarkThemeEnabled,
                        onCheckedChange = { checked ->
                            // This permanently overrides the system setting and saves it via the callback
                            if (checked) {
                                onThemeChange(AppTheme.DARK)
                            } else {
                                onThemeChange(AppTheme.LIGHT)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 4. SUPPORT & ABOUT ---
        item {
            SectionTitle("Support & About")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    SettingsActionRow(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = "Help Center",
                        onClick = onHelpCenterClick
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    SettingsActionRow(
                        icon = Icons.Outlined.Info,
                        title = "About Khabrify",
                        onClick = onAboutClick
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- 5. LOGOUT BUTTON ---
        item {
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Version 1.0.0",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- REUSABLE UI COMPONENTS ---

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, count: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // CHANGE: color from primary to onSurface
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsToggleRow(icon: ImageVector, title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}