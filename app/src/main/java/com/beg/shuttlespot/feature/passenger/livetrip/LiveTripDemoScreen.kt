package com.beg.shuttlespot.feature.passenger.livetrip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.ui.components.BusRouteProgress
import com.beg.shuttlespot.core.ui.components.OpenStreetMap
import java.text.DateFormat
import java.util.Date

@Composable
fun LiveTripDemoScreen(onBoardingIntentClick: () -> Unit, viewModel: LiveTripViewModel = hiltViewModel()) {
    val trip by viewModel.trip.collectAsState()
    when (val snapshot = trip) {
        null -> Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.spacedBy(16.dp), Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text("Memuat perjalanan demo…")
        }
        else -> LiveTripContent(snapshot, onBoardingIntentClick)
    }
}

@Composable
private fun LiveTripContent(trip: ActiveTripSnapshot, onBoardingIntentClick: () -> Unit) {
    val location = trip.location
    Column(
        Modifier.fillMaxSize().navigationBarsPadding().imePadding()
            .padding(horizontal = 16.dp, vertical = 16.dp).verticalScroll(rememberScrollState()),
        Arrangement.spacedBy(16.dp)
    ) {
        Text("Pelacak Bus", style = MaterialTheme.typography.headlineMedium)
        Text("Data simulasi untuk demo — bukan pelacakan real-time.", style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), Arrangement.spacedBy(8.dp)) {
                Text("${trip.vehicleLabel} · menuju ${trip.nextStopName ?: "halte berikutnya"}", style = MaterialTheme.typography.titleMedium)
                Text("ETA ${trip.eta.minutes?.let { "$it menit" } ?: "tidak tersedia"}", style = MaterialTheme.typography.headlineSmall)
                BusRouteProgress(trip.progress, Modifier.fillMaxWidth())
                Text("Progres rute ${"%.0f".format(trip.progress * 100)}%", style = MaterialTheme.typography.labelLarge)
            }
        }
        OpenStreetMap(
            Modifier.fillMaxWidth().height(240.dp).semantics { contentDescription = "Peta posisi bus simulasi" },
            latitude = location?.latitude ?: -6.2561,
            longitude = location?.longitude ?: 106.6186
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Text("Posisi bus simulasi: ${location?.latitude?.let { "%.4f".format(it) } ?: "—"}, ${location?.longitude?.let { "%.4f".format(it) } ?: "—"}")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Update, contentDescription = null)
            Text("Data terakhir diperbarui ${trip.lastUpdatedEpochMillis?.let(::formatTime) ?: "tidak tersedia"}")
        }
        Button(onClick = onBoardingIntentClick, modifier = Modifier.fillMaxWidth()) { Text("Konfirmasi akan naik") }
    }
}

private fun formatTime(epochMillis: Long): String = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(epochMillis))
