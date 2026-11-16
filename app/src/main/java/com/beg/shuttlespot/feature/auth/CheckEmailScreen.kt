package com.beg.shuttlespot.feature.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.components.SimpleScreenColumn

@Composable
fun CheckEmailScreen(
    email: String,
    onBackToSignIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SimpleScreenColumn(stringResource(R.string.auth_check_email_title)) {
        Text(stringResource(R.string.auth_confirmation_sent_to))
        Text(email, style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.auth_check_email_description))
        state.messageRes?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.primary) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.errorRes?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { viewModel.resendConfirmation(email) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (state.loading) R.string.auth_sending else R.string.auth_resend_email))
        }
        TextButton(onClick = onBackToSignIn) {
            Text(stringResource(R.string.auth_back_to_login))
        }
    }
}
