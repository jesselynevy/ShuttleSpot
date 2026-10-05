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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    Scaffold(bottomBar = { PassengerBottomNavigation(onRoutes, onAnnouncements, onNotifications, onProfile) }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(paddingValues).padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Beranda", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Pantau perjalanan shuttle pilihan Anda.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            when {
                state.isLoading -> LoadingState("Memuat status perjalanan")
                state.errorMessage != null -> ErrorState(state.errorMessage)
                else -> {
                    PassengerTripStatusCard(state, onLiveTrip, onBoardingIntent)
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
}

@Composable
private fun PassengerTripStatusCard(state: PassengerHomeUiState, onLiveTrip: () -> Unit, onBoardingIntent: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(state.vehicleLabel ?: "Layanan shuttle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(state.status.label, style = MaterialTheme.typography.bodyMedium, color = statusColor(state.status))
                }
                state.etaMinutes?.let { Text("$it mnt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            }
            if (state.canViewLiveTrip) {
                TripProgress(state.progress, state.nextStopName)
            } else {
                HorizontalDivider()
                Text(emptyStateMessage(state.status), style = MaterialTheme.typography.bodyMedium)
            }
            if (state.isStale) OfflineState()
            LastUpdatedLabel(state.lastUpdatedEpochMillis, state.isStale)
            if (state.canConfirmBoarding) Button(onClick = onBoardingIntent, modifier = Modifier.fillMaxWidth()) { Text("Konfirmasi akan naik") }
            if (state.canViewLiveTrip) OutlinedButton(onClick = onLiveTrip, modifier = Modifier.fillMaxWidth()) { Text("Lihat pelacak perjalanan") }
        }
    }
}

@Composable
private fun TripProgress(progress: Float, nextStopName: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progres perjalanan", style = MaterialTheme.typography.labelLarge)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
        }
        androidx.compose.material3.LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Progres perjalanan ${(progress * 100).toInt()} persen" })
        nextStopName?.let { Text("Halte berikutnya: $it", style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun LastUpdatedLabel(timestamp: Long?, isStale: Boolean) {
    val context = LocalContext.current
    val text = timestamp?.let { "Data terakhir diperbarui ${DateFormat.getTimeFormat(context).format(it)}" } ?: "Belum ada pembaruan perjalanan"
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(if (isStale) "$text (mungkin tidak terbaru)" else text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PassengerBottomNavigation(onRoutes: () -> Unit, onAnnouncements: () -> Unit, onNotifications: () -> Unit, onProfile: () -> Unit) {
    Surface(shadowElevation = 6.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            PassengerNavItem("Beranda", Icons.Default.Home, true) {}
            PassengerNavItem("Rute", Icons.Default.Route, false, onRoutes)
            PassengerNavItem("Info", Icons.Default.Campaign, false, onAnnouncements)
            PassengerNavItem("Notifikasi", Icons.Default.Notifications, false, onNotifications)
            PassengerNavItem("Profil", Icons.Default.Person, false, onProfile)
        }
    }
}

@Composable
private fun PassengerNavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(0.2f).semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
            OutlinedButton(onClick = onClick, modifier = Modifier.size(48.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun emptyStateMessage(status: PassengerTripStatus): String = when (status) {
    PassengerTripStatus.NO_ACTIVE_BUS -> "Tidak ada perjalanan aktif saat ini. Periksa jadwal atau rute utama Anda."
    PassengerTripStatus.MISSED -> "Bus telah melewati halte jemput Anda. Periksa perjalanan berikutnya."
    PassengerTripStatus.COMPLETED -> "Perjalanan ini sudah selesai. Tunggu perjalanan berikutnya."
    PassengerTripStatus.NOT_STARTED -> "Perjalanan belum dimulai."
    else -> "Informasi perjalanan sedang diperbarui."
}

@Composable
private fun statusColor(status: PassengerTripStatus) = when (status) {
    PassengerTripStatus.AT_PICKUP, PassengerTripStatus.RIDING -> MaterialTheme.colorScheme.primary
    PassengerTripStatus.MISSED -> MaterialTheme.colorScheme.error
    PassengerTripStatus.NO_ACTIVE_BUS, PassengerTripStatus.COMPLETED, PassengerTripStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
    PassengerTripStatus.EN_ROUTE -> MaterialTheme.colorScheme.tertiary
}
