package com.beg.shuttlespot.feature.driver.trip

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.model.TripLifecycle
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.core.ui.components.DriverStopState
import com.beg.shuttlespot.core.ui.components.DriverStopUi
import com.beg.shuttlespot.core.ui.components.SimpleScreenColumn
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.AvatarHeader
import com.beg.shuttlespot.core.ui.components.DriverBusStatusCard
import com.beg.shuttlespot.core.ui.components.DriverStatusStrip
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

@Composable
fun DriverHomeScreen(
    onLiveTrip: () -> Unit,
    viewModel: DriverTripViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val routeDetails by viewModel.routeDetails.collectAsState()
    val sessionState by sessionViewModel.state.collectAsState()
    val driverName = (sessionState as? SessionUiState.Ready)?.session?.displayName ?: "Driver"
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[Manifest.permission.POST_NOTIFICATIONS] == true
        } else true
        if ((fineGranted || coarseGranted) && notifGranted) {
            viewModel.startTrip()
        }
    }

    val onStartTrip: () -> Unit = {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        if (fine && notif) {
            viewModel.startTrip()
        } else {
            val toAsk = buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()
            permissionLauncher.launch(toAsk)
        }
    }

    val trip = (state as? DriverTripUiState.Ready)?.trip
    val extended = LocalExtendedColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AvatarHeader(name = driverName, role = UserRole.DRIVER, avatar = painterResource(R.drawable.avatar))

        // still partially hardcoded
        DriverStatusStrip(
            dutyStatus = "Bertugas",
            shift = "06:00 - 14:00",
            tripIndex = trip?.currentStopSequence ?: 0,
            tripTotal = trip?.totalStops ?: 0,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Pantauan Perjalanan Anda", style = MaterialTheme.typography.titleSmall)
            DriverBusStatusCard(
                status = trip?.status?: TripLifecycle.NOT_STARTED,
                stops = if (trip != null) buildDriverStops(trip, routeDetails?.stops.orEmpty()) else emptyList(),
                nextStopName = trip?.nextStopName,
                // eta to do
                eta = null,
                startEnabled = trip != null,
                startLabel = if (trip != null) stringResource(R.string.driver_start_trip)
                else stringResource(R.string.driver_no_assignment),
                onStartTrip = onStartTrip,
                onLiveTrip = onLiveTrip,
            )
        }
    }
}

// util
private fun buildDriverStops(trip: DriverTripSnapshot, stops: List<Stop>): List<DriverStopUi> =
    stops.sortedBy { it.sequence }.map { stop ->
        val stopState = when {
            trip.status == TripLifecycle.NOT_STARTED -> DriverStopState.UPCOMING
            stop.sequence < trip.currentStopSequence -> DriverStopState.DONE
            stop.sequence == trip.currentStopSequence -> DriverStopState.CURRENT
            stop.sequence == trip.currentStopSequence + 1 -> DriverStopState.NEXT
            else -> DriverStopState.UPCOMING
        }
        DriverStopUi(
            name = stop.name,
            scheduledTime = stop.scheduledTime ?: "-",
            passengerCount = if (stopState == DriverStopState.CURRENT) trip.waitingPassengerCount else 0,
            state = stopState,
        )
    }


// to be later replaced by real time map, still temporary
@Composable
fun DriverActiveTripScreen(
    onFinished: () -> Unit,
    viewModel: DriverTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val ready = state as? DriverTripUiState.Ready

    LaunchedEffect(ready?.trip?.status) {
        if (ready?.trip?.status == TripLifecycle.COMPLETED) onFinished()
    }

    SimpleScreenColumn(stringResource(R.string.driver_active_trip_title)) {
        when (val current = state) {
            DriverTripUiState.Loading -> CircularProgressIndicator()
            DriverTripUiState.NoAssignment -> Text(stringResource(R.string.driver_active_trip_missing))
            is DriverTripUiState.Error -> {
                Text(current.message)
                OutlinedButton(
                    onClick = viewModel::retry,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.driver_retry)) }
            }

            is DriverTripUiState.Ready -> {
                val trip = current.trip
                Text(
                    trip.nextStopName?.let {
                        stringResource(R.string.driver_next_stop, it)
                    } ?: stringResource(R.string.driver_final_stop),
                )
                Text(
                    stringResource(
                        R.string.driver_progress,
                        trip.currentStopSequence,
                        trip.totalStops,
                    ),
                )
                Text(
                    pluralStringResource(
                        R.plurals.driver_waiting_count,
                        trip.waitingPassengerCount,
                        trip.waitingPassengerCount,
                    ),
                )
                Button(
                    onClick = viewModel::advanceStop,
                    enabled = !current.actionInProgress &&
                        trip.status == TripLifecycle.ACTIVE &&
                        trip.currentStopSequence < trip.totalStops,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.driver_advance_stop)) }
                OutlinedButton(
                    onClick = viewModel::finishTrip,
                    enabled = !current.actionInProgress &&
                        trip.status == TripLifecycle.ACTIVE &&
                        trip.currentStopSequence >= trip.totalStops,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.driver_finish_trip)) }
            }
        }
    }
}
