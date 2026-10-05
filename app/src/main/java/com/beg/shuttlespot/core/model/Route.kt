package com.beg.shuttlespot.core.model

data class Route(
    val id: String,
    val name: String
)

data class Stop(
    val id: String,
    val routeId: String,
    val name: String,
    val sequence: Int,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class TimetableEntry(
    val period: String,
    val time: String,
    val description: String
)

data class RouteDetails(
    val route: Route,
    val stops: List<Stop>,
    val timetableEntries: List<TimetableEntry>,
    val serviceNotice: String? = null
)

data class RouteSelection(
    val routeId: String,
    val pickupStopId: String,
    val destinationStopId: String
)
