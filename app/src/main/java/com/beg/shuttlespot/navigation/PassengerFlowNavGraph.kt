package com.beg.shuttlespot.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen
import com.beg.shuttlespot.feature.passenger.announcement.AnnouncementScreen
import com.beg.shuttlespot.feature.passenger.livetrip.LiveTripDemoScreen

object PassengerFlowRoute {
    const val LIVE_TRIP = "passenger/live_trip"
    const val NOTIFICATIONS = "passenger/notifications"
    const val ANNOUNCEMENTS  = "passenger/announcements"

    // Disiapkan untuk halaman detail pengumuman. Belum didaftarkan di nav graph.
    const val ARG_ANNOUNCEMENT_ID = "id"
    const val ANNOUNCEMENT_DETAIL = "passenger/announcements/{$ARG_ANNOUNCEMENT_ID}"
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
                // TODO(detail): saat halaman detail ada, daftarkan PassengerFlowRoute.ANNOUNCEMENT_DETAIL
                //  lalu ganti dengan: navController.navigate(PassengerFlowRoute.announcementDetail(id))
                onAnnouncementClick = { _ -> },
            )
        }
    }
}
