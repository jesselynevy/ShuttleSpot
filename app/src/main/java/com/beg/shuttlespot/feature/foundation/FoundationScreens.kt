package com.beg.shuttlespot.feature.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.StudentOnboardingProgressHeader
import com.beg.shuttlespot.core.ui.components.StudentOnboardingStep
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.feature.auth.AuthViewModel
import com.beg.shuttlespot.feature.driver.trip.DriverTripViewModel

@Composable
fun RoleSelectionScreen(onPassenger: () -> Unit) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    val colors = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 32.dp, vertical = 28.dp),
    ) {
        val compactLayout = maxHeight < 600.dp
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .verticalScroll(scrollState),
        ) {
            Spacer(Modifier.height(if (compactLayout) 24.dp else 72.dp))
            Text(
                stringResource(R.string.role_selection_title),
                Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(if (compactLayout) 20.dp else 40.dp))
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
            if (selectedRole == UserRole.DRIVER) {
                Spacer(Modifier.height(8.dp))
                DriverUnavailableTooltip()
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    if (selectedRole == UserRole.PASSENGER) onPassenger()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ),
                enabled = selectedRole == UserRole.PASSENGER,
            ) { Text(stringResource(R.string.role_continue), style = MaterialTheme.typography.labelLarge) }
            Spacer(Modifier.height(24.dp))
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
private fun DriverUnavailableTooltip() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Fitur Supir Bus belum tersedia.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun RoleCard(title: String, description: String, avatarRes: Int, selected: Boolean, tags: List<String>, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.primary.copy(alpha = 0.16f) else colors.surface,
            contentColor = colors.onSurface,
        ),
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(avatarRes),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                RoleTags(tags)
            }
        }
    }
}

@Composable
private fun RoleTags(tags: List<String>) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalExtendedColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        RoleTag(tags[0], colors.errorContainer, colors.onErrorContainer)
        RoleTag(tags[1], extended.pendingContainer, extended.onPendingContainer)
    }
    Spacer(Modifier.height(4.dp))
    RoleTag(tags[2], extended.successContainer, extended.onSuccessContainer)
}

@Composable
private fun RoleTag(text: String, containerColor: androidx.compose.ui.graphics.Color, contentColor: androidx.compose.ui.graphics.Color) {
    Surface(color = containerColor, contentColor = contentColor, shape = MaterialTheme.shapes.small) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SignInScreen(
    isDriver: Boolean,
    onSuccess: () -> Unit,
    showPassengerOnboardingProgress: Boolean = false,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.signedIn) { if (state.signedIn) onSuccess() }
    if (isDriver) {
        var phoneNumber by remember { mutableStateOf("") }
        BaseColumn("Login Driver") {
            Text("Masukkan nomor yang sudah terdaftar.")
            OutlinedTextField(phoneNumber, { phoneNumber = it }, Modifier.fillMaxWidth(), label = { Text("Nomor telepon") }, singleLine = true)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { viewModel.signInDriver(phoneNumber) },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.loading) "Memeriksa…" else "Lanjut") }
        }
    } else {
        StudentSsoScreen(
            state = state,
            showProgress = showPassengerOnboardingProgress,
            onLogin = viewModel::signInPassenger,
        )
    }
}

@Composable
private fun StudentSsoScreen(
    state: com.beg.shuttlespot.feature.auth.SignInUiState,
    showProgress: Boolean,
    onLogin: (String, String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 32.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
        ) {
            if (showProgress) {
                StudentOnboardingProgressHeader(StudentOnboardingStep.SSO)
            } else {
                Spacer(Modifier.height(20.dp))
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.outline),
            ) {
                Column(Modifier.padding(horizontal = 28.dp, vertical = 32.dp)) {
                    Text(
                        text = stringResource(R.string.sso_title),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = colors.primaryContainer,
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = stringResource(R.string.sso_email_label),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g name@student.umn.ac.id") },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.sso_password_label),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = stringResource(
                                        if (passwordVisible) R.string.sso_hide_password else R.string.sso_show_password,
                                    ),
                                )
                            }
                        },
                    )
                    state.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = colors.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onLogin(username, password) },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(if (state.loading) "Memeriksa…" else stringResource(R.string.sso_login))
            }
            Spacer(Modifier.height(24.dp))
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        content()
    }
}
