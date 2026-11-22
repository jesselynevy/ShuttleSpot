package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.DriverTripSnapshot

interface DriverTripRepository {
    suspend fun getAssignedTrip(): DriverTripSnapshot?
    suspend fun startTrip(tripId: String): DriverTripSnapshot
    suspend fun advanceStop(tripId: String): DriverTripSnapshot
    suspend fun finishTrip(tripId: String): DriverTripSnapshot
    suspend fun publishLocation(
        tripId: String,
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float?,
        capturedAtEpochMillis: Long,
    )
}

class DriverTripException(message: String) : Exception(message)
