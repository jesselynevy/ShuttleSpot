package com.beg.shuttlespot.feature.passenger.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteSelectionUiState(
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = true,
    val selectedRoute: RouteDetails? = null,
    val pickupStopId: String? = null,
    val destinationStopId: String? = null,
    val showReview: Boolean = false,
    val selectionSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val pickupStop: Stop? get() = selectedRoute?.stops?.find { it.id == pickupStopId }
    val destinationStop: Stop? get() = selectedRoute?.stops?.find { it.id == destinationStopId }
    val canReview: Boolean get() = pickupStop != null && destinationStop != null && pickupStop!!.sequence < destinationStop!!.sequence
}

@HiltViewModel
class RouteSelectionViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RouteSelectionUiState())
    val uiState: StateFlow<RouteSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            routeRepository.observeRoutes().collectLatest { routes ->
                _uiState.update { state -> state.copy(routes = routes, isLoading = false) }
                if (_uiState.value.selectedRoute == null && routes.isNotEmpty()) selectRoute(routes.first().id)
            }
        }
        viewModelScope.launch {
            routeRepository.observePrimarySelection().collectLatest { saved ->
                if (saved != null && (
                        _uiState.value.selectedRoute?.route?.id != saved.routeId ||
                            _uiState.value.pickupStopId != saved.pickupStopId ||
                            _uiState.value.destinationStopId != saved.destinationStopId
                    )
                ) {
                    selectRoute(saved.routeId, saved.pickupStopId, saved.destinationStopId)
                }
            }
        }
    }

    fun selectRoute(routeId: String, pickupStopId: String? = null, destinationStopId: String? = null) {
        viewModelScope.launch {
            val details = routeRepository.observeRouteDetails(routeId).first() ?: return@launch
            // Tanpa pilihan tersimpan, pilih halte pertama (jemput) dan terakhir (tujuan) supaya pilihan awal selalu valid.
            val pickup = pickupStopId ?: details.stops.firstOrNull()?.id
            val destination = destinationStopId ?: details.stops.lastOrNull()?.takeIf { it.id != pickup }?.id
            _uiState.update {
                it.copy(selectedRoute = details, pickupStopId = pickup, destinationStopId = destination, showReview = false, selectionSaved = false, errorMessage = null)
            }
        }
    }

    fun selectPickup(stopId: String) {
        val destination = _uiState.value.destinationStop
        val pickup = _uiState.value.selectedRoute?.stops?.find { it.id == stopId }
        _uiState.update {
            it.copy(pickupStopId = stopId, destinationStopId = if (destination != null && pickup != null && destination.sequence <= pickup.sequence) null else it.destinationStopId, errorMessage = null)
        }
    }

    fun selectDestination(stopId: String) {
        val pickup = _uiState.value.pickupStop
        val destination = _uiState.value.selectedRoute?.stops?.find { it.id == stopId }
        _uiState.update {
            it.copy(destinationStopId = stopId, errorMessage = if (pickup != null && destination != null && destination.sequence <= pickup.sequence) "Tujuan harus berada setelah halte jemput." else null)
        }
    }

    fun openReview() {
        _uiState.update { state -> if (state.canReview) state.copy(showReview = true, errorMessage = null) else state.copy(errorMessage = "Pilih halte jemput dan tujuan yang urutannya benar.") }
    }

    fun closeReview() = _uiState.update { it.copy(showReview = false) }

    fun saveSelection() {
        val state = _uiState.value
        val route = state.selectedRoute ?: return
        val pickup = state.pickupStop ?: return
        val destination = state.destinationStop ?: return
        if (!state.canReview) {
            _uiState.update { it.copy(errorMessage = "Tujuan harus berada setelah halte jemput.", showReview = false) }
            return
        }
        viewModelScope.launch {
            runCatching { routeRepository.savePrimaryRoute(route.route.id, pickup.id, destination.id) }
                .onSuccess { _uiState.update { it.copy(showReview = false, selectionSaved = true, errorMessage = null) } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message ?: "Pilihan rute belum dapat disimpan.") } }
        }
    }
}
