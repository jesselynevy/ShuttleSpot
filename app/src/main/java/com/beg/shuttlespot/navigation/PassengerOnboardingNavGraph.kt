package com.beg.shuttlespot.navigation

// Overview: Defines passenger onboarding nav graph for the shuttlespot navigation area of ShuttleSpot.

// Dependencies used by this file.
import android.os.Build
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.passenger.location.LocationAssistanceScreen
import com.beg.shuttlespot.feature.passenger.notification.NotificationPermissionScreen
// Dependencies used by this file.
import com.beg.shuttlespot.feature.passenger.route.RouteSelectionScreen

// Defines the onboarding route type.
object OnboardingRoute {
    const val GRAPH = "onboarding"

    const val LOCATION = "onboarding/location"
    const val NOTIFICATION = "onboarding/notification"
    const val ROUTE_SELECTION = "onboarding/route_selection"
}

/**
 * Onboarding penumpang: izin notifikasi -> Location Assistance -> pilih rute/halte -> review -> selesai.
 * Izin notifikasi dilewati di Android < 13 karena POST_NOTIFICATIONS tidak tersedia.
 * RouteSelectionScreen saat ini sudah memuat pilih rute, halte, review, dan layar "tersimpan".
 * Kalau nanti dipecah (Nearby Stops, Manual, Route Results, Route Detail), tambahkan destinasi di sini.
 */
// Handles the nav graph builder responsibility.
fun NavGraphBuilder.passengerOnboardingNavGraph(
    navController: NavHostController,
    onCompleted: () -> Unit,
) {
    navigation(
        startDestination = firstOnboardingDestination(),
        route = OnboardingRoute.GRAPH,
    ) {
        composable(OnboardingRoute.LOCATION) {
            RequireRole(UserRole.PASSENGER, navController) {
                LocationAssistanceScreen(
                    onLocationGranted = { navController.navigate(OnboardingRoute.ROUTE_SELECTION) },
                    onChooseManually = { navController.navigate(OnboardingRoute.ROUTE_SELECTION) },
                )
            }
        }

        composable(OnboardingRoute.NOTIFICATION) {
            RequireRole(UserRole.PASSENGER, navController) {
                NotificationPermissionScreen(
                    onContinue = { navController.navigate(OnboardingRoute.LOCATION) },
                )
            }
        }

        composable(OnboardingRoute.ROUTE_SELECTION) {
            RequireRole(UserRole.PASSENGER, navController) {
                RouteSelectionScreen(onFinished = onCompleted, showOnboardingProgress = true, onBack = { navController.popBackStack() })
            }
        }
    }
}

/** POST_NOTIFICATIONS baru ada di Android 13 (TIRAMISU); di bawah itu mulai dari izin lokasi. */
private fun firstOnboardingDestination(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) OnboardingRoute.NOTIFICATION
    else OnboardingRoute.LOCATION
