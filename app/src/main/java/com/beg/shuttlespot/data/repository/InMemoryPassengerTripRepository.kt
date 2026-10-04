package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.BoardingIntent
import com.beg.shuttlespot.core.model.BoardingIntentStatus
import com.beg.shuttlespot.core.model.EtaEstimate
import com.beg.shuttlespot.core.model.LiveLocation
import com.beg.shuttlespot.core.model.TripStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryPassengerTripRepository @Inject constructor() : PassengerTripRepository {
    private val activeTrip = MutableStateFlow<ActiveTripSnapshot?>(
        ActiveTripSnapshot(
            tripId = "trip-bsd-1", routeId = "bsd_umn", vehicleLabel = "D 8801 VBN",
            status = TripStatus.EN_ROUTE, progress = 0.45f, nextStopName = "Halte Jababeka II",
            eta = EtaEstimate(minutes = 5), lastUpdatedEpochMillis = System.currentTimeMillis(),
            location = LiveLocation(-6.267, 106.628, System.currentTimeMillis())
        )
    )
    private val intent = MutableStateFlow<BoardingIntent?>(null)

    override fun observeActiveTrip(routeId: String): Flow<ActiveTripSnapshot?> = activeTrip.asStateFlow()
    override fun observeBoardingIntent(tripId: String, stopId: String): Flow<BoardingIntent?> = intent.asStateFlow()

    override suspend fun submitBoardingIntent(tripId: String, stopId: String, idempotencyKey: String): BoardingIntent {
        val existing = intent.value
        if (existing?.idempotencyKey == idempotencyKey) return existing
        return BoardingIntent(tripId, stopId, BoardingIntentStatus.CONFIRMED, idempotencyKey)
            .also { intent.value = it }
    }

    override suspend fun cancelBoardingIntent(tripId: String, stopId: String) {
        intent.value = intent.value?.copy(status = BoardingIntentStatus.CANCELLED)
    }
}
