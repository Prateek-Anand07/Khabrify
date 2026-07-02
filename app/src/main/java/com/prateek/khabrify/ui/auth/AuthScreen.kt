package com.prateek.khabrify.ui.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.prateek.khabrify.R

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showResendVerificationDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isResetting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && task.result?.additionalUserInfo?.isNewUser == true) {
                            val db = FirebaseFirestore.getInstance()
                            val userProfile = hashMapOf(
                                "uid" to user.uid,
                                "name" to (user.displayName ?: "User"),
                                "email" to (user.email ?: ""),
                                "articlesRead" to 0,
                                "profilePicUrl" to "",
                                "avatarColor" to "0D1B2A",
                                "country" to "in",
                                "language" to "en",
                                "notificationsEnabled" to false
                            )
                            db.collection("users").document(user.uid).set(userProfile)
                                .addOnCompleteListener { onLoginSuccess() }
                        } else {
                            onLoginSuccess()
                        }
                    } else {
                        errorMessage = "Google sign-in failed."
                    }
                }
        } catch (e: Exception) {
            errorMessage = "Google sign-in canceled or failed."
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { if (!isResetting) showResetDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your email address and we will send you a link to reset your password.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!isValidEmail(resetEmail.trim())) {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        isResetting = true
                        sendPasswordReset(
                            email = resetEmail.trim(),
                            onSuccess = {
                                isResetting = false
                                Toast.makeText(context, "Reset link sent! Check your inbox.", Toast.LENGTH_LONG).show()
                                showResetDialog = false
                            },
                            onError = { error ->
                                isResetting = false
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isResetting
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                    } else {
                        Text("Send Link", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    enabled = !isResetting
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showResendVerificationDialog) {
        AlertDialog(
            onDismissRequest = {
                auth.signOut()
                showResendVerificationDialog = false
            },
            title = { Text("Email Not Verified", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your email hasn't been verified yet. Would you like us to send a new verification link to your email address?", fontSize = 14.sp)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resendVerificationEmail(
                            onSuccess = {
                                Toast.makeText(context, "Verification email resent! Check your inbox.", Toast.LENGTH_LONG).show()
                                showResendVerificationDialog = false
                            },
                            onError = { error ->
                                Toast.makeText(context, "Failed: $error", Toast.LENGTH_LONG).show()
                                showResendVerificationDialog = false
                            }
                        )
                    }
                ) {
                    Text("Resend Link", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        auth.signOut()
                        showResendVerificationDialog = false
                    }
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Fixes root background color
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.k_icon_bg),
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Khabrify",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (isSignUp) "Create an account to save news" else "Welcome back! Please sign in",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), // Adapts to theme
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Show Error Message if one exists
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (isSignUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (!isSignUp) {
            TextButton(
                onClick = {
                    resetEmail = email
                    showResetDialog = true
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Login / Signup Button
        Button(
            onClick = {
                // ... validation logic stays exactly the same ...
                if (isSignUp && name.isBlank()) {
                    errorMessage = "Please enter your name"
                    return@Button
                }
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password cannot be empty"
                    return@Button
                }
                if (!isValidEmail(email.trim())) {
                    errorMessage = "Please enter a valid email address"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                if (isSignUp) {
                    performSignUp(
                        name = name.trim(),
                        email = email.trim(),
                        password = password,
                        onSuccess = { successMessage ->
                            isLoading = false
                            errorMessage = successMessage
                        },
                        onEmailInUse = { collisionMessage ->
                            isLoading = false
                            errorMessage = collisionMessage
                            isSignUp = false
                        },
                        onError = { errorMsg ->
                            isLoading = false
                            errorMessage = errorMsg
                        }
                    )
                } else {
                    performLogin(
                        email = email.trim(),
                        password = password,
                        onSuccess = {
                            isLoading = false
                            onLoginSuccess()
                        },
                        onUnverified = {
                            isLoading = false
                            showResendVerificationDialog = true
                        },
                        onError = { errorMsg ->
                            isLoading = false
                            errorMessage = errorMsg
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary // Auto white/dark text
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = if (isSignUp) "Sign Up" else "Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                isSignUp = !isSignUp
                errorMessage = null
            },
            enabled = !isLoading
        ) {
            Text(
                text = if (isSignUp) "Already have an account? Log in" else "Don't have an account? Sign up",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // THE NEW GOOGLE SIGN IN BUTTON
        OutlinedButton(
            onClick = {
                val client = getGoogleSignInClient(context)
                launcher.launch(client.signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            enabled = !isLoading
        ) {
            // Note: Replace R.drawable.ic_google_logo with your actual G-logo drawable ID
            // If you don't have one, just remove the Image and Spacer for now.
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}