package com.beg.shuttlespot.navigation

// Overview: Defines passenger flow nav graph for the shuttlespot navigation area of ShuttleSpot.

// Dependencies used by this file.
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen
import com.beg.shuttlespot.feature.passenger.announcement.AnnouncementScreen
// Dependencies used by this file.
import com.beg.shuttlespot.feature.passenger.announcement.AnnouncementDetailScreen
import com.beg.shuttlespot.feature.passenger.livetrip.LiveTripDemoScreen

// Defines the passenger flow route type.
object PassengerFlowRoute {
    const val LIVE_TRIP = "passenger/live_trip"
    const val NOTIFICATIONS = "passenger/notifications"
    const val ANNOUNCEMENTS  = "passenger/announcements"

    // Disiapkan untuk halaman detail pengumuman. Belum didaftarkan di nav graph.
    const val ARG_ANNOUNCEMENT_ID = "id"
    const val ANNOUNCEMENT_DETAIL = "passenger/announcements/{$ARG_ANNOUNCEMENT_ID}"
    // Handles the announcement detail responsibility.
    fun announcementDetail(id: String) = "passenger/announcements/$id"
}

/** Layar penumpang layar penuh (di luar shell): plan.md layar 13, 14, 17. */
fun NavGraphBuilder.passengerFlowNavGraph(navController: NavHostController) {
    composable(PassengerFlowRoute.LIVE_TRIP) {
        RequireRole(UserRole.PASSENGER, navController) {
            LiveTripDemoScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }

    composable(PassengerFlowRoute.NOTIFICATIONS) {
        RequireRole(UserRole.PASSENGER, navController) {
            PlaceholderScreen("Notifikasi", "Alert personal akan membuka detail perjalanan terkait.")
        }
    }

    composable(PassengerFlowRoute.ANNOUNCEMENTS) {
        RequireRole(UserRole.PASSENGER, navController) {
            AnnouncementScreen(
                onBack = { navController.popBackStack() },
                onAnnouncementClick = { id -> navController.navigate(PassengerFlowRoute.announcementDetail(id)) },
            )
        }
    }

    composable(
        PassengerFlowRoute.ANNOUNCEMENT_DETAIL,
        arguments = listOf(navArgument(PassengerFlowRoute.ARG_ANNOUNCEMENT_ID) { type = NavType.StringType }),
    ) { entry ->
        RequireRole(UserRole.PASSENGER, navController) {
            AnnouncementDetailScreen(
                id = entry.arguments?.getString(PassengerFlowRoute.ARG_ANNOUNCEMENT_ID).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
