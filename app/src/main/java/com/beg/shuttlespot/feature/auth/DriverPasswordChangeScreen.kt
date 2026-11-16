package com.beg.shuttlespot.feature.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.components.SimpleScreenColumn

@Composable
fun DriverPasswordChangeScreen(
    onPasswordUpdated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    LaunchedEffect(state.passwordUpdated) {
        if (state.passwordUpdated) onPasswordUpdated()
    }

    SimpleScreenColumn(stringResource(R.string.driver_password_change_title)) {
        Text(stringResource(R.string.driver_password_change_description))
        PasswordField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = stringResource(R.string.driver_temporary_password),
        )
        PasswordField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = stringResource(R.string.auth_new_password),
        )
        PasswordField(
            value = confirmation,
            onValueChange = { confirmation = it },
            label = stringResource(R.string.auth_new_password_confirmation),
        )
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        state.errorRes?.let {
            Text(stringResource(it), color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                viewModel.updateDriverPassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmation = confirmation,
                )
            },
            enabled = !state.loading &&
                currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                confirmation.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(
                    if (state.loading) R.string.auth_saving
                    else R.string.auth_save_password,
                ),
            )
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = stringResource(
                        if (visible) R.string.sso_hide_password
                        else R.string.sso_show_password,
                    ),
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
