package com.beg.shuttlespot.feature.driver.trip

// Overview: Defines driver trip view model for the driver trip area of ShuttleSpot.

// Dependencies used by this file.
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.data.location.LocationPublisher
import com.beg.shuttlespot.data.repository.DriverTripRepository
import com.beg.shuttlespot.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
// Defines the driver trip view model type.
class DriverTripViewModel @Inject constructor(
    private val trips: DriverTripRepository,
    private val locationPublisher: LocationPublisher,
    private val sessions: SessionRepository,
) : ViewModel() {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val trip: StateFlow<DriverTripSnapshot?> = sessions.observeSession()
        .flatMapLatest { session ->
            if (session == null) flowOf(null) else trips.observeAssignedTrip(session.profileId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Handles the start trip action.
    fun startTrip() = viewModelScope.launch { trip.value?.let { trips.startTrip(it.tripId); locationPublisher.start(it.tripId) } }
    // Handles the confirm stop action.
    fun confirmStop() = viewModelScope.launch { trip.value?.let { trips.confirmCurrentStop(it.tripId) } }
    // Handles the finish trip action.
    fun finishTrip() = viewModelScope.launch { trip.value?.let { trips.finishTrip(it.tripId); locationPublisher.stop() } }
}
