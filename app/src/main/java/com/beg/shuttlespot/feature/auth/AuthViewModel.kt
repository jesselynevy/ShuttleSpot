package com.beg.shuttlespot.feature.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.common.Validation
import com.beg.shuttlespot.core.model.AuthActionResult
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.SignUpResult
import com.beg.shuttlespot.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    @param:StringRes val errorRes: Int? = null,
    val signedIn: Boolean = false,
    val confirmationEmail: String? = null,
    val accountAlreadyRegistered: Boolean = false,
    val resetEmailSent: Boolean = false,
    val passwordUpdated: Boolean = false,
    @param:StringRes val messageRes: Int? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessions: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun signUpPassenger(
        displayName: String,
        email: String,
        password: String,
        passwordConfirmation: String,
    ) {
        if (password != passwordConfirmation) {
            _state.value = AuthUiState(
                errorRes = R.string.auth_password_confirmation_mismatch,
            )
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)

            _state.value = when (
                val result = sessions.signUpPassenger(
                    displayName = displayName,
                    email = email,
                    password = password,
                )
            ) {
                is SignUpResult.ConfirmationRequired ->
                    AuthUiState(
                        confirmationEmail = result.email,
                    )

                SignUpResult.AlreadyRegistered ->
                    AuthUiState(accountAlreadyRegistered = true)

                is SignUpResult.Rejected ->
                    AuthUiState(error = result.message)
            }
        }
    }

    fun signInPassenger(
        email: String,
        password: String,
    ) = signIn { sessions.signInPassenger(email, password) }

    fun signInDriver(phone: String) = signIn { sessions.signInDriver(phone) }

    fun resendConfirmation(email: String) {
        runAction(
            successState = {
                AuthUiState(
                    confirmationEmail = email,
                    messageRes = R.string.auth_confirmation_resent,
                )
            },
        ) {
            sessions.resendConfirmation(email)
        }
    }

    fun sendPasswordReset(email: String) {
        val normalizedEmail = email.trim().lowercase()
        if (!Validation.isStudentEmail(normalizedEmail)) {
            _state.value = AuthUiState(
                errorRes = R.string.auth_student_email_required,
            )
            return
        }

        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            sessions.sendPasswordReset(email)

            // Always use the same response so the UI never reveals whether an
            // account exists for this valid student address.
            _state.value = AuthUiState(
                resetEmailSent = true,
                messageRes = R.string.auth_reset_email_generic,
            )
        }
    }

    fun updatePassword(
        newPassword: String,
        confirmation: String,
    ) {
        if (newPassword != confirmation) {
            _state.value = AuthUiState(
                errorRes = R.string.auth_password_confirmation_mismatch,
            )
            return
        }

        runAction(
            successState = {
                AuthUiState(
                    passwordUpdated = true,
                    messageRes = R.string.auth_password_updated,
                )
            },
        ) {
            sessions.updatePassword(newPassword)
        }
    }

    fun clearTransientState() {
        _state.value = AuthUiState()
    }

    private fun signIn(block: suspend () -> SignInResult) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            _state.value = when (val result = block()) {
                is SignInResult.Success -> AuthUiState(signedIn = true)
                is SignInResult.Rejected -> AuthUiState(error = result.message)
            }
        }
    }

    private fun runAction(
        successState: () -> AuthUiState,
        block: suspend () -> AuthActionResult,
    ) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)

            _state.value = when (val result = block()) {
                AuthActionResult.Success -> successState()

                is AuthActionResult.Rejected ->
                    AuthUiState(error = result.message)
            }
        }
    }
}
