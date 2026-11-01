package com.beg.shuttlespot.navigation

// Overview: Defines auth nav graph for the shuttlespot navigation area of ShuttleSpot.

// Dependencies used by this file.
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.auth.RoleSelectionScreen
// Dependencies used by this file.
import com.beg.shuttlespot.feature.auth.SignInScreen
import com.beg.shuttlespot.feature.foundation.WelcomeScreen

// Defines the auth route type.
object AuthRoute {
    const val GRAPH = "auth" // parent route

    const val WELCOME = "auth/welcome"
    const val ROLE_SELECTION = "auth/role_selection"
    const val SIGN_IN = "auth/sign_in/{role}"

    const val ARG_ROLE = "role"
    // Handles the sign in responsibility.
    fun signIn(role: UserRole) = "auth/sign_in/${role.name}"
}

// Handles the nav graph builder responsibility.
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onSignedIn: (UserRole) -> Unit,
) {
    navigation(
        startDestination = AuthRoute.WELCOME,
        route = AuthRoute.GRAPH,
    ) {
        composable(AuthRoute.WELCOME) {
            WelcomeScreen {
                navController.navigate(AuthRoute.ROLE_SELECTION)
            }
        }

        composable(AuthRoute.ROLE_SELECTION) {
            RoleSelectionScreen(
                onPassenger = { navController.navigate(AuthRoute.signIn(UserRole.PASSENGER)) },
            )
        }

        composable(
            route = AuthRoute.SIGN_IN,
            arguments = listOf(navArgument(AuthRoute.ARG_ROLE) { type = NavType.StringType }),
        ) { entry ->
            val role = entry.arguments
                ?.getString(AuthRoute.ARG_ROLE)
                ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                ?: UserRole.PASSENGER

            SignInScreen(
                isDriver = role == UserRole.DRIVER,
                onSuccess = { onSignedIn(role) },
                showPassengerOnboardingProgress = role == UserRole.PASSENGER,
            )
        }
    }
}
