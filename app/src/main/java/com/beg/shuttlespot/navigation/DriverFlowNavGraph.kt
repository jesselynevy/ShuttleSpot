package com.beg.shuttlespot.navigation

// Overview: Defines driver flow nav graph for the shuttlespot navigation area of ShuttleSpot.

// Dependencies used by this file.
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen
import com.beg.shuttlespot.feature.driver.trip.DriverActiveTripScreen

// Defines the driver flow route type.
object DriverFlowRoute {
    const val ACTIVE_TRIP = "driver/active_trip"
    const val TRIP_COMPLETE = "driver/trip_complete"
}

/** Layar driver layar penuh (di luar shell): plan.md layar 22-24. */
fun NavGraphBuilder.driverFlowNavGraph(navController: NavHostController) {
    composable(DriverFlowRoute.ACTIVE_TRIP) {
        RequireRole(UserRole.DRIVER, navController) {
            DriverActiveTripScreen(
                onFinished = {
                    navController.navigate(DriverFlowRoute.TRIP_COMPLETE) {
                        popUpTo(DriverFlowRoute.ACTIVE_TRIP) { inclusive = true }
                    }
                },
            )
        }
    }

    composable(DriverFlowRoute.TRIP_COMPLETE) {
        RequireRole(UserRole.DRIVER, navController) {
            PlaceholderScreen(
                "Perjalanan selesai",
                "Ringkasan rute, kendaraan, dan nomor trip akan tampil di sini.",
                "Kembali ke beranda",
            ) {
                navController.popBackStack(MainRoute.SHELL, inclusive = false)
            }
        }
    }
}
