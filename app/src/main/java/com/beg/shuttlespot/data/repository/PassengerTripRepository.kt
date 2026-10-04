package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.BoardingIntent
import kotlinx.coroutines.flow.Flow

interface PassengerTripRepository {
    fun observeActiveTrip(routeId: String): Flow<ActiveTripSnapshot?>
    fun observeBoardingIntent(tripId: String, stopId: String): Flow<BoardingIntent?>
    suspend fun submitBoardingIntent(tripId: String, stopId: String, idempotencyKey: String): BoardingIntent
    suspend fun cancelBoardingIntent(tripId: String, stopId: String)
}
