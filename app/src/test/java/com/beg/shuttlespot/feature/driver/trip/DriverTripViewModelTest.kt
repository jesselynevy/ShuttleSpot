package com.beg.shuttlespot.feature.driver.trip

import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.AuthActionResult
import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.SignUpResult
import com.beg.shuttlespot.core.model.TripLifecycle
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.data.location.LocationPublisher
import com.beg.shuttlespot.data.repository.DriverTripRepository
import com.beg.shuttlespot.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverTripViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `driver without assignment sees empty state`() = runTest {
        val viewModel = DriverTripViewModel(
            trips = FakeDriverTripRepository(null),
            locationPublisher = FakeLocationPublisher(),
            sessions = FakeSessionRepository(driverSession()),
        )
        advanceUntilIdle()

        assertEquals(DriverTripUiState.NoAssignment, viewModel.state.value)
    }

    @Test
    fun `start advance and finish update state and location boundary`() = runTest {
        val repository = FakeDriverTripRepository(snapshot())
        val location = FakeLocationPublisher()
        val viewModel = DriverTripViewModel(
            trips = repository,
            locationPublisher = location,
            sessions = FakeSessionRepository(driverSession()),
        )
        advanceUntilIdle()

        viewModel.startTrip()
        advanceUntilIdle()
        assertEquals(TripLifecycle.ACTIVE, viewModel.readyTrip().status)
        assertEquals("trip-1", location.startedTripId)

        viewModel.advanceStop()
        advanceUntilIdle()
        assertEquals(2, viewModel.readyTrip().currentStopSequence)

        viewModel.finishTrip()
        advanceUntilIdle()
        assertEquals(TripLifecycle.COMPLETED, viewModel.readyTrip().status)
        assertTrue(location.stopped)
    }

    @Test
    fun `repository failure becomes retryable error state`() = runTest {
        val repository = FakeDriverTripRepository(snapshot()).apply {
            loadError = IllegalStateException("Jaringan belum tersedia.")
        }
        val viewModel = DriverTripViewModel(
            trips = repository,
            locationPublisher = FakeLocationPublisher(),
            sessions = FakeSessionRepository(driverSession()),
        )
        advanceUntilIdle()

        assertEquals(
            "Jaringan belum tersedia.",
            (viewModel.state.value as DriverTripUiState.Error).message,
        )

        repository.loadError = null
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.state.value is DriverTripUiState.Ready)
    }

    @Test
    fun `second start is ignored while the first action is running`() = runTest {
        val repository = FakeDriverTripRepository(snapshot()).apply {
            startGate = CompletableDeferred()
        }
        val viewModel = DriverTripViewModel(
            trips = repository,
            locationPublisher = FakeLocationPublisher(),
            sessions = FakeSessionRepository(driverSession()),
        )
        advanceUntilIdle()

        viewModel.startTrip()
        viewModel.startTrip()
        assertEquals(1, repository.startCalls)

        repository.startGate?.complete(Unit)
        advanceUntilIdle()
        assertEquals(TripLifecycle.ACTIVE, viewModel.readyTrip().status)
    }

    private fun DriverTripViewModel.readyTrip() =
        (state.value as DriverTripUiState.Ready).trip

    private fun snapshot() = DriverTripSnapshot(
        tripId = "trip-1",
        routeId = "route-1",
        vehicleLabel = "Bus 01",
        status = TripLifecycle.NOT_STARTED,
        currentStopName = null,
        nextStopName = "Halte A",
        currentStopSequence = 0,
        totalStops = 2,
        waitingPassengerCount = 1,
    )

    private fun driverSession() = AppSession(
        profileId = "driver-1",
        role = UserRole.DRIVER,
        displayName = "Driver",
        contact = "+628123456789",
    )
}

private class FakeDriverTripRepository(
    initial: DriverTripSnapshot?,
) : DriverTripRepository {
    private var trip = initial
    var loadError: Exception? = null
    var startGate: CompletableDeferred<Unit>? = null
    var startCalls = 0

    override suspend fun getAssignedTrip(): DriverTripSnapshot? {
        loadError?.let { throw it }
        return trip
    }

    override suspend fun startTrip(tripId: String): DriverTripSnapshot {
        startCalls += 1
        startGate?.await()
        return update { it.copy(status = TripLifecycle.ACTIVE) }
    }

    override suspend fun advanceStop(tripId: String): DriverTripSnapshot = update {
        it.copy(currentStopSequence = it.currentStopSequence + 2, nextStopName = null)
    }

    override suspend fun finishTrip(tripId: String): DriverTripSnapshot = update {
        it.copy(status = TripLifecycle.COMPLETED)
    }

    private fun update(block: (DriverTripSnapshot) -> DriverTripSnapshot): DriverTripSnapshot {
        return block(requireNotNull(trip)).also { trip = it }
    }
}

private class FakeLocationPublisher : LocationPublisher {
    var startedTripId: String? = null
    var stopped = false

    override suspend fun start(tripId: String) {
        startedTripId = tripId
    }

    override suspend fun stop() {
        stopped = true
    }
}

private class FakeSessionRepository(
    session: AppSession,
) : SessionRepository {
    private val sessionFlow = MutableStateFlow<AppSession?>(session)
    override fun observeSession(): Flow<AppSession?> = sessionFlow
    override suspend fun restoreSession() = Unit
    override suspend fun signUpPassenger(displayName: String, email: String, password: String) =
        SignUpResult.Rejected("unused")
    override suspend fun signInPassenger(email: String, password: String) =
        SignInResult.Rejected("unused")
    override suspend fun signInDriver(phoneNumber: String, password: String) =
        SignInResult.Rejected("unused")
    override suspend fun resendConfirmation(email: String) = AuthActionResult.Success
    override suspend fun sendPasswordReset(email: String) = AuthActionResult.Success
    override suspend fun updatePassword(newPassword: String) = AuthActionResult.Success
    override suspend fun updateDriverPassword(currentPassword: String, newPassword: String) =
        AuthActionResult.Success
    override suspend fun signOut() = Unit
}
