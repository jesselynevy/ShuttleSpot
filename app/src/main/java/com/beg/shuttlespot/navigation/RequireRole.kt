package com.beg.shuttlespot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel

/** Menampilkan [content] hanya untuk [role] yang cocok; selain itu kembali ke alur auth. */
@Composable
fun RequireRole(
    role: UserRole,
    navController: NavHostController,
    viewModel: SessionViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()
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
