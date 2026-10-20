package com.beg.shuttlespot.feature.passenger.route

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.model.TimetableEntry
import com.beg.shuttlespot.core.ui.components.EmptyState
import com.beg.shuttlespot.core.ui.components.LoadingState

@Composable
fun RouteSelectionScreen(onFinished: () -> Unit, viewModel: RouteSelectionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    when {
        state.selectionSaved -> RouteSavedScreen(onFinished)
        state.showReview -> RouteReviewScreen(state, viewModel::closeReview, viewModel::saveSelection)
        else -> RoutePickerScreen(state, viewModel::selectRoute, viewModel::selectPickup, viewModel::selectDestination, viewModel::openReview)
    }
}

@Composable
private fun RoutePickerScreen(state: RouteSelectionUiState, onRouteSelected: (String) -> Unit, onPickupSelected: (String) -> Unit, onDestinationSelected: (String) -> Unit, onReview: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Text("Pilih rute dan halte", style = MaterialTheme.typography.headlineMedium)
        Text("Tentukan halte jemput dan tujuan untuk melihat jadwal perjalanan.", modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            item { Text("Rute tersedia", style = MaterialTheme.typography.titleMedium) }
            if (state.isLoading) {
                item { LoadingState("Memuat rute dan halte") }
            } else if (state.routes.isEmpty()) {
                item { EmptyState("Belum ada rute", "Rute belum tersedia. Coba lagi saat data tersinkronkan.") }
            } else {
                items(state.routes, key = { it.id }) { route -> RouteCard(route, route.id == state.selectedRoute?.route?.id) { onRouteSelected(route.id) } }
            }
            state.selectedRoute?.let { details ->
                item { Text("Halte jemput", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
                items(details.stops, key = { "pickup_${it.id}" }) { stop -> StopChoice(stop, state.pickupStopId == stop.id, onPickupSelected) }
                item { Text("Tujuan", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
                items(details.stops, key = { "destination_${it.id}" }) { stop -> StopChoice(stop, state.destinationStopId == stop.id, onDestinationSelected) }
                item { TimetableCard(details.timetableEntries, details.serviceNotice) }
            }
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
        Button(onClick = onReview, modifier = Modifier.fillMaxWidth(), enabled = state.selectedRoute != null && !state.isLoading) { Text("Tinjau pilihan") }
    }
}

@Composable
private fun RouteCard(route: Route, selected: Boolean, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Column(Modifier.padding(start = 8.dp)) { Text(route.name, style = MaterialTheme.typography.titleMedium); Text("Data jadwal demo", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun StopChoice(stop: Stop, selected: Boolean, onSelected: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onSelected(stop.id) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = { onSelected(stop.id) })
        Column(Modifier.padding(start = 8.dp)) { Text("${stop.sequence}. ${stop.name}"); Text("Urutan halte ${stop.sequence}", style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun TimetableCard(entries: List<TimetableEntry>, serviceNotice: String?) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Jadwal keberangkatan", style = MaterialTheme.typography.titleMedium)
            entries.forEach { entry -> Text("${entry.period} · ${entry.time} — ${entry.description}", modifier = Modifier.padding(top = 4.dp)) }
            serviceNotice?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}

@Composable
private fun RouteReviewScreen(state: RouteSelectionUiState, onBack: () -> Unit, onConfirm: () -> Unit) {
    val route = state.selectedRoute ?: return
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Tinjau pilihan rute", style = MaterialTheme.typography.headlineMedium)
        Text("Pastikan rute utama Anda sudah benar sebelum disimpan.")
        ReviewRow("Rute", route.route.name); ReviewRow("Halte jemput", state.pickupStop?.name.orEmpty()); ReviewRow("Tujuan", state.destinationStop?.name.orEmpty())
        HorizontalDivider(); Text("Jadwal: ${route.timetableEntries.joinToString { "${it.period} ${it.time}" }}")
        Spacer(Modifier.weight(1f))
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text("Sudah benar") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Ubah pilihan") }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) { Column { Text(label, style = MaterialTheme.typography.labelLarge); Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium) } }

@Composable
private fun RouteSavedScreen(onFinished: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Rute utama siap", style = MaterialTheme.typography.headlineMedium)
        Text("Pilihan rute, halte jemput, dan tujuan Anda sudah disimpan.", modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onFinished, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) { Text("Ke beranda") }
    }
}
