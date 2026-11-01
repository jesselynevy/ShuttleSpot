package com.beg.shuttlespot.feature.auth

// Overview: Defines session view model for the feature auth area of ShuttleSpot.

// Dependencies used by this file.
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Defines the session ui state type.
sealed interface SessionUiState {
    // Defines the loading type.
    data object Loading : SessionUiState
    // Defines the ready type.
    data class Ready(val session: AppSession?) : SessionUiState
}

@HiltViewModel
// Defines the session view model type.
class SessionViewModel @Inject constructor(private val repository: SessionRepository) : ViewModel() {
    val state: StateFlow<SessionUiState> = repository.observeSession()
        .map { SessionUiState.Ready(it) as SessionUiState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionUiState.Loading)

    // Handles the sign out responsibility.
    fun signOut() {
        // Performs this state update asynchronously in the ViewModel scope.
        viewModelScope.launch { repository.signOut() }
    }
}
