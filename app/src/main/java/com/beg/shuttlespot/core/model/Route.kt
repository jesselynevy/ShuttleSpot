package com.beg.shuttlespot.core.model

// Overview: Defines route for the core model area of ShuttleSpot.

data class Route(
    val id: String,
    val name: String
)

// Defines the stop type.
data class Stop(
    val id: String,
    val routeId: String,
    val name: String,
    val sequence: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val scheduledTime: String? = null,
) 

// Defines the timetable entry type.
data class TimetableEntry(
    val period: String,
    val time: String,
    val description: String
)

// Defines the geo point type.
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

// Defines the route details type.
data class RouteDetails(
    val route: Route,
    val stops: List<Stop>,
    val timetableEntries: List<TimetableEntry>,
    val serviceNotice: String? = null,
    /** Jalur jalan (urut) antar halte; kosong = tidak ada, peta menarik garis lurus antar halte. */
    val path: List<GeoPoint> = emptyList()
)

// Defines the route selection type.
data class RouteSelection(
    val routeId: String,
    val pickupStopId: String,
    val destinationStopId: String
)
