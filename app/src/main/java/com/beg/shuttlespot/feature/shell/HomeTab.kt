package com.beg.shuttlespot.feature.shell

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.foundation.DriverHomeScreen
import com.beg.shuttlespot.feature.passenger.home.PassengerHomeScreen
import com.beg.shuttlespot.navigation.DriverFlowRoute
import com.beg.shuttlespot.navigation.MainRoute
import com.beg.shuttlespot.navigation.PassengerFlowRoute
import com.beg.shuttlespot.navigation.navigateToTab

/** Tab Beranda. Pindah tab lewat [tabNav]; layar layar-penuh lewat [rootNav]. */
@Composable
fun HomeTab(role: UserRole, tabNav: NavController, rootNav: NavController) {
    when (role) {
        UserRole.PASSENGER -> PassengerHomeScreen(
            onLiveTrip = { rootNav.navigate(PassengerFlowRoute.LIVE_TRIP) },
            onBoardingIntent = { rootNav.navigate(PassengerFlowRoute.BOARDING_INTENT) },
            onRoutes = { tabNav.navigateToTab(MainRoute.ROUTES) },
            onAnnouncements = { tabNav.navigateToTab(MainRoute.ANNOUNCEMENTS) },
            onNotifications = { rootNav.navigate(PassengerFlowRoute.NOTIFICATIONS) },
            onProfile = { tabNav.navigateToTab(MainRoute.PROFILE) },
        )
        UserRole.DRIVER -> DriverHomeScreen(
            onActiveTrip = { rootNav.navigate(DriverFlowRoute.ACTIVE_TRIP) },
        )
    }
}
