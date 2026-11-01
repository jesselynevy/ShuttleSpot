package com.beg.shuttlespot.data.repository

// Overview: Defines passenger trip repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.BoardingIntent
import kotlinx.coroutines.flow.Flow

// Defines the passenger trip repository type.
interface PassengerTripRepository {
    // Prepares or returns observe active trip.
    fun observeActiveTrip(routeId: String): Flow<ActiveTripSnapshot?>
    // Prepares or returns observe boarding intent.
    fun observeBoardingIntent(tripId: String, stopId: String): Flow<BoardingIntent?>
    // Handles the submit boarding intent responsibility.
    suspend fun submitBoardingIntent(tripId: String, stopId: String, idempotencyKey: String): BoardingIntent
    // Handles the cancel boarding intent responsibility.
    suspend fun cancelBoardingIntent(tripId: String, stopId: String)
}
