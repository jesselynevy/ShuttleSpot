package com.beg.shuttlespot.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen
import com.beg.shuttlespot.feature.passenger.boarding.BoardingIntentScreen
import com.beg.shuttlespot.feature.passenger.livetrip.LiveTripDemoScreen

object PassengerFlowRoute {
    const val LIVE_TRIP = "passenger/live_trip"
    const val BOARDING_INTENT = "passenger/boarding_intent"
    const val NOTIFICATIONS = "passenger/notifications"
}

/** Layar penumpang layar penuh (di luar shell): plan.md layar 13, 14, 17. */
fun NavGraphBuilder.passengerFlowNavGraph(navController: NavHostController) {
    composable(PassengerFlowRoute.LIVE_TRIP) {
        RequireRole(UserRole.PASSENGER, navController) {
            LiveTripDemoScreen(
                onBoardingIntentClick = { navController.navigate(PassengerFlowRoute.BOARDING_INTENT) },
            )
        }
    }

    composable(PassengerFlowRoute.BOARDING_INTENT) {
        RequireRole(UserRole.PASSENGER, navController) {
            BoardingIntentScreen(onDone = { navController.popBackStack() })
        }
    }

    composable(PassengerFlowRoute.NOTIFICATIONS) {
        RequireRole(UserRole.PASSENGER, navController) {
            PlaceholderScreen("Notifikasi", "Alert personal akan membuka detail perjalanan terkait.")
        }
    }
}
