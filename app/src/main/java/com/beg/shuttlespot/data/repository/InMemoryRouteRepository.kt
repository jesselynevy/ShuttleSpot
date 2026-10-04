package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.RouteSelection
import com.beg.shuttlespot.core.model.Stop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton //object u create once and use it everywhere
class InMemoryRouteRepository @Inject constructor(): RouteRepository{
    private val routes = listOf(
        Route(id = "bsd_umn", name = "BSD ke UMN"),
        Route(id = "umn_bsd", name = "UMN ke BSD")
    )

    private val routeDetails = listOf(
        RouteDetails(
            route = routes[0],
            stops = listOf(
                Stop("bsd_stop", "bsd_umn", "Halte BSD", 1, -6.3000, 106.6500),
                Stop("summarecon_stop", "bsd_umn", "Summarecon Mall Serpong", 2, -6.2400, 106.6300),
                Stop(
                    "umn_stop",
                    "bsd_umn",
                    "Universitas Multimedia Nusantara",
                    3,
                    -6.2561,
                    106.6186
                )
            ),
            departureTimes = listOf("07:00", "09:00", "11:00", "13:00", "15:00")
        ),
        RouteDetails(
            route = routes[1],
            stops = listOf(
                Stop("umn_stop_return", "umn_bsd", "Universitas Multimedia Nusantara", 1, -6.2561, 106.6186),
                Stop("summarecon_stop_return", "umn_bsd", "Summarecon Mall Serpong", 2, -6.2400, 106.6300),
                Stop("bsd_stop_return", "umn_bsd", "Halte BSD", 3, -6.3000, 106.6500)
            ),
            departureTimes = listOf("08:00", "10:00", "12:00", "14:00", "16:00")
        )
    )

    private val routeState = MutableStateFlow(routes)
    private val selectionState = MutableStateFlow<RouteSelection?>(null)

    override fun observeRoutes(): Flow<List<Route>> = routeState.asStateFlow()

    override fun observeRouteDetails(routeId: String): Flow<RouteDetails?> {
        return MutableStateFlow(routeDetails.find{it.route.id == routeId})
    }

    override fun observePrimarySelection(): Flow<RouteSelection?> {
        return selectionState.asStateFlow()
    }

    override suspend fun refreshRoutes() = Unit

    override suspend fun savePrimaryRoute(
        routeId: String,
        pickupStopId: String,
        destinationStopId: String
    ) {
        val details = routeDetails.firstOrNull { it.route.id == routeId }
            ?: throw IllegalArgumentException("Route tidak ditemukan.")
        val pickup = details.stops.firstOrNull { it.id == pickupStopId }
            ?: throw IllegalArgumentException("Halte jemput tidak ditemukan.")
        val destination = details.stops.firstOrNull { it.id == destinationStopId }
            ?: throw IllegalArgumentException("Tujuan tidak ditemukan.")
        require(pickup.sequence < destination.sequence) { "Tujuan harus berada setelah halte jemput." }
        selectionState.value = RouteSelection(
            routeId = routeId,
            pickupStopId = pickupStopId,
            destinationStopId = destinationStopId
        )
    }
}

/*
MutableStateFlow = private writable state.
StateFlow = public read-only state.
asStateFlow() = safely expose the read-only version.
*/
