package com.beg.shuttlespot.feature.driver.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.data.location.LocationPublisher
import com.beg.shuttlespot.data.repository.DriverTripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverTripViewModel @Inject constructor(
    private val trips: DriverTripRepository,
    private val locationPublisher: LocationPublisher
) : ViewModel() {
    val trip: StateFlow<DriverTripSnapshot?> = trips.observeAssignedTrip("driver-demo")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun startTrip() = viewModelScope.launch { trip.value?.let { trips.startTrip(it.tripId); locationPublisher.start(it.tripId) } }
    fun confirmStop() = viewModelScope.launch { trip.value?.let { trips.confirmCurrentStop(it.tripId) } }
    fun finishTrip() = viewModelScope.launch { trip.value?.let { trips.finishTrip(it.tripId); locationPublisher.stop() } }
}
