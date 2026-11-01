package com.beg.shuttlespot.feature.shell

// Overview: Defines home tab for the feature shell area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.driver.trip.DriverHomeScreen
import com.beg.shuttlespot.feature.passenger.home.PassengerHomeScreen
import com.beg.shuttlespot.navigation.DriverFlowRoute
import com.beg.shuttlespot.navigation.MainRoute
import com.beg.shuttlespot.navigation.PassengerFlowRoute
// Dependencies used by this file.
import com.beg.shuttlespot.navigation.navigateToTab

/** Tab Beranda. Pindah tab lewat [tabNav]; layar layar-penuh lewat [rootNav]. */
@Composable
// Renders the home tab UI section.
fun HomeTab(role: UserRole, tabNav: NavController, rootNav: NavController) {
    // Handles the current state branch.
    when (role) {
        UserRole.PASSENGER -> PassengerHomeScreen(
            onLiveTrip = { rootNav.navigate(PassengerFlowRoute.LIVE_TRIP) },
            onRoutes = { tabNav.navigateToTab(MainRoute.ROUTES) },
            onAnnouncements = { rootNav.navigate(PassengerFlowRoute.ANNOUNCEMENTS) },
            onNotifications = { rootNav.navigate(PassengerFlowRoute.NOTIFICATIONS) },
            onProfile = { tabNav.navigateToTab(MainRoute.PROFILE) },
        )
        UserRole.DRIVER -> DriverHomeScreen(
            onActiveTrip = { rootNav.navigate(DriverFlowRoute.ACTIVE_TRIP) },
        )
    }
}
