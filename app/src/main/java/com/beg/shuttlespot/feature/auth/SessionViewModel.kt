package com.beg.shuttlespot.feature.auth

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

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data class Ready(val session: AppSession?) : SessionUiState
}

@HiltViewModel
class SessionViewModel @Inject constructor(private val repository: SessionRepository) : ViewModel() {
    val state: StateFlow<SessionUiState> = repository.observeSession()
        .map { SessionUiState.Ready(it) as SessionUiState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionUiState.Loading)

    fun signOut() {
        viewModelScope.launch { repository.signOut() }
    }
}
