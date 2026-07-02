package com.prateek.khabrify.ui.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prateek.khabrify.ui.theme.KhabrifyNavy
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.URLEncoder
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    // Predefined list of beautiful avatar background colors (Hex strings without #)
    val avatarColors = listOf("0D1B2A", "1B4332", "5E503F", "4A154B", "0077B6", "D90429", "3D348B")

    // State Variables
    var name by remember { mutableStateOf("") }
    var originalName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Track avatar color state independently
    var selectedColor by remember { mutableStateOf("0D1B2A") }
    var originalColor by remember { mutableStateOf("0D1B2A") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Fetch current user data when screen loads
    LaunchedEffect(user?.uid) {
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val fetchedName = document.getString("name") ?: ""
                    name = fetchedName
                    originalName = fetchedName
                    email = document.getString("email") ?: ""

                    // Fetch saved color preference, default to first item if empty
                    val fetchedColor = document.getString("avatarColor") ?: "0D1B2A"
                    selectedColor = fetchedColor
                    originalColor = fetchedColor

                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // --- DISCARD WARNING LOGIC ---
    val hasChanges = (name.trim() != originalName) || (selectedColor != originalColor)

    val handleBackPress = {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler { handleBackPress() }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?", fontWeight = FontWeight.Bold) },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack()
                }) {
                    Text("Discard", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel", color = KhabrifyNavy)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KhabrifyNavy)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Custom Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { handleBackPress() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Text(
                        text = "Edit Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- AVATAR PREVIEW WITH DYNAMIC COLOR ---
                val displayUrl = if (name.isNotBlank()) {
                    val encoded = URLEncoder.encode(name.trim(), "UTF-8")
                    // Injecting the live state variable 'selectedColor' into the API call
                    "https://ui-avatars.com/api/?name=$encoded&background=$selectedColor&color=fff&size=250"
                } else {
                    ""
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable {
                            // Cycle to the next color in the array upon tap
                            val currentIndex = avatarColors.indexOf(selectedColor)
                            val nextIndex = (currentIndex + 1) % avatarColors.size
                            selectedColor = avatarColors[nextIndex]
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (displayUrl.isNotEmpty()) {
                        AsyncImage(
                            model = displayUrl,
                            contentDescription = "Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Icon",
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap avatar to change color",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // --- INPUT FIELDS ---
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email (Cannot be changed)") },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(48.dp))

                // --- SAVE BUTTON WITH UPDATED STATE VERIFICATION ---
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Save button executes if name OR color has been altered
                        if (!hasChanges) {
                            Toast.makeText(context, "No changes to save", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                            return@Button
                        }

                        isSaving = true
                        coroutineScope.launch {
                            try {
                                val uid = user?.uid ?: return@launch

                                val encoded = URLEncoder.encode(name.trim(), "UTF-8")
                                val finalPicUrl = "https://ui-avatars.com/api/?name=$encoded&background=$selectedColor&color=fff&size=250"

                                // Storing both the full image link string and the standalone color preference
                                val updates = mapOf(
                                    "name" to name.trim(),
                                    "profilePicUrl" to finalPicUrl,
                                    "avatarColor" to selectedColor
                                )

                                // Set a 5-second timeout. If offline, this throws a TimeoutCancellationException
                                withTimeout(5000L.milliseconds) {
                                    db.collection("users").document(uid).update(updates).await()
                                }

                                isSaving = false
                                Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()

                            } catch (e: TimeoutCancellationException) {
                                // Firestore already saved it locally and will sync to the cloud automatically later!
                                isSaving = false
                                Toast.makeText(context, "Saved offline! Will sync when internet is available.", Toast.LENGTH_LONG).show()
                                onNavigateBack()
                            } catch (e: Exception) {
                                isSaving = false
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KhabrifyNavy),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Profile", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}