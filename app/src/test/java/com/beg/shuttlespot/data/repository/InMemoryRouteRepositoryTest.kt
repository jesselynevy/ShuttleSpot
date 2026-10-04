package com.beg.shuttlespot.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Test

class InMemoryRouteRepositoryTest {
    @Test(expected = IllegalArgumentException::class)
    fun pickup_must_precede_destination() = runTest {
        InMemoryRouteRepository().savePrimaryRoute("bsd_umn", "umn_stop", "bsd_stop")
    }
}
