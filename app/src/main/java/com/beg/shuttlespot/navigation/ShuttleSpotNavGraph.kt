package com.beg.shuttlespot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen
import com.beg.shuttlespot.feature.foundation.DriverActiveTripScreen
import com.beg.shuttlespot.feature.foundation.DriverHomeScreen
import com.beg.shuttlespot.feature.foundation.RoleSelectionScreen
import com.beg.shuttlespot.feature.passenger.route.RouteSelectionScreen
import com.beg.shuttlespot.feature.foundation.SignInScreen
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.passenger.livetrip.LiveTripDemoScreen
import com.beg.shuttlespot.feature.passenger.boarding.BoardingIntentScreen
import com.beg.shuttlespot.feature.passenger.home.PassengerHomeScreen
import com.beg.shuttlespot.feature.passenger.location.LocationAssistanceScreen

@Composable
fun ShuttleSpotNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppDestination.Welcome.route) {
        composable(AppDestination.Welcome.route) { PlaceholderScreen("ShuttleSpot", "Pelacak shuttle kampus UMN.", "Masuk") { navController.navigate(AppDestination.RoleSelection.route) } }
        composable(AppDestination.RoleSelection.route) { RoleSelectionScreen({ navController.navigate(AppDestination.PassengerSignIn.route) }, { navController.navigate(AppDestination.DriverSignIn.route) }) }
        composable(AppDestination.PassengerSignIn.route) { SignInScreen(isDriver = false, onSuccess = { navController.navigate(AppDestination.LocationPermission.route) }) }
        composable(AppDestination.DriverSignIn.route) { SignInScreen(isDriver = true, onSuccess = { navController.navigate(AppDestination.DriverHome.route) }) }
        composable(AppDestination.LocationPermission.route) { RequireRole(UserRole.PASSENGER, navController) {
            LocationAssistanceScreen(
                onLocationGranted = { navController.navigate(AppDestination.RouteSelection.route) },
                onChooseManually = { navController.navigate(AppDestination.RouteSelection.route) }
            )
        } }
        composable(AppDestination.RouteSelection.route) { RequireRole(UserRole.PASSENGER, navController) { RouteSelectionScreen(onFinished = { navController.navigate(AppDestination.PassengerHomeActive.route) }) } }
        composable(AppDestination.PassengerHomeInactive.route) { PlaceholderScreen("Tidak ada bus aktif", "Jadwal berikutnya tetap tersedia dari cache.", "Atur rute") { navController.navigate(AppDestination.RouteSelection.route) } }
        composable(AppDestination.PassengerHomeActive.route) { RequireRole(UserRole.PASSENGER, navController) {
            PassengerHomeScreen(
                onLiveTrip = { navController.navigate(AppDestination.LiveTrip.route) },
                onBoardingIntent = { navController.navigate(AppDestination.BoardingIntent.route) },
                onRoutes = { navController.navigate(AppDestination.RouteSelection.route) },
                onAnnouncements = { navController.navigate(AppDestination.Announcements.route) },
                onNotifications = { navController.navigate(AppDestination.Notifications.route) },
                onProfile = {}
            )
        } }
        composable(AppDestination.LiveTrip.route) { RequireRole(UserRole.PASSENGER, navController) {
            LiveTripDemoScreen(onBoardingIntentClick = { navController.navigate(AppDestination.BoardingIntent.route) })
        } }
        composable(AppDestination.BoardingIntent.route) { RequireRole(UserRole.PASSENGER, navController) {
            BoardingIntentScreen(onDone = { navController.popBackStack() })
        } }
        composable(AppDestination.Announcements.route) { PlaceholderScreen("Pengumuman", "Pengumuman rute akan menggunakan cache dan Supabase Realtime.") }
        composable(AppDestination.Notifications.route) { PlaceholderScreen("Notifikasi", "Alert personal akan membuka detail perjalanan terkait.") }
        composable(AppDestination.DriverHome.route) { RequireRole(UserRole.DRIVER, navController) { DriverHomeScreen(onActiveTrip = { navController.navigate(AppDestination.DriverTrip.route) }) } }
        composable(AppDestination.DriverTrip.route) { RequireRole(UserRole.DRIVER, navController) { DriverActiveTripScreen(onFinished = { navController.navigate(AppDestination.DriverHome.route) }) } }
    }
}

@Composable
private fun RequireRole(role: UserRole, navController: NavHostController, viewModel: SessionViewModel = hiltViewModel(), content: @Composable () -> Unit) {
    val state by viewModel.state.collectAsState()
    when (state) {
        SessionUiState.Loading -> Unit
        is SessionUiState.Ready -> {
            val session = (state as SessionUiState.Ready).session
            LaunchedEffect(session?.role) {
                if (session?.role != role) navController.navigate(AppDestination.Welcome.route) { popUpTo(0) }
            }
            if (session?.role == role) content()
        }
    }
}
