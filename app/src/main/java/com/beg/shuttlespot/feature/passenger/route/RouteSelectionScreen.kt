package com.beg.shuttlespot.feature.passenger.route

import androidx.compose.foundation.clickable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.ui.components.EmptyState
import com.beg.shuttlespot.core.ui.components.LoadingState
import com.beg.shuttlespot.core.ui.components.StudentOnboardingProgressHeader
import com.beg.shuttlespot.core.ui.components.StudentOnboardingStep

@Composable
fun RouteSelectionScreen(
    onFinished: () -> Unit,
    showOnboardingProgress: Boolean = false,
    onBack: () -> Unit = {},
    viewModel: RouteSelectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var choosingStops by remember { mutableStateOf(false) }
    if (showOnboardingProgress && !state.selectionSaved) {
        BackHandler {
            when {
                state.showTimetable -> viewModel.closeTimetable()
                choosingStops -> choosingStops = false
                else -> onBack()
            }
        }
    }
    when {
        state.selectionSaved && showOnboardingProgress -> RegistrationCompleteScreen(onFinished)
        state.selectionSaved -> RouteSavedScreen(onFinished, showOnboardingProgress)
        state.showTimetable -> TimetableDetailsScreen(state, viewModel::closeTimetable, viewModel::saveSelection)
        showOnboardingProgress && !choosingStops -> OnboardingRouteList(state, onBack) { id ->
            viewModel.selectRoute(id, useDefaults = false)
            choosingStops = true
        }
        showOnboardingProgress -> OnboardingStopList(state, { choosingStops = false }, viewModel::selectOnboardingStop, viewModel::openTimetable)
        else -> RoutePickerScreen(state, { viewModel.selectRoute(it) }, viewModel::selectPickup, viewModel::selectDestination, viewModel::openTimetable, showOnboardingProgress)
    }
}

@Composable
private fun OnboardingRouteList(state: RouteSelectionUiState, onBack: () -> Unit, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 32.dp)) {
        StudentOnboardingProgressHeader(StudentOnboardingStep.ROUTE)
        Text("Mari kita cari rute yang cocok dengan Anda!", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
        Text("Jadwal dan rute demo", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            if (state.isLoading) item { LoadingState("Memuat rute") }
            else if (state.routes.isEmpty()) item { EmptyState("Belum ada rute", "Rute belum tersedia.") }
            else items(state.routes, key = { it.id }) { route ->
                Card(
                    colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,)
                    ,modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Rute", style = MaterialTheme.typography.labelSmall)
                            Text(route.name, style = MaterialTheme.typography.titleSmall)
                        }
                        RoutePrimaryButton(text = "Pilih", onClick = { onSelect(route.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStopList(state: RouteSelectionUiState, onBack: () -> Unit, onSelect: (String) -> Unit, onShowTimetable: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 32.dp)) {
        StudentOnboardingProgressHeader(StudentOnboardingStep.ROUTE)
        RouteSecondaryButton(text = "‹  Kembali", onClick = onBack)
        Text("Pilih titik jemput dan destinasi", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
        Text(if (state.pickupStop == null || state.destinationStop != null) "Pilih titik jemput terlebih dahulu." else "Pilih destinasi setelah titik jemput.", style = MaterialTheme.typography.bodyMedium)
        state.selectedRoute?.let { details ->
            Text(details.route.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 12.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(details.stops.sortedBy { it.sequence }, key = { it.id }) { stop ->
                    val isPickup = stop.id == state.pickupStopId
                    val isDestination = stop.id == state.destinationStopId
                    Row(Modifier.fillMaxWidth().border(1.dp, if (isPickup || isDestination) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${stop.sequence}. ${stop.name}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        if (isPickup || isDestination) Surface(
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.extraLarge,
                        ) { Text(if (isPickup) "Jemput" else "Tujuan", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) }
                        else RouteSecondaryButton(text = "Pilih", onClick = { onSelect(stop.id) })
                    }
                }
//                item { TimetableCard(details.timetableEntries, details.serviceNotice) }
            }
        } ?: run { Spacer(Modifier.weight(1f)); Text("Memuat halte...") }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp)) }
        RoutePrimaryButton(text = "Pilih Rute", onClick = onShowTimetable, enabled = state.canReview, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TimetableDetailsScreen(
    state: RouteSelectionUiState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val route = state.selectedRoute ?: return
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
    ) {
        StudentOnboardingProgressHeader(StudentOnboardingStep.REVIEW)
        RouteSecondaryButton(text = "‹  Kembali", onClick = onBack)
        Text(
            "Tinjau pilihan rute",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            "Pastikan rute dan jadwal Anda sudah benar sebelum disimpan.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(route.route.name, style = MaterialTheme.typography.titleMedium)
                Text("Titik jemput", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 12.dp))
                Text(state.pickupStop?.name.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                Text("Destinasi", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                Text(state.destinationStop?.name.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ScheduleTime(
                        label = "Waktu berangkat",
                        time = state.timetableSummary?.pickupTime,
                        stopName = state.pickupStop?.name.orEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "→",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    ScheduleTime(
                        label = "Tiba tujuan",
                        time = state.timetableSummary?.destinationTime,
                        stopName = state.destinationStop?.name.orEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Text(text = "*Untuk waktu tiba dititik drop off dapat berubah sesuai kondisi kepadatan lalu lintas", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.weight(1f))
        RoutePrimaryButton(text = "Sudah benar", onClick = onContinue, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
        RouteSecondaryButton(text = "Ubah pilihan halte", onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun RoutePickerScreen(state: RouteSelectionUiState, onRouteSelected: (String) -> Unit, onPickupSelected: (String) -> Unit, onDestinationSelected: (String) -> Unit, onReview: () -> Unit, showOnboardingProgress: Boolean) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 32.dp),
    ) {
        if (showOnboardingProgress) {
            StudentOnboardingProgressHeader(StudentOnboardingStep.ROUTE)
        } else {
            Spacer(Modifier.height(20.dp))
        }
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
//                item { TimetableCard(details.timetableEntries, details.serviceNotice) }
            }
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
        RoutePrimaryButton(text = "Tinjau pilihan", onClick = onReview, enabled = state.selectedRoute != null && !state.isLoading, modifier = Modifier.fillMaxWidth())
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
private fun ScheduleTime(label: String, time: String?, stopName: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(time?.let { "$it WIB" } ?: "Belum tersedia", style = MaterialTheme.typography.titleSmall)
        Text(stopName, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RouteSavedScreen(onFinished: () -> Unit, showOnboardingProgress: Boolean) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 32.dp),
    ) {
        if (showOnboardingProgress) {
            StudentOnboardingProgressHeader(StudentOnboardingStep.REVIEW)
        } else {
            Spacer(Modifier.height(20.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Rute utama siap", style = MaterialTheme.typography.headlineMedium)
            Text("Pilihan rute, halte jemput, dan tujuan Anda sudah disimpan.", modifier = Modifier.padding(top = 8.dp))
            RoutePrimaryButton(text = "Ke beranda", onClick = onFinished, modifier = Modifier.fillMaxWidth().padding(top = 24.dp))
        }
    }
}

/** Matches the primary action treatment used by the notification permission screen. */
@Composable
internal fun RoutePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
        ),
    ) { Text(text) }
}

/** Matches the outlined secondary action treatment used by the notification permission screen. */
@Composable
internal fun RouteSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
    ) { Text(text) }
}
