package com.beg.shuttlespot.feature.passenger.livetrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.LiveLocation
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.ui.components.MapMarker
import com.beg.shuttlespot.core.ui.components.MarkerKind
import com.beg.shuttlespot.data.repository.PassengerTripRepository
import com.beg.shuttlespot.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.beg.shuttlespot.core.model.GeoPoint

data class RouteMapUiState(
    val routeName: String = "",
    val stops: List<Stop> = emptyList(),
    val selectedStopId: String? = null,
    val pickupStopId: String? = null,
    val destinationStopId: String? = null,
    val busLocation: LiveLocation? = null,
    /** Jalur jalan rute (urut); kosong -> peta menarik garis lurus antar halte. */
    val path: List<GeoPoint> = emptyList(),
) {
    /** Halte berkoordinat (urut sesuai rute) + marker bus, siap dikirim ke OpenStreetMap. */
    val markers: List<MapMarker>
        get() {
            val stopMarkers = stops.sortedBy { it.sequence }.mapNotNull { s ->
                val lat = s.latitude ?: return@mapNotNull null
                val lng = s.longitude ?: return@mapNotNull null
                MapMarker(
                    id = s.id, latitude = lat, longitude = lng,
                    kind = when (s.id) {
                        pickupStopId -> MarkerKind.Pickup
                        destinationStopId -> MarkerKind.Destination
                        else -> MarkerKind.Stop
                    },
                    title = s.name,
                    schedule = s.scheduledTime,
                )
            }
            val bus = busLocation?.let {
                MapMarker("bus", it.latitude, it.longitude, MarkerKind.Bus, title = "Posisi bus")
            }
            return stopMarkers + listOfNotNull(bus)
        }
}

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

    private val selectedStopId = MutableStateFlow<String?>(null)

    /** Dipanggil saat marker halte diklik (id) atau saat area kosong peta diklik (null). */
    fun selectStop(id: String?) {
        selectedStopId.value = id
    }

    private val routeMapBase: StateFlow<RouteMapUiState> = routeRepository.observePrimarySelection()
        .flatMapLatest { selection ->
            if (selection == null) flowOf(RouteMapUiState())
            else combine(routeRepository.observeRouteDetails(selection.routeId), trip) { details, trip ->
                RouteMapUiState(
                    routeName = details?.route?.name.orEmpty(),
                    stops = details?.stops.orEmpty(),
                    pickupStopId = selection.pickupStopId,
                    destinationStopId = selection.destinationStopId,
                    busLocation = trip?.location,
                    path = details?.path.orEmpty(),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RouteMapUiState())

    val mapState: StateFlow<RouteMapUiState> = combine(routeMapBase, selectedStopId) { base, selected ->
        base.copy(selectedStopId = selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RouteMapUiState())
}
