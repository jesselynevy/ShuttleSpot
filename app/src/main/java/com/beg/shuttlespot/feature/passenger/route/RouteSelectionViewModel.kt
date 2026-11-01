package com.beg.shuttlespot.feature.passenger.route

// Overview: Defines route selection view model for the passenger route area of ShuttleSpot.

// Dependencies used by this file.
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.RouteSelection
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.model.TimetableEntry
import com.beg.shuttlespot.data.repository.RouteRepository
// Dependencies used by this file.
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
// Dependencies used by this file.
import javax.inject.Inject

// Defines the route selection ui state type.
data class RouteSelectionUiState(
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = true,
    val selectedRoute: RouteDetails? = null,
    val pickupStopId: String? = null,
    val destinationStopId: String? = null,
    val showTimetable: Boolean = false,
    val selectionSaved: Boolean = false,
    val hasPrimarySelection: Boolean = false,
    val errorMessage: String? = null
) {
    val pickupStop: Stop? get() = selectedRoute?.stops?.find { it.id == pickupStopId }
    val destinationStop: Stop? get() = selectedRoute?.stops?.find { it.id == destinationStopId }
    val canReview: Boolean get() = pickupStop != null && destinationStop != null && pickupStop!!.sequence < destinationStop!!.sequence
    val timetableSummary: TimetableSummary? get() = selectedRoute?.let { route ->
        pickupStop?.let { pickup ->
            destinationStop?.let { destination ->
                TimetableSummary(
                    pickupTime = route.timetableEntries.timeFor(pickup.name, isPickup = true),
                    destinationTime = route.timetableEntries.timeFor(destination.name, isPickup = false),
                )
            }
        }
    }
}

// Defines the timetable summary type.
data class TimetableSummary(
    val pickupTime: String?,
    val destinationTime: String?,
)

// Handles the list responsibility.
private fun List<TimetableEntry>.timeFor(stopName: String, isPickup: Boolean): String? {
    val matchingEntries = filter { entry -> entry.description.contains(stopName, ignoreCase = true) }
    val entry = (if (isPickup) matchingEntries.firstOrNull() else matchingEntries.lastOrNull()) ?: return null
    val endpoints = entry.time.split('–', '-', limit = 2).map(String::trim)
    return (if (isPickup) endpoints.firstOrNull() else endpoints.lastOrNull())?.takeIf(String::isNotBlank)
}

@HiltViewModel
// Defines the route selection view model type.
class RouteSelectionViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RouteSelectionUiState())
    val uiState: StateFlow<RouteSelectionUiState> = _uiState.asStateFlow()
    private var primarySelection: RouteSelection? = null

    // Initializes the component state.
    init {
        // Performs this state update asynchronously in the ViewModel scope.
        viewModelScope.launch {
            runCatching { routeRepository.refreshRoutes() }
                .onFailure { error -> _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Rute belum dapat diperbarui.") } }
        }
        viewModelScope.launch {
            routeRepository.observeRoutes().collectLatest { routes ->
                _uiState.update { state -> state.copy(routes = routes, isLoading = false) }
                if (_uiState.value.selectedRoute == null && routes.isNotEmpty()) selectRoute(routes.first().id)
            }
        }
        // Performs this state update asynchronously in the ViewModel scope.
        viewModelScope.launch {
            routeRepository.observePrimarySelection().collectLatest { saved ->
                primarySelection = saved
                if (saved != null && (
                        _uiState.value.selectedRoute?.route?.id != saved.routeId ||
                            _uiState.value.pickupStopId != saved.pickupStopId ||
                            _uiState.value.destinationStopId != saved.destinationStopId
                    )
                ) {
                    selectRoute(saved.routeId, saved.pickupStopId, saved.destinationStopId)
                }
                _uiState.update { it.copy(hasPrimarySelection = saved != null) }
            }
        }
    }

    // Handles the select route action.
    fun selectRoute(routeId: String, pickupStopId: String? = null, destinationStopId: String? = null, useDefaults: Boolean = true) {
        if (!useDefaults) {
            _uiState.update { it.copy(selectedRoute = null, pickupStopId = null, destinationStopId = null, errorMessage = null) }
        }
        // Performs this state update asynchronously in the ViewModel scope.
        viewModelScope.launch {
            val details = routeRepository.observeRouteDetails(routeId).first() ?: return@launch
            // Tanpa pilihan tersimpan, pilih halte pertama (jemput) dan terakhir (tujuan) supaya pilihan awal selalu valid.
            val pickup = pickupStopId ?: if (useDefaults) details.stops.firstOrNull()?.id else null
            val destination = destinationStopId ?: if (useDefaults) details.stops.lastOrNull()?.takeIf { it.id != pickup }?.id else null
            _uiState.update {
                it.copy(selectedRoute = details, pickupStopId = pickup, destinationStopId = destination, showTimetable = false, selectionSaved = false, errorMessage = null)
            }
        }
    }

    // Handles the select onboarding stop action.
    fun selectOnboardingStop(stopId: String) {
        val state = _uiState.value
        if (state.pickupStop == null || state.destinationStop != null) {
            _uiState.update { it.copy(pickupStopId = stopId, destinationStopId = null, errorMessage = null) }
        } else {
            selectDestination(stopId)
        }
    }

    // Handles the select pickup action.
    fun selectPickup(stopId: String) {
        val destination = _uiState.value.destinationStop
        val pickup = _uiState.value.selectedRoute?.stops?.find { it.id == stopId }
        _uiState.update {
            it.copy(pickupStopId = stopId, destinationStopId = if (destination != null && pickup != null && destination.sequence <= pickup.sequence) null else it.destinationStopId, errorMessage = null)
        }
    }

    // Handles the select destination action.
    fun selectDestination(stopId: String) {
        val pickup = _uiState.value.pickupStop
        val destination = _uiState.value.selectedRoute?.stops?.find { it.id == stopId }
        _uiState.update {
            it.copy(destinationStopId = stopId, errorMessage = if (pickup != null && destination != null && destination.sequence <= pickup.sequence) "Tujuan harus berada setelah halte jemput." else null)
        }
    }

    // Handles the open timetable responsibility.
    fun openTimetable() {
        _uiState.update { state ->
            if (state.canReview) state.copy(showTimetable = true, errorMessage = null)
            else state.copy(errorMessage = "Pilih halte jemput dan tujuan yang urutannya benar.")
        }
    }

    // Handles the close timetable responsibility.
    fun closeTimetable() = _uiState.update { it.copy(showTimetable = false) }

    // Handles the begin route change responsibility.
    fun beginRouteChange() {
        _uiState.update { it.copy(selectionSaved = false, showTimetable = false, errorMessage = null) }
    }

    /** Drops an unfinished replacement so the next Route-tab visit shows the saved primary route. */
    fun discardUnconfirmedRouteChange() {
        val saved = primarySelection ?: return
        selectRoute(
            routeId = saved.routeId,
            pickupStopId = saved.pickupStopId,
            destinationStopId = saved.destinationStopId,
            useDefaults = false,
        )
    }

    // Handles the save selection action.
    fun saveSelection() {
        val state = _uiState.value
        val route = state.selectedRoute ?: return
        val pickup = state.pickupStop ?: return
        val destination = state.destinationStop ?: return
        if (!state.canReview) {
            _uiState.update { it.copy(errorMessage = "Tujuan harus berada setelah halte jemput.") }
            return
        }
        // Performs this state update asynchronously in the ViewModel scope.
        viewModelScope.launch {
            runCatching { routeRepository.savePrimaryRoute(route.route.id, pickup.id, destination.id) }
                .onSuccess { _uiState.update { it.copy(showTimetable = false, selectionSaved = true, errorMessage = null) } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message ?: "Pilihan rute belum dapat disimpan.") } }
        }
    }
}
