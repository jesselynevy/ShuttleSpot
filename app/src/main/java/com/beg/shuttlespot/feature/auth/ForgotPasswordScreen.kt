package com.beg.shuttlespot.feature.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.components.SimpleScreenColumn

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }

    SimpleScreenColumn(stringResource(R.string.auth_forgot_password_title)) {
        Text(stringResource(R.string.auth_forgot_password_description))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.auth_student_email)) },
            singleLine = true,
        )
        state.messageRes?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.primary) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.errorRes?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { viewModel.sendPasswordReset(email) },
            enabled = !state.loading && !state.resetEmailSent,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (state.loading) R.string.auth_sending else R.string.auth_send_reset_link))
        }
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.auth_back))
        }
    }
}
