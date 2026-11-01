package com.beg.shuttlespot.data.remote

// Overview: Defines supabase mappers for the data remote area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.GeoPoint
import com.beg.shuttlespot.core.model.LiveLocation

/** DTOs are intentionally small until the Supabase SDK is added. */
data class RemoteLocationDto(val latitude: Double, val longitude: Double, val capturedAtEpochMillis: Long)

// Handles the remote location dto responsibility.
fun RemoteLocationDto.toDomain() = LiveLocation(latitude, longitude, capturedAtEpochMillis)
// Handles the geo point responsibility.
fun GeoPoint.toStoragePair(): Pair<Double, Double> = latitude to longitude
