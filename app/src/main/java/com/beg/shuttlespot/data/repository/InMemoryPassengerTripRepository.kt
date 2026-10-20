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
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryPassengerTripRepository @Inject constructor() : PassengerTripRepository {
    private val demoUpdatedAtEpochMillis = 1_791_360_000_000L
    private val activeTrip = MutableStateFlow<ActiveTripSnapshot?>(
        ActiveTripSnapshot(
            tripId = "trip-bsd-1", routeId = "bsd_umn", vehicleLabel = "D 8801 VBN",
            status = TripStatus.EN_ROUTE, progress = 0.45f, nextStopName = "Halte Jababeka II",
            eta = EtaEstimate(minutes = 5), lastUpdatedEpochMillis = demoUpdatedAtEpochMillis,
            location = LiveLocation(-6.264968, 106.654605, demoUpdatedAtEpochMillis)
        )
    )
    private val intent = MutableStateFlow<BoardingIntent?>(null)

    override fun observeActiveTrip(routeId: String): Flow<ActiveTripSnapshot?> =
        activeTrip.map { trip -> trip?.takeIf { it.routeId == routeId } }
    override fun observeBoardingIntent(tripId: String, stopId: String): Flow<BoardingIntent?> =
        intent.map { current -> current?.takeIf { it.tripId == tripId && it.stopId == stopId } }

    override suspend fun submitBoardingIntent(tripId: String, stopId: String, idempotencyKey: String): BoardingIntent {
        val existing = intent.value
        // One passenger can have only one intent for a trip and pickup stop. This makes
        // retries safe even if a caller creates a new idempotency key.
        if (existing?.tripId == tripId && existing.stopId == stopId) return existing
        return BoardingIntent(tripId, stopId, BoardingIntentStatus.CONFIRMED, idempotencyKey)
            .also { intent.value = it }
    }

    override suspend fun cancelBoardingIntent(tripId: String, stopId: String) {
        if (intent.value?.tripId == tripId && intent.value?.stopId == stopId) {
            intent.value = intent.value?.copy(status = BoardingIntentStatus.CANCELLED)
        }
    }
}
