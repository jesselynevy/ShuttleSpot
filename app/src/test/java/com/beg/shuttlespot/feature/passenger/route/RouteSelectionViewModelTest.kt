package com.beg.shuttlespot.feature.passenger.route

import com.beg.shuttlespot.data.repository.InMemoryRouteRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteSelectionViewModelTest {
    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `onboarding requires a pickup followed by a later destination`() {
        val viewModel = RouteSelectionViewModel(InMemoryRouteRepository())
        viewModel.selectRoute("bsd_umn", useDefaults = false)
        assertNull(viewModel.uiState.value.pickupStop)
        assertNull(viewModel.uiState.value.destinationStop)
        viewModel.selectOnboardingStop("bsd_umn_3")
        viewModel.selectOnboardingStop("bsd_umn_2")
        assertFalse(viewModel.uiState.value.canReview)
        assertNotNull(viewModel.uiState.value.errorMessage)
        viewModel.selectOnboardingStop("bsd_umn_3")
        viewModel.selectOnboardingStop("bsd_umn_9")
        assertTrue(viewModel.uiState.value.canReview)
        assertEquals("06.15", viewModel.uiState.value.timetableSummary?.pickupTime)
        assertEquals("07.30", viewModel.uiState.value.timetableSummary?.destinationTime)
        viewModel.openTimetable()
        assertTrue(viewModel.uiState.value.showTimetable)
    }

    @Test fun `discarding an unconfirmed replacement restores the saved primary route`() = runBlocking {
        val repository = InMemoryRouteRepository()
        repository.savePrimaryRoute("bsd_umn", "bsd_umn_3", "bsd_umn_9")
        val viewModel = RouteSelectionViewModel(repository)

        viewModel.selectRoute("grogol_umn", useDefaults = false)
        viewModel.selectOnboardingStop("grogol_umn_1")
        viewModel.selectOnboardingStop("grogol_umn_7")
        assertEquals("grogol_umn", viewModel.uiState.value.selectedRoute?.route?.id)

        viewModel.discardUnconfirmedRouteChange()

        assertEquals("bsd_umn", viewModel.uiState.value.selectedRoute?.route?.id)
        assertEquals("bsd_umn_3", viewModel.uiState.value.pickupStopId)
        assertEquals("bsd_umn_9", viewModel.uiState.value.destinationStopId)
    }
}
