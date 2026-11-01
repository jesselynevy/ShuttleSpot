package com.beg.shuttlespot.feature.passenger.notification

// Overview: Defines notifications view model for the passenger notification area of ShuttleSpot.

// Dependencies used by this file.
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.AppNotification
import com.beg.shuttlespot.data.repository.NotificationRepository
import com.beg.shuttlespot.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
// Dependencies used by this file.
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Defines the notifications ui state type.
data class NotificationsUiState(val profileId: String? = null, val items: List<AppNotification> = emptyList())

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
// Defines the notifications view model type.
class NotificationsViewModel @Inject constructor(
    private val sessions: SessionRepository,
    private val notifications: NotificationRepository,
) : ViewModel() {
    val state: StateFlow<NotificationsUiState> = sessions.observeSession()
        .flatMapLatest { session ->
            session?.let { profile -> notifications.observeNotifications(profile.profileId)
                .map { NotificationsUiState(profile.profileId, it) } }
                ?: flowOf(NotificationsUiState())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    // Handles the mark read action.
    fun markRead(id: String) = viewModelScope.launch { notifications.markRead(id) }
    // Handles the clear action.
    fun clear() = viewModelScope.launch { state.value.profileId?.let { notifications.clear(it) } }
}
