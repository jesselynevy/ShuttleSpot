package com.beg.shuttlespot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen
import com.beg.shuttlespot.core.ui.pages.HomeTemp
import com.beg.shuttlespot.feature.passenger.livetrip.LiveTripDemoScreen

@Composable
fun ShuttleSpotNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Welcome.route
    ) {
        composable(AppDestination.Welcome.route) {
            PlaceholderScreen(
                title = "ShuttleSpot",
                description = "Track UMN campus shuttles with clearer, timely information.",
                buttonText = "Get Started",
                onButtonClick = {
                    navController.navigate(AppDestination.SignIn.route)
                }
            )
        }

        composable(AppDestination.SignIn.route) {
            PlaceholderScreen(
                title = "Sign In",
                description = "Passenger, Driver, or Admin authentication.",
                buttonText = "Continue as Passenger",
                onButtonClick = {
                    navController.navigate(AppDestination.LocationPermission.route)
                }
            )
        }

        composable(AppDestination.LocationPermission.route) {
            PlaceholderScreen(
                title = "Nearby Stops",
                description = "Location is optional and helps find nearby shuttle stops.",
                buttonText = "Choose Route",
                onButtonClick = {
                    navController.navigate(AppDestination.RouteSelection.route)
                }
            )
        }

        composable(AppDestination.RouteSelection.route) {
            PlaceholderScreen(
                title = "Choose Route and Stop",
                description = "Select your primary shuttle route and pickup stop.",
                buttonText = "View Home",
                onButtonClick = {
                    navController.navigate(AppDestination.PassengerHomeInactive.route)
                }
            )
        }

        composable(AppDestination.PassengerHomeInactive.route) {
            HomeTemp(
                title = "No Active Bus",
                description = "The next shuttle schedule will appear here.",
                buttonText = "Show Demo Active Bus",
                onButtonClick = {
                    navController.navigate(AppDestination.PassengerHomeActive.route)
                }
            )
        }

        composable(AppDestination.PassengerHomeActive.route) {
            PlaceholderScreen(
                title = "Bus is Approaching",
                description = "Route BSD • Estimated arrival: 5 minutes.",
                buttonText = "View Live Trip",
                onButtonClick = {
                    navController.navigate(AppDestination.LiveTrip.route)
                }
            )
        }

        composable(AppDestination.LiveTrip.route) {
            LiveTripDemoScreen(
                onBoardingIntentClick = {
                    navController.navigate(AppDestination.BoardingIntent.route)
                }
            )
        }
        composable(AppDestination.BoardingIntent.route) {
            PlaceholderScreen(
                title = "Boarding Intent",
                description = "You are marked as waiting at Halte APCD.",
                buttonText = "Read Announcements",
                onButtonClick = {
                    navController.navigate(AppDestination.Announcements.route)
                }
            )
        }

        composable(AppDestination.Announcements.route) {
            PlaceholderScreen(
                title = "Announcements",
                description = "Service updates from drivers and UMN operations staff.",
                buttonText = "Open Notifications",
                onButtonClick = {
                    navController.navigate(AppDestination.Notifications.route)
                }
            )
        }

        composable(AppDestination.Notifications.route) {
            PlaceholderScreen(
                title = "Notifications",
                description = "Arrival alerts and shuttle service updates.",
                buttonText = "Driver Demo",
                onButtonClick = {
                    navController.navigate(AppDestination.DriverHome.route)
                }
            )
        }

        composable(AppDestination.DriverHome.route) {
            PlaceholderScreen(
                title = "Driver Dashboard",
                description = "Your assigned trip is ready to start.",
                buttonText = "Start Trip",
                onButtonClick = {
                    navController.navigate(AppDestination.DriverTrip.route)
                }
            )
        }

        composable(AppDestination.DriverTrip.route) {
            PlaceholderScreen(
                title = "Active Driver Trip",
                description = "Next stop, passenger demand, route progress, and trip controls."
            )
        }
    }
}