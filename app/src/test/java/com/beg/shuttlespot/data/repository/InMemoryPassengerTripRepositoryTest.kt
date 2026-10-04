package com.beg.shuttlespot.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryPassengerTripRepositoryTest {
    @Test fun duplicate_idempotency_key_reuses_existing_intent() = runTest {
        val repository = InMemoryPassengerTripRepository()
        val first = repository.submitBoardingIntent("trip-bsd-1", "bsd_stop", "same-key")
        val retry = repository.submitBoardingIntent("trip-bsd-1", "bsd_stop", "same-key")
        assertEquals(first, retry)
    }
}
