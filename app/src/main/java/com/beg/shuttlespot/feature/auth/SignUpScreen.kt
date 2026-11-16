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
import androidx.compose.material3.TextButton
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
fun SignUpScreen(
    onCheckEmail: (String) -> Unit,
    onAccountAlreadyRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.confirmationEmail) {
        state.confirmationEmail?.let(onCheckEmail)
    }

    LaunchedEffect(state.accountAlreadyRegistered) {
        if (state.accountAlreadyRegistered) {
            onAccountAlreadyRegistered()
        }
    }

    SimpleScreenColumn(stringResource(R.string.auth_sign_up_title)) {
        Text(stringResource(R.string.auth_sign_up_description))
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.auth_display_name)) },
            singleLine = true,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.auth_student_email)) },
            singleLine = true,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.auth_password)) },
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
            label = { Text(stringResource(R.string.auth_password_confirmation)) },
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
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        state.errorRes?.let {
            Text(stringResource(it), color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                viewModel.signUpPassenger(
                    displayName = displayName,
                    email = email,
                    password = password,
                    passwordConfirmation = passwordConfirmation,
                )
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (state.loading) R.string.auth_registering else R.string.auth_register))
        }
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.auth_back_to_login))
        }
    }
}
