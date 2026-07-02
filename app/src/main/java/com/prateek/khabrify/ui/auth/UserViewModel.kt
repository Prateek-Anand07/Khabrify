package com.prateek.khabrify.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prateek.khabrify.data.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class UserViewModel @Inject constructor() : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    init {
        // Load from Firestore once and keep it updated
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    _userProfile.value = snapshot?.toObject(UserProfile::class.java)
                }
        }
    }
}