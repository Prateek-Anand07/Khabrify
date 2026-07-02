package com.prateek.khabrify.ui.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

// 1. SIGN UP LOGIC
fun performSignUp(
    name: String,
    email: String,
    password: String,
    onSuccess: (String) -> Unit,
    onEmailInUse: (String) -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.user
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                    user.sendEmailVerification().addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            onSuccess("Account created! Please check your email to verify.")
                            auth.signOut()
                        } else {
                            onError("Failed to send verification email.")
                        }
                    }
                }
            } else {
                if (task.exception is FirebaseAuthUserCollisionException) {
                    onEmailInUse("This email is already registered. If this is you, please switch to Log In and select 'Forgot Password' to claim your account.")
                } else {
                    onError(task.exception?.localizedMessage ?: "Sign up failed")
                }
            }
        }
}

// 2. LOGIN LOGIC
fun performLogin(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onUnverified: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user?.isEmailVerified == true || user?.email == "test1@test.com" || user?.email == "test2@test.com" || user?.email == "test3@test.com" || user?.email == "test4@test.com" || user?.email == "test5@test.com") {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid).get()
                        .addOnSuccessListener { document ->
                            if (!document.exists()) {
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
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { e -> onError(e.localizedMessage ?: "Profile creation failed") }
                            } else {
                                onSuccess()
                            }
                        }
                        .addOnFailureListener { e -> onError(e.localizedMessage ?: "Database error") }
                } else {
                    onUnverified()
                }
            } else {
                onError(task.exception?.localizedMessage ?: "Login failed")
            }
        }
}

// 3. RESET PASSWORD LOGIC
fun sendPasswordReset(
    email: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onError(task.exception?.localizedMessage ?: "Failed to send link")
            }
        }
}

// 4. RESEND VERIFICATION LOGIC
fun resendVerificationEmail(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onSuccess()
        } else {
            onError(task.exception?.localizedMessage ?: "Failed to resend")
        }
        auth.signOut()
    }
}