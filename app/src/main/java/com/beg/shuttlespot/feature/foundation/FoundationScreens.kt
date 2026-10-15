package com.beg.shuttlespot.feature.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.StudentOnboardingProgressIndicator
import com.beg.shuttlespot.core.ui.components.StudentOnboardingStep
import com.beg.shuttlespot.core.ui.theme.SpringSky
import com.beg.shuttlespot.feature.auth.AuthViewModel
import com.beg.shuttlespot.feature.driver.trip.DriverTripViewModel

@Composable
fun RoleSelectionScreen(onPassenger: () -> Unit, onDriver: () -> Unit) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    Box(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 32.dp, vertical = 28.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(72.dp))
            Text(
                stringResource(R.string.role_selection_title),
                Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(40.dp))
            RoleCard(
                title = stringResource(R.string.role_student),
                description = stringResource(R.string.role_student_description),
                avatarRes = R.drawable.mahasiswa_avatar,
                selected = selectedRole == UserRole.PASSENGER,
                tags = listOf(
                    stringResource(R.string.role_student_tag_track),
                    stringResource(R.string.role_student_tag_eta),
                    stringResource(R.string.role_student_tag_notifications),
                ),
                onClick = { selectedRole = UserRole.PASSENGER },
            )
            Spacer(Modifier.height(14.dp))
            RoleCard(
                title = stringResource(R.string.role_driver),
                description = stringResource(R.string.role_driver_description),
                avatarRes = R.drawable.driver_avatar,
                selected = selectedRole == UserRole.DRIVER,
                tags = listOf(
                    stringResource(R.string.role_driver_tag_route),
                    stringResource(R.string.role_driver_tag_schedule),
                    stringResource(R.string.role_driver_tag_passengers),
                ),
                onClick = { selectedRole = UserRole.DRIVER },
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    when (selectedRole) {
                        UserRole.PASSENGER -> onPassenger()
                        UserRole.DRIVER -> onDriver()
                        null -> Unit
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpringSky),
                enabled = selectedRole != null,
            ) { Text(stringResource(R.string.role_continue), style = MaterialTheme.typography.labelLarge) }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))
            Text(
                stringResource(R.string.welcome_language),
                Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun RoleCard(title: String, description: String, avatarRes: Int, selected: Boolean, tags: List<String>, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFDDF3FF) else Color.White),
        border = BorderStroke(1.dp, if (selected) SpringSky else Color(0xFFE0E0E0)),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(avatarRes),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                RoleTags(tags)
            }
        }
    }
}

@Composable
private fun RoleTags(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        RoleTag(tags[0], Color(0xFFFFDDE2))
        RoleTag(tags[1], Color(0xFFFFEDBC))
    }
    Spacer(Modifier.height(4.dp))
    RoleTag(tags[2], Color(0xFFD5F7D7))
}

@Composable
private fun RoleTag(text: String, color: Color) {
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), Color(0xFF5E5E5E), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SignInScreen(
    isDriver: Boolean,
    onSuccess: () -> Unit,
    showPassengerOnboardingProgress: Boolean = false,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var value by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.signedIn) { if (state.signedIn) onSuccess() }
    BaseColumn(if (isDriver) "Login Driver" else "Masuk Mahasiswa") {
        if (showPassengerOnboardingProgress) {
            StudentOnboardingProgressIndicator(StudentOnboardingStep.SSO)
        }
        Text(if (isDriver) "Masukkan nomor yang sudah terdaftar." else "Gunakan email @student.umn.ac.id.")
        OutlinedTextField(value, { value = it }, Modifier.fillMaxWidth(), label = { Text(if (isDriver) "Nomor telepon" else "Email mahasiswa") }, singleLine = true)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { if (isDriver) viewModel.signInDriver(value) else viewModel.signInPassenger(value) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (state.loading) "Memeriksa…" else "Lanjut") }
    }
}

@Composable
fun DriverHomeScreen(onActiveTrip: () -> Unit, viewModel: DriverTripViewModel = hiltViewModel()) {
    val trip by viewModel.trip.collectAsState()
    BaseColumn("Dashboard Driver") {
        if (trip == null) Text("Tidak ada trip yang ditugaskan hari ini.") else {
            Text("${trip!!.vehicleLabel} · ${trip!!.currentStopSequence}/${trip!!.totalStops} halte")
            Text("Menunggu di halte berikutnya: ${trip!!.waitingPassengerCount} mahasiswa")
            Button({ viewModel.startTrip(); onActiveTrip() }, Modifier.fillMaxWidth()) { Text("Mulai perjalanan") }
        }
    }
}

@Composable
fun DriverActiveTripScreen(onFinished: () -> Unit, viewModel: DriverTripViewModel = hiltViewModel()) {
    val trip by viewModel.trip.collectAsState()
    BaseColumn("Trip aktif") {
        Text(trip?.nextStopName?.let { "Halte berikutnya: $it" } ?: "Trip telah selesai")
        Text("Progress: ${trip?.currentStopSequence ?: 0}/${trip?.totalStops ?: 0}")
        Button(viewModel::confirmStop, Modifier.fillMaxWidth()) { Text("Tiba di halte ini") }
        OutlinedButton({ viewModel.finishTrip(); onFinished() }, Modifier.fillMaxWidth()) { Text("Selesaikan perjalanan") }
    }
}

@Composable
fun BaseColumn(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        content()
    }
}
