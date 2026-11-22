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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.TripLifecycle
import com.beg.shuttlespot.core.ui.components.SimpleScreenColumn
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun DriverHomeScreen(
    onActiveTrip: () -> Unit,
    viewModel: DriverTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val ready = state as? DriverTripUiState.Ready
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

    LaunchedEffect(ready?.trip?.status) {
        if (ready?.trip?.status == TripLifecycle.ACTIVE) onActiveTrip()
    }

    SimpleScreenColumn(stringResource(R.string.driver_dashboard_title)) {
        when (val current = state) {
            DriverTripUiState.Loading -> CircularProgressIndicator()
            DriverTripUiState.NoAssignment ->
                Text(stringResource(R.string.driver_no_assignment))

            is DriverTripUiState.Error -> {
                Text(current.message)
                OutlinedButton(
                    onClick = viewModel::retry,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.driver_retry)) }
            }

            is DriverTripUiState.Ready -> {
                val trip = current.trip
                Text("${trip.vehicleLabel} · ${trip.currentStopSequence}/${trip.totalStops} halte")
                Text(
                    pluralStringResource(
                        R.plurals.driver_waiting_count,
                        trip.waitingPassengerCount,
                        trip.waitingPassengerCount,
                    ),
                )
                Button(
                    onClick = {
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
                    },
                    enabled = !current.actionInProgress &&
                            trip.status == TripLifecycle.NOT_STARTED,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(
                            if (current.actionInProgress) R.string.driver_starting_trip
                            else R.string.driver_start_trip,
                        ),
                    )
                }
            }
        }
    }
}

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
