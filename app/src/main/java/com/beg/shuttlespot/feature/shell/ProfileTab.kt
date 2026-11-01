package com.beg.shuttlespot.feature.shell

// Overview: Defines profile tab for the feature shell area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DarkMode
// Dependencies used by this file.
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Route
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
// Dependencies used by this file.
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.AvatarHeader
import com.beg.shuttlespot.core.ui.components.ProfileBlockGroup
import com.beg.shuttlespot.core.ui.components.ProfileDataBlock
import com.beg.shuttlespot.core.ui.components.ProfileDataBlockActionType
// Dependencies used by this file.
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel
import com.beg.shuttlespot.core.ui.components.SimpleScreenColumn
import com.beg.shuttlespot.feature.passenger.home.PassengerHomeUiState
import com.beg.shuttlespot.feature.passenger.home.PassengerHomeViewModel
import com.beg.shuttlespot.feature.passenger.home.PassengerTripStatus
import com.beg.shuttlespot.feature.theme.ThemeViewModel

/** Tab Profil (plan.md layar 18). Sign-out menghapus session; shell lalu mengembalikan pengguna ke auth. */
@Composable
// Renders the profile tab UI section.
fun ProfileTab(
    role: UserRole,
    viewModel: SessionViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    routeViewModel: PassengerHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val routeState by routeViewModel.state.collectAsState(initial = PassengerHomeUiState(PassengerTripStatus.NO_ACTIVE_BUS))
    val session = (state as? SessionUiState.Ready)?.session

    val isDarkModeOverride by themeViewModel.isDarkMode.collectAsState()
    val isDarkMode = isDarkModeOverride ?: isSystemInDarkTheme()

    SimpleScreenColumn("Profil") {
        AvatarHeader(
            name = session?.displayName ?: "-",
            role = role,
            isGreeting = false,
            centerContent = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        ProfileBlockGroup("Informasi Pribadi") {
            ProfileDataBlock(
                icon = Icons.Outlined.Person,
                informationTitle = "Email",
                informationData = session?.contact?.takeIf { it.isNotBlank() } ?: "angelabenedictins@gmail.com",
                showDivider = true
            )
            ProfileDataBlock(
                icon = Icons.Outlined.Route,
                informationTitle = "Rute Utama",
                informationData = routeState.primaryRouteName,
                showDivider = true
            )
            ProfileDataBlock(
                icon = Icons.Outlined.DirectionsBus,
                informationTitle = "Halte kamu",
                informationData = routeState.primaryPickupStopName,
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ProfileBlockGroup("Pengaturan") {
            ProfileDataBlock(
                icon = Icons.Outlined.DarkMode,
                informationTitle = "Dark Mode",
                informationData = if (isDarkMode) "Aktif (Mode Gelap)" else "Nonaktif (Mode Terang)",
                actionType = ProfileDataBlockActionType.Switch,
                switchChecked = isDarkMode,
                onSwitchChange = { enabled -> themeViewModel.setDarkMode(enabled) },
                showDivider = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ProfileBlockGroup("Lainnya") {
            ProfileDataBlock(
                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                informationTitle = "Keluar",
                actionType = ProfileDataBlockActionType.Button,
                onClick = viewModel::signOut,
                showDivider = false
            )
        }
    }
}
