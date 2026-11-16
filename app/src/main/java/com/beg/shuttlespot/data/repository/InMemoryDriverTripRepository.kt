package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.core.model.TripLifecycle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryDriverTripRepository @Inject constructor() : DriverTripRepository {
    private var trip: DriverTripSnapshot? = DriverTripSnapshot(
        tripId = "trip-bsd-1",
        routeId = "bsd_umn",
        vehicleLabel = "D 8801 VBN",
        status = TripLifecycle.NOT_STARTED,
        currentStopName = null,
        nextStopName = "Halte BSD",
        currentStopSequence = 0,
        totalStops = 3,
        waitingPassengerCount = 2,
    )

    override suspend fun getAssignedTrip(): DriverTripSnapshot? = trip

    override suspend fun startTrip(tripId: String): DriverTripSnapshot = update {
        it.copy(
            status = TripLifecycle.ACTIVE,
            currentStopName = "Halte BSD",
            nextStopName = "Summarecon Mall Serpong",
        )
    }

    override suspend fun advanceStop(tripId: String): DriverTripSnapshot = update { current ->
        val nextSequence = (current.currentStopSequence + 1)
            .coerceAtMost(current.totalStops)
        current.copy(
            currentStopSequence = nextSequence,
            currentStopName = current.nextStopName,
            nextStopName = if (nextSequence >= current.totalStops) {
                null
            } else {
                "Universitas Multimedia Nusantara"
            },
        )
    }

    override suspend fun finishTrip(tripId: String): DriverTripSnapshot = update {
        it.copy(status = TripLifecycle.COMPLETED, nextStopName = null)
    }

    private fun update(
        block: (DriverTripSnapshot) -> DriverTripSnapshot,
    ): DriverTripSnapshot {
        val updated = block(requireNotNull(trip))
        trip = updated
        return updated
    }
}
