package com.beg.shuttlespot.navigation

import android.os.Build
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.passenger.location.LocationAssistanceScreen
import com.beg.shuttlespot.feature.passenger.notification.NotificationPermissionScreen
import com.beg.shuttlespot.feature.passenger.route.RouteSelectionScreen

object OnboardingRoute {
    const val GRAPH = "onboarding"

    const val LOCATION = "onboarding/location"
    const val NOTIFICATION = "onboarding/notification"
    const val ROUTE_SELECTION = "onboarding/route_selection"
}

/**
 * Onboarding penumpang: Location Assistance (plan.md layar 4) -> izin notifikasi -> pilih rute/halte -> review -> selesai (layar 5-10).
 * Izin notifikasi ditambahkan di luar plan.md; layar itu dilewati di Android < 13 karena izinnya tidak ada.
 * RouteSelectionScreen saat ini sudah memuat pilih rute, halte, review, dan layar "tersimpan".
 * Kalau nanti dipecah (Nearby Stops, Manual, Route Results, Route Detail), tambahkan destinasi di sini.
 */
fun NavGraphBuilder.passengerOnboardingNavGraph(
    navController: NavHostController,
    onCompleted: () -> Unit,
) {
    navigation(
        startDestination = OnboardingRoute.LOCATION,
        route = OnboardingRoute.GRAPH,
    ) {
        composable(OnboardingRoute.LOCATION) {
            RequireRole(UserRole.PASSENGER, navController) {
                LocationAssistanceScreen(
                    onLocationGranted = { navController.navigate(nextAfterLocation()) },
                    onChooseManually = { navController.navigate(nextAfterLocation()) },
                )
            }
        }

        composable(OnboardingRoute.NOTIFICATION) {
            RequireRole(UserRole.PASSENGER, navController) {
                NotificationPermissionScreen(
                    onContinue = { navController.navigate(OnboardingRoute.ROUTE_SELECTION) },
                )
            }
        }

        composable(OnboardingRoute.ROUTE_SELECTION) {
            RequireRole(UserRole.PASSENGER, navController) {
                RouteSelectionScreen(onFinished = onCompleted)
            }
        }
    }
}

/** POST_NOTIFICATIONS baru ada di Android 13 (TIRAMISU); di bawah itu langsung ke pemilihan rute. */
private fun nextAfterLocation(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) OnboardingRoute.NOTIFICATION
    else OnboardingRoute.ROUTE_SELECTION
