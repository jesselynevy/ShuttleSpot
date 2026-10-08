package com.beg.shuttlespot.feature.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.navigation.MainRoute
import com.beg.shuttlespot.navigation.navigateToTab
import com.beg.shuttlespot.navigation.tabsFor

/**
 * Shell dengan 4 tab yang sama untuk semua role. Isi tab ditentukan per role di file *Tab.
 * [rootNav] dipakai untuk layar layar-penuh di luar shell; [tabNav] hanya untuk pindah antar tab.
 */
@Composable
fun MainShell(role: UserRole, rootNav: NavHostController) {
    val tabNav = rememberNavController()
    val current = tabNav.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabsFor(role).forEach { spec ->
                    NavigationBarItem(
                        selected = current == spec.tab.route,
                        onClick = { tabNav.navigateToTab(spec.tab.route) },
                        icon = { Icon(spec.icon, contentDescription = spec.label) },
                        label = { Text(spec.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(tabNav, startDestination = MainRoute.HOME, modifier = Modifier.padding(padding)) {
            composable(MainRoute.HOME) { HomeTab(role, tabNav, rootNav) }
            composable(MainRoute.ROUTES) {
                RoutesTab(role, onSelectionSaved = { tabNav.navigateToTab(MainRoute.HOME) })
            }
            composable(MainRoute.ANNOUNCEMENTS) { AnnouncementsTab() }
            composable(MainRoute.PROFILE) { ProfileTab(role) }
        }
    }
}
