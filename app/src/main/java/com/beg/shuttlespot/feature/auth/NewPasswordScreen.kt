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
fun NewPasswordScreen(
    onPasswordUpdated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.passwordUpdated) {
        if (state.passwordUpdated) onPasswordUpdated()
    }

    SimpleScreenColumn(stringResource(R.string.auth_new_password_title)) {
        Text(stringResource(R.string.auth_password_requirement))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.auth_new_password)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = stringResource(
                            if (passwordVisible) {
                                R.string.sso_hide_password
                            } else {
                                R.string.sso_show_password
                            },
                        ),
                    )
                }
            },
        )
        OutlinedTextField(
            value = passwordConfirmation,
            onValueChange = { passwordConfirmation = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.auth_new_password_confirmation)) },
            singleLine = true,
            visualTransformation = if (passwordConfirmationVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        passwordConfirmationVisible = !passwordConfirmationVisible
                    },
                ) {
                    Icon(
                        imageVector = if (passwordConfirmationVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = stringResource(
                            if (passwordConfirmationVisible) {
                                R.string.sso_hide_password
                            } else {
                                R.string.sso_show_password
                            },
                        ),
                    )
                }
            },
        )
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.errorRes?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                viewModel.updatePassword(password, passwordConfirmation)
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (state.loading) R.string.auth_saving else R.string.auth_save_password))
        }
    }
}
