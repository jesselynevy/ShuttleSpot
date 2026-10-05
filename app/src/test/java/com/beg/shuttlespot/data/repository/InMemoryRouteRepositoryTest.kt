package com.beg.shuttlespot.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryRouteRepositoryTest {
    @Test(expected = IllegalArgumentException::class)
    fun pickup_must_precede_destination() = runTest {
        InMemoryRouteRepository().savePrimaryRoute("bsd_umn", "bsd_umn_8", "bsd_umn_1")
    }

    @Test
    fun save_primary_route_persists_valid_selection() = runTest {
        val repository = InMemoryRouteRepository()
        repository.savePrimaryRoute("bsd_umn", "bsd_umn_1", "bsd_umn_8")
        val selection = repository.observePrimarySelection().first()
        assertEquals("bsd_umn", selection?.routeId)
        assertEquals("bsd_umn_1", selection?.pickupStopId)
        assertEquals("bsd_umn_8", selection?.destinationStopId)
    }

    @Test
    fun exposes_current_reference_routes_and_timetable_entries() = runTest {
        val repository = InMemoryRouteRepository()
        val routes = repository.observeRoutes().first()
        val grogol = repository.observeRouteDetails("grogol_umn").first()

        assertEquals(8, routes.size)
        assertEquals("05.25–05.47", grogol?.timetableEntries?.first()?.time)
        assertEquals("Grogol", grogol?.stops?.first()?.name)
    }
}
