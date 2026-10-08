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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.ui.components.BusStatusCard
import com.beg.shuttlespot.core.ui.components.BusStatusType
import com.beg.shuttlespot.core.ui.components.ErrorState
import com.beg.shuttlespot.core.ui.components.LoadingState
import com.beg.shuttlespot.core.ui.components.OfflineState

@Composable
fun PassengerHomeScreen(
    onLiveTrip: () -> Unit,
    onBoardingIntent: () -> Unit,
    onRoutes: () -> Unit,
    onAnnouncements: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    viewModel: PassengerHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState(initial = PassengerHomeUiState(PassengerTripStatus.NO_ACTIVE_BUS))
    PassengerHomeContent(state, onLiveTrip, onBoardingIntent, onRoutes, onAnnouncements, onNotifications, onProfile)
}

@Composable
fun PassengerHomeContent(
    state: PassengerHomeUiState,
    onLiveTrip: () -> Unit,
    onBoardingIntent: () -> Unit,
    onRoutes: () -> Unit,
    onAnnouncements: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Beranda", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onNotifications) { Icon(Icons.Default.Notifications, contentDescription = "Notifikasi") }
        }
        Text("Pantau perjalanan shuttle pilihan Anda.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            state.isLoading -> LoadingState("Memuat status perjalanan")
            state.errorMessage != null -> ErrorState(state.errorMessage)
            else -> {
                BusStatusCard(busStatus = BusStatusType.PassedStops)
                Text("Informasi perjalanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (state.status == PassengerTripStatus.NO_ACTIVE_BUS) "Pilih atau perbarui rute utama untuk melihat jadwal shuttle berikutnya."
                    else "Status dan ETA pada layar ini adalah data demo. Posisi langsung tersedia di pelacak perjalanan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

