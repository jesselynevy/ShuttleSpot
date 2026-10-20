package com.beg.shuttlespot.feature.passenger.home

import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.EtaEstimate
import com.beg.shuttlespot.core.model.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PassengerHomeViewModelTest {
    @Test fun `null trip maps to no active bus`() {
        assertEquals(PassengerTripStatus.NO_ACTIVE_BUS, toPassengerHomeUiState(null).status)
    }

    @Test fun `each trip status receives its Indonesian passenger state`() {
        val expected = mapOf(
            TripStatus.EN_ROUTE to PassengerTripStatus.EN_ROUTE,
            TripStatus.AT_PICKUP to PassengerTripStatus.AT_PICKUP,
            TripStatus.MISSED to PassengerTripStatus.MISSED,
            TripStatus.RIDING to PassengerTripStatus.RIDING,
            TripStatus.COMPLETED to PassengerTripStatus.COMPLETED,
            TripStatus.NO_ACTIVE_BUS to PassengerTripStatus.NO_ACTIVE_BUS
        )
        expected.forEach { (tripStatus, homeStatus) -> assertEquals(homeStatus, toPassengerHomeUiState(snapshot(tripStatus)).status) }
    }

    @Test fun `only en route and at pickup can confirm boarding`() {
        assertTrue(toPassengerHomeUiState(snapshot(TripStatus.EN_ROUTE)).canConfirmBoarding)
        assertTrue(toPassengerHomeUiState(snapshot(TripStatus.AT_PICKUP)).canConfirmBoarding)
        assertFalse(toPassengerHomeUiState(snapshot(TripStatus.RIDING)).canConfirmBoarding)
    }

    private fun snapshot(status: TripStatus) = ActiveTripSnapshot(
        tripId = "trip-1", routeId = "bsd_umn", vehicleLabel = "D 8801 VBN", status = status,
        progress = 1.2f, nextStopName = "Halte UMN", eta = EtaEstimate(5, isStale = true),
        lastUpdatedEpochMillis = 1_700_000_000_000, location = null
    )
}
