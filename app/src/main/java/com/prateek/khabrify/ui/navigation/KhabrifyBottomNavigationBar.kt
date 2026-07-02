package com.prateek.khabrify.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.prateek.khabrify.ui.theme.KhabrifyOffWhite
import com.prateek.khabrify.ui.theme.KhabrifyRed
import com.prateek.khabrify.ui.theme.LightBackground

@Composable
fun KhabrifyBottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Explore,
        BottomNavItem.Saved,
        BottomNavItem.Profile
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        // Find out which screen we are currently looking at
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Button for each item in list
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) }, // highlights if it matches
                selected = currentRoute==item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Prevent building a massive backstack if user taps the same button multiple times
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Prevent multiple copies of same screen
                        launchSingleTop = true
                        // Remember where the user scrolled when come back
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KhabrifyRed,
                    selectedTextColor = KhabrifyRed,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}