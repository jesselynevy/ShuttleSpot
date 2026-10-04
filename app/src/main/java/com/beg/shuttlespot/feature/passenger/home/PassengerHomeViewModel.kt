package com.beg.shuttlespot.feature.passenger.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.data.repository.PassengerTripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PassengerHomeViewModel @Inject constructor(repository: PassengerTripRepository) : ViewModel() {
    val trip: StateFlow<ActiveTripSnapshot?> = repository.observeActiveTrip("bsd_umn")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
