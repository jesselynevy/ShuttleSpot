package com.beg.shuttlespot.navigation

// Overview: Defines require role for the shuttlespot navigation area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.auth.SessionUiState
// Dependencies used by this file.
import com.beg.shuttlespot.feature.auth.SessionViewModel

/** Menampilkan [content] hanya untuk [role] yang cocok; selain itu kembali ke alur auth. */
@Composable
// Renders the require role UI section.
fun RequireRole(
    role: UserRole,
    navController: NavHostController,
    viewModel: SessionViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    // Handles the current state branch.
    when (val s = state) {
        SessionUiState.Loading -> Unit
        is SessionUiState.Ready -> {
            val sessionRole = s.session?.role
            LaunchedEffect(sessionRole) {
                if (sessionRole != role) navController.navigate(AuthRoute.GRAPH) { popUpTo(0) }
            }
            if (sessionRole == role) content()
        }
    }
}
