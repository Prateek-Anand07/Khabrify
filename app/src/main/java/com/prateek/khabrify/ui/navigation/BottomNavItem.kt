package com.prateek.khabrify.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Explore : BottomNavItem("explore", "Explore", Icons.Filled.Explore)
    object Saved : BottomNavItem("saved", "Saved", Icons.Filled.BookmarkBorder)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.PersonOutline)
}