package com.beg.shuttlespot.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryPassengerTripRepositoryTest {
    @Test fun active_trip_is_available_only_for_its_selected_route() = runTest {
        val repository = InMemoryPassengerTripRepository()

        assertEquals("bsd_umn", repository.observeActiveTrip("bsd_umn").first()?.routeId)
        assertNull(repository.observeActiveTrip("grogol_umn").first())
    }

    @Test fun duplicate_idempotency_key_reuses_existing_intent() = runTest {
        val repository = InMemoryPassengerTripRepository()
        val first = repository.submitBoardingIntent("trip-bsd-1", "bsd_stop", "same-key")
        val retry = repository.submitBoardingIntent("trip-bsd-1", "bsd_stop", "same-key")
        assertEquals(first, retry)
    }

    @Test fun boarding_intent_is_available_only_for_its_selected_trip_and_stop() = runTest {
        val repository = InMemoryPassengerTripRepository()
        repository.submitBoardingIntent("trip-bsd-1", "bsd_umn_1", "key")

        assertEquals("bsd_umn_1", repository.observeBoardingIntent("trip-bsd-1", "bsd_umn_1").first()?.stopId)
        assertNull(repository.observeBoardingIntent("trip-bsd-1", "bsd_umn_2").first())
    }

    @Test fun duplicate_trip_and_stop_with_new_key_reuses_existing_intent() = runTest {
        val repository = InMemoryPassengerTripRepository()
        val first = repository.submitBoardingIntent("trip-bsd-1", "bsd_stop", "initial-key")
        val retry = repository.submitBoardingIntent("trip-bsd-1", "bsd_stop", "retry-key")
        assertEquals(first, retry)
    }
}
