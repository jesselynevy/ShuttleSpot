package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.core.model.TripStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryDriverTripRepository @Inject constructor() : DriverTripRepository {
    private val trip = MutableStateFlow<DriverTripSnapshot?>(
        DriverTripSnapshot("trip-bsd-1", "bsd_umn", "D 8801 VBN", TripStatus.NOT_STARTED, null, "Halte BSD", 0, 3, 2)
    )

    override fun observeAssignedTrip(driverProfileId: String): Flow<DriverTripSnapshot?> = trip.asStateFlow()
    override suspend fun startTrip(tripId: String) { trip.value = trip.value?.copy(status = TripStatus.EN_ROUTE, currentStopName = "Halte BSD", nextStopName = "Summarecon Mall Serpong") }
    override suspend fun confirmCurrentStop(tripId: String) { trip.value = trip.value?.let { current -> current.copy(currentStopSequence = (current.currentStopSequence + 1).coerceAtMost(current.totalStops), currentStopName = current.nextStopName, nextStopName = if (current.currentStopSequence + 1 >= current.totalStops) null else "Universitas Multimedia Nusantara") } }
    override suspend fun finishTrip(tripId: String) { trip.value = trip.value?.copy(status = TripStatus.COMPLETED, nextStopName = null) }
}
