package com.beg.shuttlespot.navigation

// Overview: Defines auth nav graph for the shuttlespot navigation area of ShuttleSpot.

// Dependencies used by this file.
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.auth.CheckEmailScreen
import com.beg.shuttlespot.feature.auth.ForgotPasswordScreen
import com.beg.shuttlespot.feature.auth.NewPasswordScreen
import com.beg.shuttlespot.feature.auth.RoleSelectionScreen
import com.beg.shuttlespot.feature.auth.SignInScreen
import com.beg.shuttlespot.feature.auth.SignUpScreen
import com.beg.shuttlespot.feature.foundation.WelcomeScreen

// Defines the auth route type.
object AuthRoute {
    const val GRAPH = "auth" // parent route

    const val WELCOME = "auth/welcome"
    const val ROLE_SELECTION = "auth/role_selection"
    const val SIGN_IN = "auth/sign_in/{role}?accountAlreadyRegistered={accountAlreadyRegistered}"

    const val ARG_ROLE = "role"
    const val ARG_ACCOUNT_ALREADY_REGISTERED = "accountAlreadyRegistered"
    const val SIGN_UP = "auth/sign_up"
    const val CHECK_EMAIL = "auth/check_email/{email}"
    const val FORGOT_PASSWORD = "auth/forgot_password"
    const val NEW_PASSWORD = "auth/new_password"

    const val ARG_EMAIL = "email"

    fun checkEmail(email: String): String =
        "auth/check_email/${android.net.Uri.encode(email)}"

    // Handles the sign in responsibility.
    fun signIn(
        role: UserRole,
        accountAlreadyRegistered: Boolean = false,
    ) = "auth/sign_in/${role.name}?accountAlreadyRegistered=$accountAlreadyRegistered"
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
            arguments = listOf(
                navArgument(AuthRoute.ARG_ROLE) {
                    type = NavType.StringType
                    defaultValue = UserRole.PASSENGER.name
                },
                navArgument(AuthRoute.ARG_ACCOUNT_ALREADY_REGISTERED) {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "shuttlespot://auth/callback"
                },
            ),
        ) { entry ->
            val role = entry.arguments
                ?.getString(AuthRoute.ARG_ROLE)
                ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                ?: UserRole.PASSENGER

            SignInScreen(
                isDriver = role == UserRole.DRIVER,
                accountAlreadyRegistered = entry.arguments
                    ?.getBoolean(AuthRoute.ARG_ACCOUNT_ALREADY_REGISTERED)
                    ?: false,
                onSuccess = { onSignedIn(role) },
                onCreateAccount = {
                    navController.navigate(AuthRoute.SIGN_UP)
                },
                onForgotPassword = {
                    navController.navigate(AuthRoute.FORGOT_PASSWORD)
                },
                showPassengerOnboardingProgress = role == UserRole.PASSENGER,
            )
        }

        composable(AuthRoute.SIGN_UP) {
            SignUpScreen(
                onCheckEmail = { email ->
                    navController.navigate(AuthRoute.checkEmail(email))
                },
                onAccountAlreadyRegistered = {
                    navController.navigate(
                        AuthRoute.signIn(
                            role = UserRole.PASSENGER,
                            accountAlreadyRegistered = true,
                        ),
                    ) {
                        popUpTo(AuthRoute.SIGN_UP) {
                            inclusive = true
                        }
                    }
                },
                onBack = navController::popBackStack,
            )
        }

        composable(
            route = AuthRoute.CHECK_EMAIL,
            arguments = listOf(
                navArgument(AuthRoute.ARG_EMAIL) {
                    type = NavType.StringType
                },
            ),
        ) { entry ->
            val email = entry.arguments
                ?.getString(AuthRoute.ARG_EMAIL)
                .orEmpty()

            CheckEmailScreen(
                email = email,
                onBackToSignIn = {
                    navController.navigate(AuthRoute.signIn(UserRole.PASSENGER)) {
                        popUpTo(AuthRoute.CHECK_EMAIL) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(AuthRoute.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = navController::popBackStack,
            )
        }

        composable(
            route = AuthRoute.NEW_PASSWORD,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "shuttlespot://auth/reset-password"
                },
            ),
        ) {
            NewPasswordScreen(
                onPasswordUpdated = {
                    navController.navigate(AuthRoute.signIn(UserRole.PASSENGER)) {
                        popUpTo(AuthRoute.NEW_PASSWORD) {
                            inclusive = true
                        }
                    }
                },
            )
        }
    }
}
