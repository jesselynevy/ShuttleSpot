package com.beg.shuttlespot.feature.foundation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.feature.auth.AuthViewModel
import com.beg.shuttlespot.feature.driver.trip.DriverTripViewModel

@Composable
fun RoleSelectionScreen(onPassenger: () -> Unit, onDriver: () -> Unit) = BaseColumn("Pilih tipe pengguna") {
    Button(onClick = onPassenger, modifier = Modifier.fillMaxWidth()) { Text("Mahasiswa") }
    OutlinedButton(onClick = onDriver, modifier = Modifier.fillMaxWidth()) { Text("Driver") }
}

@Composable
fun SignInScreen(isDriver: Boolean, onSuccess: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    var value by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.signedIn) { if (state.signedIn) onSuccess() }
    BaseColumn(if (isDriver) "Login Driver" else "Masuk Mahasiswa") {
        Text(if (isDriver) "Masukkan nomor yang sudah terdaftar." else "Gunakan email @student.umn.ac.id.")
        OutlinedTextField(value = value, onValueChange = { value = it }, modifier = Modifier.fillMaxWidth(), label = { Text(if (isDriver) "Nomor telepon" else "Email mahasiswa") }, singleLine = true)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { if (isDriver) viewModel.signInDriver(value) else viewModel.signInPassenger(value) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.loading) "Memeriksa…" else "Lanjut")
        }
    }
}

@Composable
fun DriverHomeScreen(onActiveTrip: () -> Unit, viewModel: DriverTripViewModel = hiltViewModel()) {
    val trip by viewModel.trip.collectAsState()
    BaseColumn("Dashboard Driver") {
        if (trip == null) Text("Tidak ada trip yang ditugaskan hari ini.") else {
            Text("${trip!!.vehicleLabel} · ${trip!!.currentStopSequence}/${trip!!.totalStops} halte")
            Text("Menunggu di halte berikutnya: ${trip!!.waitingPassengerCount} mahasiswa")
            Button(onClick = { viewModel.startTrip(); onActiveTrip() }, modifier = Modifier.fillMaxWidth()) { Text("Mulai perjalanan") }
        }
    }
}

@Composable
fun DriverActiveTripScreen(onFinished: () -> Unit, viewModel: DriverTripViewModel = hiltViewModel()) {
    val trip by viewModel.trip.collectAsState()
    BaseColumn("Trip aktif") {
        Text(trip?.nextStopName?.let { "Halte berikutnya: $it" } ?: "Trip telah selesai")
        Text("Progress: ${trip?.currentStopSequence ?: 0}/${trip?.totalStops ?: 0}")
        Button(onClick = viewModel::confirmStop, modifier = Modifier.fillMaxWidth()) { Text("Tiba di halte ini") }
        OutlinedButton(onClick = { viewModel.finishTrip(); onFinished() }, modifier = Modifier.fillMaxWidth()) { Text("Selesaikan perjalanan") }
    }
}

@Composable
fun BaseColumn(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()
            .padding(horizontal = 16.dp, vertical = 24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        content()
    }
}
