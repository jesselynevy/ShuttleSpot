package com.beg.shuttlespot.feature.shell

import androidx.compose.runtime.Composable
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen
import com.beg.shuttlespot.feature.passenger.route.RouteSelectionScreen

/** Tab Rute. Penumpang: ubah rute utama (plan.md layar 15). Driver: rute yang ditugaskan. */
@Composable
fun RoutesTab(role: UserRole, onSelectionSaved: () -> Unit) {
    when (role) {
        UserRole.PASSENGER -> RouteSelectionScreen(onFinished = onSelectionSaved)
        UserRole.DRIVER -> PlaceholderScreen(
            "Rute ditugaskan",
            "Rute dan daftar halte untuk trip hari ini akan tampil di sini.",
        )
    }
}
