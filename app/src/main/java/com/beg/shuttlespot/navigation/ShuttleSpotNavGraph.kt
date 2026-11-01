package com.beg.shuttlespot.navigation

// Overview: Defines shuttle spot nav graph for the shuttlespot navigation area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
// Dependencies used by this file.
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel

// Defines the root route type.
object RootRoute {
    const val GATE = "gate" //the app begins at GATE that will render SessionGate
}

@Composable
// Renders the shuttle spot nav graph UI section.
fun ShuttleSpotNavGraph(navController: NavHostController) {
    // Declares the navigation destinations handled by this graph.
    NavHost(
        navController = navController,
        startDestination = RootRoute.GATE,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) },
    ) {
        composable(RootRoute.GATE) { SessionGate(navController) }

        authNavGraph(navController) { role ->
            val next = when (role) {
                UserRole.PASSENGER -> OnboardingRoute.GRAPH   // pilih rute dulu (plan.md layar 4-10)
                UserRole.DRIVER -> MainRoute.SHELL            // driver langsung ke Driver Home (layar 19-20)
            }
            navController.navigate(next) { popUpTo(AuthRoute.GRAPH) { inclusive = true } }
        }

        passengerOnboardingNavGraph(navController) {
            navController.navigate(MainRoute.SHELL) { popUpTo(OnboardingRoute.GRAPH) { inclusive = true } }
        }

        mainNavGraph(navController)
        passengerFlowNavGraph(navController)
        driverFlowNavGraph(navController)
    }
}

@Composable
// Renders the session gate UI section.
private fun SessionGate(navController: NavHostController, viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    // Handles the current state branch.
    when (val s = state) {
        SessionUiState.Loading -> Unit
        is SessionUiState.Ready -> LaunchedEffect(Unit) {
            val target = if (s.session != null) MainRoute.SHELL else AuthRoute.GRAPH
            navController.navigate(target) { popUpTo(RootRoute.GATE) { inclusive = true } }
        }
    }
}
