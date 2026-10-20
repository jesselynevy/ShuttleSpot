package com.beg.shuttlespot.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.CircularShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.WaveBackground
import com.beg.shuttlespot.core.ui.components.WaveStyle
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
    val style = if(current == MainRoute.PROFILE) WaveStyle.Profile else WaveStyle.Main

    WaveBackground(style) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                val scheme = MaterialTheme.colorScheme
                NavigationBar(containerColor = LocalExtendedColors.current.groupingColor) {
                    tabsFor(role).forEach { spec ->
                        val selected = current == spec.tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { tabNav.navigateToTab(spec.tab.route) },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircularShape)
                                        .background(if (selected) scheme.primary.copy(alpha = 0.1f) else Color.Transparent),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(spec.icon, contentDescription = spec.label)
                                }
                            },
                            label = { Text(spec.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = scheme.primary,
                                selectedTextColor = scheme.primary,
                                unselectedIconColor = scheme.onBackground,
                                unselectedTextColor = scheme.onBackground,
                                indicatorColor = Color.Transparent,
                            ),
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
                composable(MainRoute.NOTIFICATIONS) { NotificationsTab() }
                composable(MainRoute.PROFILE) { ProfileTab(role) }
            }
        }
    }

}
