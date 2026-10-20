package com.beg.shuttlespot.feature.passenger.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.data.repository.PassengerTripRepository
import com.beg.shuttlespot.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class PassengerHomeViewModel @Inject constructor(
    private val repository: PassengerTripRepository,
    routeRepository: RouteRepository
) : ViewModel() {
    val state: StateFlow<PassengerHomeUiState> = routeRepository.observePrimarySelection()
        .flatMapLatest { selection ->
            selection?.let { repository.observeActiveTrip(it.routeId) } ?: flowOf(null)
        }
        .map { snapshot -> toPassengerHomeUiState(snapshot).copy(isLoading = false) }
        .catch { emit(PassengerHomeUiState(PassengerTripStatus.NO_ACTIVE_BUS, isLoading = false, errorMessage = "Status bus belum dapat dimuat.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PassengerHomeUiState(PassengerTripStatus.NO_ACTIVE_BUS, isLoading = true))
}

data class PassengerHomeUiState(
    val status: PassengerTripStatus,
    val vehicleLabel: String? = null,
    val nextStopName: String? = null,
    val etaMinutes: Int? = null,
    val progress: Float = 0f,
    val lastUpdatedEpochMillis: Long? = null,
    val isStale: Boolean = false,
    val canConfirmBoarding: Boolean = false,
    val canViewLiveTrip: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class PassengerTripStatus(val label: String) {
    NO_ACTIVE_BUS("Tidak ada perjalanan aktif"),
    EN_ROUTE("Dalam perjalanan"),
    AT_PICKUP("Tiba di halte jemput"),
    MISSED("Bus sudah lewat"),
    RIDING("Sedang dalam perjalanan"),
    COMPLETED("Perjalanan selesai"),
    NOT_STARTED("Belum beroperasi")
}

internal fun toPassengerHomeUiState(snapshot: ActiveTripSnapshot?): PassengerHomeUiState {
    if (snapshot == null) return PassengerHomeUiState(status = PassengerTripStatus.NO_ACTIVE_BUS)

    val status = when (snapshot.status) {
        TripStatus.NO_ACTIVE_BUS -> PassengerTripStatus.NO_ACTIVE_BUS
        TripStatus.EN_ROUTE -> PassengerTripStatus.EN_ROUTE
        TripStatus.AT_PICKUP -> PassengerTripStatus.AT_PICKUP
        TripStatus.MISSED -> PassengerTripStatus.MISSED
        TripStatus.RIDING -> PassengerTripStatus.RIDING
        TripStatus.COMPLETED -> PassengerTripStatus.COMPLETED
        TripStatus.NOT_STARTED -> PassengerTripStatus.NOT_STARTED
    }
    val isActive = status == PassengerTripStatus.EN_ROUTE || status == PassengerTripStatus.AT_PICKUP || status == PassengerTripStatus.RIDING
    return PassengerHomeUiState(
        status = status,
        vehicleLabel = snapshot.vehicleLabel,
        nextStopName = snapshot.nextStopName,
        etaMinutes = snapshot.eta.minutes,
        progress = snapshot.progress.coerceIn(0f, 1f),
        lastUpdatedEpochMillis = snapshot.lastUpdatedEpochMillis,
        isStale = snapshot.eta.isStale,
        canConfirmBoarding = status == PassengerTripStatus.EN_ROUTE || status == PassengerTripStatus.AT_PICKUP,
        canViewLiveTrip = isActive
    )
}
