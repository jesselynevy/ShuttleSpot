package com.beg.shuttlespot.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.foundation.RoleSelectionScreen
import com.beg.shuttlespot.feature.foundation.SignInScreen
import com.beg.shuttlespot.feature.foundation.WelcomeScreen

object AuthRoute {
    const val GRAPH = "auth"                         // route induk (pintu masuk)

    const val WELCOME = "auth/welcome"
    const val ROLE_SELECTION = "auth/role_selection"
    const val SIGN_IN = "auth/sign_in/{role}"

    const val ARG_ROLE = "role"
    fun signIn(role: UserRole) = "auth/sign_in/${role.name}"
}

/**
 * Alur (plan.md layar 1-3 dan 19): Welcome -> RoleSelection -> SignIn(role).
 * Setelah sign-in berhasil, graph ini memanggil [onSignedIn]; tujuan berikutnya
 * (onboarding penumpang atau langsung shell driver) diputuskan oleh pemanggil.
 */
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
