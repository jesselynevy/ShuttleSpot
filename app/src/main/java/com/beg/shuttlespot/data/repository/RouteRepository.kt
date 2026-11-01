package com.beg.shuttlespot.data.repository

// Overview: Defines route repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.RouteSelection
import kotlinx.coroutines.flow.Flow

// Flow is Kotlin’s way to send values over time.
interface RouteRepository {
    // Prepares or returns observe routes.
    fun observeRoutes(): Flow<List<Route>>
    // Prepares or returns observe route details.
    fun observeRouteDetails(routeId: String) : Flow<RouteDetails?>
    // Prepares or returns observe primary selection.
    fun observePrimarySelection(): Flow<RouteSelection?>
    // Prepares or returns refresh routes.
    suspend fun refreshRoutes()
    // Handles the save primary route action.
    suspend fun savePrimaryRoute(
        routeId: String,
        pickupStopId: String,
        destinationStopId: String
    )
}
