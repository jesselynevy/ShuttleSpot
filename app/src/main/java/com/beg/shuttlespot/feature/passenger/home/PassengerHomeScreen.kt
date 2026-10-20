package com.beg.shuttlespot.feature.passenger.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.beg.shuttlespot.feature.passenger.boarding.BoardingIntentDialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.ui.components.BusStatusCard
import com.beg.shuttlespot.core.ui.components.BusStatusType
import com.beg.shuttlespot.core.ui.components.ErrorState
import com.beg.shuttlespot.core.ui.components.LoadingState
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.AvatarHeader
import com.beg.shuttlespot.core.ui.components.LinkText
import com.beg.shuttlespot.core.ui.components.PengumumanHomeCard
import com.beg.shuttlespot.core.ui.components.TodayRideStatus
import com.beg.shuttlespot.core.ui.components.TripSummaryCard
import com.beg.shuttlespot.feature.auth.SessionUiState

@Composable
fun PassengerHomeScreen(
    onLiveTrip: () -> Unit,
    onRoutes: () -> Unit,
    onAnnouncements: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    viewModel: PassengerHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState(initial = PassengerHomeUiState(PassengerTripStatus.NO_ACTIVE_BUS))
    var showBoardingIntent by remember { mutableStateOf(false) }
    PassengerHomeContent(
        state = state,
        onLiveTrip = onLiveTrip,
        onBoardingIntent = { showBoardingIntent = true },
        onRoutes = onRoutes,
        onAnnouncements = onAnnouncements,
        onNotifications = onNotifications,
        onProfile = onProfile,
    )
    if (showBoardingIntent) BoardingIntentDialog(onDismiss = { showBoardingIntent = false })
}

@Composable
fun PassengerHomeContent(
    state: PassengerHomeUiState,
    onLiveTrip: () -> Unit,
    onBoardingIntent: () -> Unit,
    onRoutes: () -> Unit,
    onAnnouncements: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
) {
    val previewAnnouncements = listOf(
        Announcement("1", "Sistem", "Bus baru saja jalan!", System.currentTimeMillis() - 10 * 60_000, isAutomatic = true),
        Announcement("2", "Pak Wahyu", "Hari ini Bus akan telat sekian menit karena ada yang lelet!", System.currentTimeMillis() - 10 * 60_000),
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AvatarHeader(name = "Angela Benedictin Sunny", role = UserRole.PASSENGER, avatar = painterResource(R.drawable.avatar))
        TripSummaryCard(routeName = "BSD", pickupStop = "Halte ABCD", todayStatus = TodayRideStatus.NotRiding)
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ){
            Text("Pantauan Perjalanan & Konfirmasi", style = MaterialTheme.typography.titleSmall)
            when {
                state.isLoading -> LoadingState("Memuat status perjalanan")
                state.errorMessage != null -> ErrorState(state.errorMessage)
                else -> {
                    BusStatusCard(
                        busStatus = BusStatusType.PassedStops,
                        onBoardingIntent = onBoardingIntent,
                        onLiveTrip = onLiveTrip,
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),

        ){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Text("Pengumuman", style = MaterialTheme.typography.titleSmall)
                LinkText("Lihat Pengumuman", onClick = onAnnouncements)
            }

            PengumumanHomeCard(previewAnnouncements)

        }

    }
}

private val previewState = PassengerHomeUiState(
    status = PassengerTripStatus.EN_ROUTE,
    vehicleLabel = "B 1234 XYZ",
    nextStopName = "Halte ABCD",
    etaMinutes = 5,
    progress = 0.3f,
    canConfirmBoarding = true,
    canViewLiveTrip = true,
)

@Preview(showBackground = true, name = "Light")
@Composable
private fun PassengerHomeContentPreview() {
    ShuttleSpotTheme(darkTheme = false) {
        PassengerHomeContent(
            state = previewState,
            onLiveTrip = {},
            onBoardingIntent = {},
            onRoutes = {},
            onAnnouncements = {},
            onNotifications = {},
            onProfile = {},
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun PassengerHomeContentDarkPreview() {
    ShuttleSpotTheme(darkTheme = true) {
        PassengerHomeContent(
            state = previewState,
            onLiveTrip = {},
            onBoardingIntent = {},
            onRoutes = {},
            onAnnouncements = {},
            onNotifications = {},
            onProfile = {},
        )
    }
}
