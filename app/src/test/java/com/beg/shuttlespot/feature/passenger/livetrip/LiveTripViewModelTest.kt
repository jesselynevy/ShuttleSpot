package com.beg.shuttlespot.feature.passenger.livetrip

import com.beg.shuttlespot.core.model.LiveLocation
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.ui.components.MarkerKind
import com.beg.shuttlespot.data.repository.InMemoryPassengerTripRepository
import com.beg.shuttlespot.data.repository.InMemoryRouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LiveTripViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stop(id: String, sequence: Int, lat: Double? = -6.25, lng: Double? = 106.62) =
        Stop(id, "route", "Halte $id", sequence, lat, lng)

    // ---- RouteMapUiState (logika konversi, tanpa ViewModel) ----

    @Test
    fun `markers are sorted by stop sequence`() {
        val state = RouteMapUiState(stops = listOf(stop("c", 3), stop("a", 1), stop("b", 2)))

        assertEquals(listOf("a", "b", "c"), state.markers.map { it.id })
    }

    @Test
    fun `pickup and destination ids become marker kinds`() {
        val state = RouteMapUiState(
            stops = listOf(stop("a", 1), stop("b", 2), stop("c", 3)),
            pickupStopId = "a",
            destinationStopId = "c",
        )

        assertEquals(
            listOf(MarkerKind.Pickup, MarkerKind.Stop, MarkerKind.Destination),
            state.markers.map { it.kind },
        )
    }

    @Test
    fun `stops without coordinates are skipped instead of crashing`() {
        val state = RouteMapUiState(
            stops = listOf(
                stop("no-lat", 1, lat = null),
                stop("no-lng", 2, lng = null),
                stop("none", 3, lat = null, lng = null),
                stop("ok", 4),
            ),
        )

        assertEquals(listOf("ok"), state.markers.map { it.id })
    }

    @Test
    fun `state with only coordinate-less stops has no markers`() {
        val state = RouteMapUiState(stops = listOf(stop("a", 1, null, null), stop("b", 2, null, null)))

        assertTrue(state.markers.isEmpty())
    }

    @Test
    fun `bus location adds a bus marker after the stops`() {
        val state = RouteMapUiState(
            stops = listOf(stop("a", 1)),
            busLocation = LiveLocation(-6.26, 106.63, 0L),
        )

        assertEquals(listOf("a", "bus"), state.markers.map { it.id })
        assertEquals(MarkerKind.Bus, state.markers.last().kind)
    }

    // ---- LiveTripViewModel (membaca repository -> RouteMapUiState) ----

    private fun TestScope.viewModelWith(routes: InMemoryRouteRepository): LiveTripViewModel {
        val viewModel = LiveTripViewModel(InMemoryPassengerTripRepository(), routes)
        // stateIn(WhileSubscribed) baru aktif kalau ada yang mengumpulkan.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.mapState.collect {} }
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `view model exposes sorted stop markers with pickup and destination for the saved route`() = runTest {
        val routes = InMemoryRouteRepository()
        routes.savePrimaryRoute("bsd_umn", "bsd_umn_1", "bsd_umn_9")

        val state = viewModelWith(routes).mapState.value
        val stopMarkers = state.markers.filter { it.kind != MarkerKind.Bus }

        assertEquals((1..9).map { "bsd_umn_$it" }, stopMarkers.map { it.id })
        assertEquals("bsd_umn_1", state.pickupStopId)
        assertEquals("bsd_umn_9", state.destinationStopId)
        assertEquals(MarkerKind.Pickup, stopMarkers.first().kind)
        assertEquals(MarkerKind.Destination, stopMarkers.last().kind)
        assertTrue(stopMarkers.subList(1, stopMarkers.size - 1).all { it.kind == MarkerKind.Stop })
    }

    @Test
    fun `view model adds the bus marker from the active trip`() = runTest {
        val routes = InMemoryRouteRepository()
        routes.savePrimaryRoute("bsd_umn", "bsd_umn_1", "bsd_umn_9")

        val markers = viewModelWith(routes).mapState.value.markers

        assertEquals(10, markers.size)
        assertEquals(MarkerKind.Bus, markers.last().kind)
    }

    @Test
    fun `view model without a saved route exposes an empty map state`() = runTest {
        val state = viewModelWith(InMemoryRouteRepository()).mapState.value

        assertEquals(RouteMapUiState(), state)
        assertTrue(state.markers.isEmpty())
    }

    @Test
    fun `view model for a route without coordinates gives empty markers not a crash`() = runTest {
        val routes = InMemoryRouteRepository()
        routes.savePrimaryRoute("gading_serpong_loop", "gading_serpong_loop_1", "gading_serpong_loop_2")

        val state = viewModelWith(routes).mapState.value

        assertEquals("Rute Keliling Gading Serpong", state.routeName)
        assertTrue(state.stops.isNotEmpty())
        assertTrue(state.markers.isEmpty())
    }

    @Test
    fun `selecting a stop is reflected in map state and can be cleared`() = runTest {
        val routes = InMemoryRouteRepository()
        routes.savePrimaryRoute("bsd_umn", "bsd_umn_1", "bsd_umn_9")
        val viewModel = viewModelWith(routes)

        assertNull(viewModel.mapState.value.selectedStopId)

        viewModel.selectStop("bsd_umn_3")
        advanceUntilIdle()
        assertEquals("bsd_umn_3", viewModel.mapState.value.selectedStopId)

        viewModel.selectStop(null)
        advanceUntilIdle()
        assertNull(viewModel.mapState.value.selectedStopId)
    }
}
