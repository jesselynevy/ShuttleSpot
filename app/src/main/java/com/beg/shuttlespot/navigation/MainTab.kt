package com.beg.shuttlespot.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.beg.shuttlespot.core.model.UserRole

enum class MainTab(val route: String) {
    Home(MainRoute.HOME),
    Routes(MainRoute.ROUTES),
    Notifications(MainRoute.NOTIFICATIONS),
    Profile(MainRoute.PROFILE),
}

data class TabSpec(val tab: MainTab, val label: String, val icon: ImageVector)

private val defaultTabs = listOf(
    TabSpec(MainTab.Home, "Beranda", Icons.Default.Home),
    TabSpec(MainTab.Routes, "Rute", Icons.Default.Map),
    TabSpec(MainTab.Notifications, "Notifikasi", Icons.Default.Notifications),
    TabSpec(MainTab.Profile, "Profil", Icons.Default.Person),
)

fun tabsFor(role: UserRole): List<TabSpec> = when (role) {
    UserRole.PASSENGER, UserRole.DRIVER -> defaultTabs
    // UserRole.ADMIN -> defaultTabs
}

/** Pindah tab dengan opsi standar. Dipakai NavigationBar dan callback di dalam layar tab. */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
