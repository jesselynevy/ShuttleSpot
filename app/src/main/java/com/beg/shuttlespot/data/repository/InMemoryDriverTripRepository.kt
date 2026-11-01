package com.beg.shuttlespot.data.repository

// Overview: Defines in memory driver trip repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.core.model.TripStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// Defines the in memory driver trip repository type.
class InMemoryDriverTripRepository @Inject constructor() : DriverTripRepository {
    private val trip = MutableStateFlow<DriverTripSnapshot?>(
        DriverTripSnapshot("trip-bsd-1", "bsd_umn", "D 8801 VBN", TripStatus.NOT_STARTED, null, "Halte BSD", 0, 3, 2)
    )

    // Implements framework behavior for observe assigned trip.
    override fun observeAssignedTrip(driverProfileId: String): Flow<DriverTripSnapshot?> = trip.asStateFlow()
    // Implements framework behavior for start trip.
    override suspend fun startTrip(tripId: String) { trip.value = trip.value?.copy(status = TripStatus.EN_ROUTE, currentStopName = "Halte BSD", nextStopName = "Summarecon Mall Serpong") }
    // Implements framework behavior for confirm current stop.
    override suspend fun confirmCurrentStop(tripId: String) { trip.value = trip.value?.let { current -> current.copy(currentStopSequence = (current.currentStopSequence + 1).coerceAtMost(current.totalStops), currentStopName = current.nextStopName, nextStopName = if (current.currentStopSequence + 1 >= current.totalStops) null else "Universitas Multimedia Nusantara") } }
    // Implements framework behavior for finish trip.
    override suspend fun finishTrip(tripId: String) { trip.value = trip.value?.copy(status = TripStatus.COMPLETED, nextStopName = null) }
}
