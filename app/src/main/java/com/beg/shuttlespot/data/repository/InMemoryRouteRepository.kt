package com.beg.shuttlespot.data.repository

// Overview: Defines in memory route repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.common.Validation
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.RouteSelection
import com.beg.shuttlespot.data.demo.DemoRouteDataSource
import com.beg.shuttlespot.data.demo.StaticDemoRouteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Temporary demo repository. It owns only transient selection state; the route
 * catalogue lives in [DemoRouteDataSource] so Room/remote implementations can
 * reuse the same source during the migration.
 */
@Singleton
// Defines the in memory route repository type.
class InMemoryRouteRepository @Inject constructor(
    private val dataSource: DemoRouteDataSource,
) : RouteRepository {
    constructor() : this(StaticDemoRouteDataSource())
    private val routeDetails = runBlocking { dataSource.getRoutes() }
    private val selectionState = MutableStateFlow<RouteSelection?>(null)

    // Implements framework behavior for observe routes.
    override fun observeRoutes(): Flow<List<Route>> =
        MutableStateFlow(routeDetails.map(RouteDetails::route)).asStateFlow()

    // Implements framework behavior for observe route details.
    override fun observeRouteDetails(routeId: String): Flow<RouteDetails?> =
        MutableStateFlow(routeDetails.firstOrNull { it.route.id == routeId }).asStateFlow()

    // Implements framework behavior for observe primary selection.
    override fun observePrimarySelection(): Flow<RouteSelection?> = selectionState.asStateFlow()

    // Static demo data has no remote refresh. PersistedRouteRepository will own
    // the future cache-refresh behavior.
    override suspend fun refreshRoutes() = Unit

    // Implements framework behavior for save primary route.
    override suspend fun savePrimaryRoute(
        routeId: String,
        pickupStopId: String,
        destinationStopId: String,
    ) {
        val details = routeDetails.firstOrNull { it.route.id == routeId }
            ?: throw IllegalArgumentException("Route tidak ditemukan.")
        val pickup = details.stops.firstOrNull { it.id == pickupStopId }
            ?: throw IllegalArgumentException("Halte jemput tidak ditemukan.")
        val destination = details.stops.firstOrNull { it.id == destinationStopId }
            ?: throw IllegalArgumentException("Tujuan tidak ditemukan.")
        Validation.requireOrderedStops(pickup.sequence, destination.sequence)
        selectionState.value = RouteSelection(routeId, pickupStopId, destinationStopId)
    }
}
