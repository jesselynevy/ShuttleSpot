package com.beg.shuttlespot.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInUiState(val loading: Boolean = false, val error: String? = null, val signedIn: Boolean = false)

@HiltViewModel
class AuthViewModel @Inject constructor(private val sessions: SessionRepository) : ViewModel() {
    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun signInPassenger(email: String) = signIn { sessions.signInPassenger(email) }
    fun signInDriver(phone: String) = signIn { sessions.signInDriver(phone) }

    private fun signIn(block: suspend () -> SignInResult) = viewModelScope.launch {
        _state.value = SignInUiState(loading = true)
        _state.value = when (val result = block()) {
            is SignInResult.Success -> SignInUiState(signedIn = true)
            is SignInResult.Rejected -> SignInUiState(error = result.message)
        }
    }
}
