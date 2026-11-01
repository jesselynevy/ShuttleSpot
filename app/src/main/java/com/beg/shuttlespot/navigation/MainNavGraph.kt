package com.beg.shuttlespot.navigation

// Overview: Defines main nav graph for the shuttlespot navigation area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.beg.shuttlespot.feature.auth.SessionUiState
// Dependencies used by this file.
import com.beg.shuttlespot.feature.auth.SessionViewModel
import com.beg.shuttlespot.feature.shell.MainShell

/** Shell utama. Role diambil dari session; tanpa session pengguna dikembalikan ke alur auth (mis. setelah sign-out). */
fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    composable(MainRoute.SHELL) {
        val viewModel: SessionViewModel = hiltViewModel()
        val state by viewModel.state.collectAsState()

        // Handles the current state branch.
        when (val s = state) {
            SessionUiState.Loading -> Unit
            is SessionUiState.Ready -> {
                val session = s.session
                if (session != null) {
                    MainShell(role = session.role, rootNav = navController)
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(AuthRoute.GRAPH) { popUpTo(0) }
                    }
                }
            }
        }
    }
}
