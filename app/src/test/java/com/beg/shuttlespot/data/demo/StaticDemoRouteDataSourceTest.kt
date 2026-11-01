package com.beg.shuttlespot.data.demo

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StaticDemoRouteDataSourceTest {
    @Test
    fun exposes_the_complete_reference_route_catalogue() = runTest {
        val routes = StaticDemoRouteDataSource().getRoutes()

        assertEquals(8, routes.size)
        assertEquals("bsd_umn", routes.first().route.id)
        assertEquals(9, routes.first().stops.size)
        assertTrue(routes.first().path.isNotEmpty())
    }
}
