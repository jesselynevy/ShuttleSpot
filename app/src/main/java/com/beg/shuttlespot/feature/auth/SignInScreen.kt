package com.beg.shuttlespot.feature.auth

// Overview: Defines sign in screen for the feature auth area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.components.StudentOnboardingProgressHeader
import com.beg.shuttlespot.core.ui.components.StudentOnboardingStep
import com.beg.shuttlespot.core.ui.components.SimpleScreenColumn

@Composable
// Renders the sign in screen UI section.
fun SignInScreen(
    isDriver: Boolean,
    accountAlreadyRegistered: Boolean = false,
    onSuccess: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    showPassengerOnboardingProgress: Boolean = false,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.signedIn) { if (state.signedIn) onSuccess() }
    if (isDriver) {
        DriverSignInContent(state, viewModel::signInDriver)
    } else {
        PassengerSignInContent(
            state = state,
            showProgress = showPassengerOnboardingProgress,
            onLogin = viewModel::signInPassenger,
            onCreateAccount = onCreateAccount,
            onForgotPassword = onForgotPassword,
            accountAlreadyRegistered = accountAlreadyRegistered,
        )
    }
}

@Composable
// Renders the driver sign in content UI section.
private fun DriverSignInContent(state: AuthUiState, onLogin: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    SimpleScreenColumn("Login Driver") {
        Text("Masukkan nomor yang sudah terdaftar.")
        OutlinedTextField(phoneNumber, { phoneNumber = it }, Modifier.fillMaxWidth(), label = { Text("Nomor telepon") }, singleLine = true)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { onLogin(phoneNumber) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.loading) "Memeriksa…" else "Lanjut")
        }
    }
}

@Composable
// Renders the passenger sign in content UI section.
private fun PassengerSignInContent(
    state: AuthUiState,
    showProgress: Boolean,
    onLogin: (String, String) -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    accountAlreadyRegistered: Boolean,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 32.dp),
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight)) {
            if (showProgress) StudentOnboardingProgressHeader(StudentOnboardingStep.SSO) else Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.outline),
            ) {
                Column(Modifier.padding(horizontal = 28.dp, vertical = 32.dp)) {
                    Text(stringResource(R.string.sso_title), Modifier.align(Alignment.CenterHorizontally), color = colors.primaryContainer, style = MaterialTheme.typography.displaySmall)
                    if (accountAlreadyRegistered) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.auth_account_already_registered),
                            color = colors.primary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(stringResource(R.string.sso_email_label), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(value = username, onValueChange = { username = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g name@student.umn.ac.id") }, singleLine = true)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.sso_password_label), style = MaterialTheme.typography.titleSmall)
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
                                    contentDescription = stringResource(if (passwordVisible) R.string.sso_hide_password else R.string.sso_show_password),
                                )
                            }
                        },
                    )
                    state.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = colors.error, style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(
                        onClick = onForgotPassword,
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(stringResource(R.string.auth_forgot_password_action))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.weight(1f))
            Button(onClick = { onLogin(username, password) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(if (state.loading) "Memeriksa…" else stringResource(R.string.sso_login))
            }
            TextButton(
                onClick = onCreateAccount,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.auth_create_student_account))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
