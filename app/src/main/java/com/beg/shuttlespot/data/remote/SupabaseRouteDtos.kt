package com.beg.shuttlespot.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteRouteDto(
    val id: String,
    val name: String,
    @SerialName("service_notice")
    val serviceNotice: String? = null,
)

@Serializable
internal data class RemoteStopDto(
    val id: String,
    @SerialName("route_id")
    val routeId: String,
    val name: String,
    val sequence: Int,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
internal data class RemoteRoutePointDto(
    @SerialName("route_id")
    val routeId: String,
    val sequence: Int,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
internal data class RemoteTimetableEntryDto(
    @SerialName("route_id")
    val routeId: String,
    val sequence: Int,
    val period: String,
    @SerialName("departure_time")
    val departureTime: String,
    val description: String,
)