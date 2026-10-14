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
    val longitude: Double? = null,
    val scheduledTime: String? = null
)

data class TimetableEntry(
    val period: String,
    val time: String,
    val description: String
)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class RouteDetails(
    val route: Route,
    val stops: List<Stop>,
    val timetableEntries: List<TimetableEntry>,
    val serviceNotice: String? = null,
    /** Jalur jalan (urut) antar halte; kosong = tidak ada, peta menarik garis lurus antar halte. */
    val path: List<GeoPoint> = emptyList()
)

data class RouteSelection(
    val routeId: String,
    val pickupStopId: String,
    val destinationStopId: String
)
