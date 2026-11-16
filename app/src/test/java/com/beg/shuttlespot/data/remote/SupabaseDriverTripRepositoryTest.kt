package com.beg.shuttlespot.data.remote

import com.beg.shuttlespot.core.model.TripLifecycle
import org.junit.Assert.assertEquals
import org.junit.Test

class SupabaseDriverTripRepositoryTest {
    @Test
    fun `normalized phone maps to private driver email alias`() {
        assertEquals(
            "driver.628123456789@shuttlespot.internal",
            driverAliasForPhone("+628123456789"),
        )
    }

    @Test
    fun `remote active trip maps to driver lifecycle`() {
        val snapshot = RemoteDriverTripDto(
            tripId = "trip-1",
            routeId = "route-1",
            vehicleLabel = "Bus 01",
            status = "active",
            currentStopName = "Halte A",
            nextStopName = "Halte B",
            currentStopSequence = 1,
            totalStops = 3,
            waitingPassengerCount = 2,
        ).toDomain()

        assertEquals(TripLifecycle.ACTIVE, snapshot.status)
        assertEquals(2, snapshot.waitingPassengerCount)
    }

    @Test
    fun `driver backend details are converted to safe messages`() {
        val message = IllegalStateException(
            "Assigned trip not found; SQL state 42501",
        ).toDriverTripMessage()

        assertEquals("Trip ini tidak ditugaskan kepada Anda.", message)
    }
}
