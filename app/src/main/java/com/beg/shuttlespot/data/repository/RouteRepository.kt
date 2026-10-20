package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.RouteSelection
import kotlinx.coroutines.flow.Flow

// Flow is Kotlin’s way to send values over time.
interface RouteRepository {
    fun observeRoutes(): Flow<List<Route>>
    fun observeRouteDetails(routeId: String) : Flow<RouteDetails?>
    fun observePrimarySelection(): Flow<RouteSelection?>
    suspend fun refreshRoutes()
    suspend fun savePrimaryRoute(
        routeId: String,
        pickupStopId: String,
        destinationStopId: String
    )
}
