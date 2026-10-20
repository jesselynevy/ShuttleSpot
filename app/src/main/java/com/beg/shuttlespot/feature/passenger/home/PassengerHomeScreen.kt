package com.beg.shuttlespot.feature.passenger.home

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.beg.shuttlespot.feature.passenger.boarding.BoardingIntentDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.ui.components.BusStatusCard
import com.beg.shuttlespot.core.ui.components.BusStatusType
import com.beg.shuttlespot.core.ui.components.ErrorState
import com.beg.shuttlespot.core.ui.components.LoadingState
import com.beg.shuttlespot.core.ui.components.OfflineState
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.ui.components.GreetingHeader
import com.beg.shuttlespot.core.ui.components.LinkText
import com.beg.shuttlespot.core.ui.components.PengumumanHomeCard
import com.beg.shuttlespot.core.ui.components.TodayRideStatus
import com.beg.shuttlespot.core.ui.components.TripSummaryCard

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
        GreetingHeader(name = "Angela Benedictin Sunny", avatar = painterResource(R.drawable.avatar))
        TripSummaryCard(routeName = "BSD", pickupStop = "Halte ABCD", todayStatus = TodayRideStatus.NotRiding)
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ){
            Text("Pantauan Perjalanan & Konfirmasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Text("Pengumuman", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
