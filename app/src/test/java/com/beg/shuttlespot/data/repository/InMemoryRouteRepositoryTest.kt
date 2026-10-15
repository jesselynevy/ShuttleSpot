package com.beg.shuttlespot.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryRouteRepositoryTest {
    @Test(expected = IllegalArgumentException::class)
    fun pickup_must_precede_destination() = runTest {
        InMemoryRouteRepository().savePrimaryRoute("bsd_umn", "bsd_umn_9", "bsd_umn_1")
    }

    @Test
    fun save_primary_route_persists_valid_selection() = runTest {
        val repository = InMemoryRouteRepository()
        repository.savePrimaryRoute("bsd_umn", "bsd_umn_1", "bsd_umn_9")
        val selection = repository.observePrimarySelection().first()
        assertEquals("bsd_umn", selection?.routeId)
        assertEquals("bsd_umn_1", selection?.pickupStopId)
        assertEquals("bsd_umn_9", selection?.destinationStopId)
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

    @Test
    fun `every stop of the bsd route has a coordinate`() = runTest {
        val stops = InMemoryRouteRepository().observeRouteDetails("bsd_umn").first()!!.stops

        assertEquals(9, stops.size)
        stops.forEach { stop ->
            assertNotNull("${stop.name} belum punya latitude", stop.latitude)
            assertNotNull("${stop.name} belum punya longitude", stop.longitude)
        }
    }

    @Test
    fun `all stored coordinates are within valid latitude and longitude ranges`() = runTest {
        val repository = InMemoryRouteRepository()
        repository.observeRoutes().first().forEach { route ->
            repository.observeRouteDetails(route.id).first()!!.stops.forEach { stop ->
                val lat = stop.latitude
                val lng = stop.longitude
                if (lat != null && lng != null) {
                    assertTrue("${stop.name}: latitude di luar -90..90", lat in -90.0..90.0)
                    assertTrue("${stop.name}: longitude di luar -180..180", lng in -180.0..180.0)
                }
            }
        }
    }

    @Test
    fun `bsd route coordinates fall inside the BSD to Gading Serpong area`() = runTest {
        // Kotak kasar ini menangkap latitude/longitude yang tertukar atau salah ketik.
        val stops = InMemoryRouteRepository().observeRouteDetails("bsd_umn").first()!!.stops
        stops.forEach { stop ->
            assertTrue("${stop.name}: latitude ${stop.latitude}", stop.latitude!! in -6.35..-6.20)
            assertTrue("${stop.name}: longitude ${stop.longitude}", stop.longitude!! in 106.60..106.70)
        }
    }

    @Test
    fun `bsd stops have unique ids and contiguous sequence numbers`() = runTest {
        val stops = InMemoryRouteRepository().observeRouteDetails("bsd_umn").first()!!.stops

        assertEquals(stops.size, stops.map { it.id }.toSet().size)
        assertEquals((1..stops.size).toList(), stops.map { it.sequence }.sorted())
    }
}
