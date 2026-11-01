package com.beg.shuttlespot.data.demo

// Overview: Defines demo route data source for the data demo area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.RouteDetails

// Defines the demo route data source type.
interface DemoRouteDataSource {
    // Handles the get routes responsibility.
    suspend fun getRoutes(): List<RouteDetails>
}
