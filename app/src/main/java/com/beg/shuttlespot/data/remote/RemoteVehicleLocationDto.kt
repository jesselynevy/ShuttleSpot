package com.beg.shuttlespot.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteVehicleLocationDto(
    val id: String,
    @SerialName("trip_id") val tripId: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("accuracy_meters") val accuracyMeters: Float? = null,
    @SerialName("captured_at") val capturedAt: String,
)


@Serializable
internal data class RemoteTripStatusDto(
    val id: String,
    @SerialName("route_id") val routeId: String,
    val status: String,
)