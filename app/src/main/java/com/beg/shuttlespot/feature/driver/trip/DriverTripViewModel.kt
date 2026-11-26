package com.beg.shuttlespot.feature.driver.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.data.location.LocationPublisher
import com.beg.shuttlespot.data.repository.DriverTripRepository
import com.beg.shuttlespot.data.repository.RouteRepository
import com.beg.shuttlespot.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DriverTripUiState {
    data object Loading : DriverTripUiState
    data object NoAssignment : DriverTripUiState

    data class Ready(
        val trip: DriverTripSnapshot,
        val actionInProgress: Boolean = false,
    ) : DriverTripUiState

    data class Error(val message: String) : DriverTripUiState
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class DriverTripViewModel @Inject constructor(
    private val trips: DriverTripRepository,
    private val locationPublisher: LocationPublisher,
    private val routes: RouteRepository,
    sessions: SessionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<DriverTripUiState>(DriverTripUiState.Loading)
    val state: StateFlow<DriverTripUiState> = _state.asStateFlow()

    // Daftar halte + nama rute untuk trip yang sedang Ready. Dipakai Driver Home untuk
    // mengisi "Progres Rute"
    val routeDetails: StateFlow<RouteDetails?> = state
        .map { (it as? DriverTripUiState.Ready)?.trip?.routeId }
        .distinctUntilChanged()
        .flatMapLatest { routeId ->
            if (routeId == null) flowOf(null) else routes.observeRouteDetails(routeId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    init {
        viewModelScope.launch {
            sessions.observeSession().collectLatest { session ->
                if (session?.role == UserRole.DRIVER && !session.mustChangePassword) {
                    loadTrip()
                } else {
                    _state.value = DriverTripUiState.NoAssignment
                }
            }
        }
    }

    fun retry() {
        viewModelScope.launch { loadTrip() }
    }

    fun startTrip() = mutate { trip ->
        trips.startTrip(trip.tripId).also {
            locationPublisher.start(it.tripId)
        }
    }

    fun advanceStop() = mutate { trip ->
        trips.advanceStop(trip.tripId)
    }

    fun finishTrip() = mutate { trip ->
        trips.finishTrip(trip.tripId).also {
            locationPublisher.stop()
        }
    }

    private suspend fun loadTrip() {
        _state.value = DriverTripUiState.Loading
        _state.value = try {
            trips.getAssignedTrip()
                ?.let(DriverTripUiState::Ready)
                ?: DriverTripUiState.NoAssignment
        } catch (error: Exception) {
            DriverTripUiState.Error(
                error.message ?: "Data perjalanan belum dapat dimuat.",
            )
        }
    }

    private fun mutate(
        operation: suspend (DriverTripSnapshot) -> DriverTripSnapshot,
    ) {
        val current = _state.value as? DriverTripUiState.Ready ?: return
        if (current.actionInProgress) return

        _state.value = current.copy(actionInProgress = true)
        viewModelScope.launch {
            _state.value = try {
                DriverTripUiState.Ready(operation(current.trip))
            } catch (error: Exception) {
                DriverTripUiState.Error(
                    error.message ?: "Perjalanan belum dapat diperbarui.",
                )
            }
        }
    }
}
