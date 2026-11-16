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

@Composable
fun DriverHomeScreen(
    onActiveTrip: () -> Unit,
    viewModel: DriverTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val ready = state as? DriverTripUiState.Ready

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
                    onClick = viewModel::startTrip,
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
