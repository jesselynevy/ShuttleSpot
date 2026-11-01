package com.beg.shuttlespot.feature.driver.trip

// Overview: Defines driver trip screens for the driver trip area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.beg.shuttlespot.core.ui.components.SimpleScreenColumn

@Composable
// Renders the driver home screen UI section.
fun DriverHomeScreen(onActiveTrip: () -> Unit, viewModel: DriverTripViewModel = hiltViewModel()) {
    val trip by viewModel.trip.collectAsState()
    SimpleScreenColumn("Dashboard Driver") {
        if (trip == null) {
            Text("Tidak ada trip yang ditugaskan hari ini.")
        } else {
            Text("${trip!!.vehicleLabel} · ${trip!!.currentStopSequence}/${trip!!.totalStops} halte")
            Text("Menunggu di halte berikutnya: ${trip!!.waitingPassengerCount} mahasiswa")
            Button({ viewModel.startTrip(); onActiveTrip() }, Modifier.fillMaxWidth()) { Text("Mulai perjalanan") }
        }
    }
}

@Composable
// Renders the driver active trip screen UI section.
fun DriverActiveTripScreen(onFinished: () -> Unit, viewModel: DriverTripViewModel = hiltViewModel()) {
    val trip by viewModel.trip.collectAsState()
    SimpleScreenColumn("Trip aktif") {
        Text(trip?.nextStopName?.let { "Halte berikutnya: $it" } ?: "Trip telah selesai")
        Text("Progress: ${trip?.currentStopSequence ?: 0}/${trip?.totalStops ?: 0}")
        Button(viewModel::confirmStop, Modifier.fillMaxWidth()) { Text("Tiba di halte ini") }
        OutlinedButton({ viewModel.finishTrip(); onFinished() }, Modifier.fillMaxWidth()) { Text("Selesaikan perjalanan") }
    }
}
