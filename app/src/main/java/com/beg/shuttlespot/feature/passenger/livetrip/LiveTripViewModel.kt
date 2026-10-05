package com.beg.shuttlespot.feature.passenger.livetrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.data.repository.PassengerTripRepository
import com.beg.shuttlespot.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LiveTripViewModel @Inject constructor(
    repository: PassengerTripRepository,
    routeRepository: RouteRepository
) : ViewModel() {
    val trip: StateFlow<ActiveTripSnapshot?> = routeRepository.observePrimarySelection()
        .flatMapLatest { selection ->
            selection?.let { repository.observeActiveTrip(it.routeId) } ?: flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
