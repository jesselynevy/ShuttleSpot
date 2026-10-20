package com.beg.shuttlespot.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel

object RootRoute {
    const val GATE = "gate"   // titik awal: memutuskan auth atau shell berdasarkan session
}

/** NavHost root. Setiap graph didefinisikan di file sendiri; di sini hanya dirangkai. */
@Composable
fun ShuttleSpotNavGraph(navController: NavHostController) {
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
private fun SessionGate(navController: NavHostController, viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    when (val s = state) {
        SessionUiState.Loading -> Unit
        is SessionUiState.Ready -> LaunchedEffect(Unit) {
            val target = if (s.session != null) MainRoute.SHELL else AuthRoute.GRAPH
            navController.navigate(target) { popUpTo(RootRoute.GATE) { inclusive = true } }
        }
    }
}
