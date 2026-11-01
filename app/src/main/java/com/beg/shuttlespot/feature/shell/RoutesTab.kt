package com.beg.shuttlespot.feature.shell

// Overview: Defines routes tab for the feature shell area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.runtime.Composable
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen
import com.beg.shuttlespot.feature.passenger.route.RouteSelectionScreen

/** Tab Rute. Penumpang: ubah rute utama (plan.md layar 15). Driver: rute yang ditugaskan. */
@Composable
// Renders the routes tab UI section.
fun RoutesTab(role: UserRole, onSelectionSaved: () -> Unit) {
    // Handles the current state branch.
    when (role) {
        UserRole.PASSENGER -> RouteSelectionScreen(onFinished = onSelectionSaved)
        UserRole.DRIVER -> PlaceholderScreen(
            "Rute ditugaskan",
            "Rute dan daftar halte untuk trip hari ini akan tampil di sini.",
        )
    }
}
