package com.beg.shuttlespot.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.model.BoardingIntentStatus
import com.beg.shuttlespot.data.demo.StaticDemoRouteDataSource
import com.beg.shuttlespot.data.local.ShuttleSpotDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistedRouteRepositoryInstrumentedTest {
    private lateinit var database: ShuttleSpotDatabase
    private lateinit var repository: PersistedRouteRepository

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ShuttleSpotDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = PersistedRouteRepository(
            StaticDemoRouteDataSource(),
            SessionFake(),
            database.shuttleSpotDao(),
        )
    }

    @After fun tearDown() = database.close()

    @Test fun refresh_caches_routes_stops_and_geometry() = runBlocking {
        repository.refreshRoutes()

        assertEquals(8, repository.observeRoutes().first().size)
        val bsd = repository.observeRouteDetails("bsd_umn").first()
        assertEquals(9, bsd?.stops?.size)
        assertNotNull(bsd?.path?.firstOrNull())
    }

    @Test fun save_selection_persists_locally_and_queues_one_mutation() = runBlocking {
        repository.refreshRoutes()
        repository.savePrimaryRoute("bsd_umn", "bsd_umn_1", "bsd_umn_9")

        assertEquals("bsd_umn", repository.observePrimarySelection().first()?.routeId)
        assertEquals("SAVE_PRIMARY_SELECTION", database.shuttleSpotDao().observeOutbox().first().single().operationType)
    }

    @Test fun boarding_intent_is_persisted_once_and_queued_for_sync() = runBlocking {
        val trips = RoomPassengerTripRepository(database.shuttleSpotDao(), SessionFake())
        val first = trips.submitBoardingIntent("trip-bsd-1", "bsd_umn_1", "intent-key")
        val retry = trips.submitBoardingIntent("trip-bsd-1", "bsd_umn_1", "different-key")

        assertEquals(BoardingIntentStatus.PENDING_SYNC, first.status)
        assertEquals(first, retry)
        assertEquals(1, database.shuttleSpotDao().observeOutbox().first().size)
    }

    private class SessionFake : SessionRepository {
        private val session = MutableStateFlow<AppSession?>(AppSession("passenger-test", UserRole.PASSENGER, "Test", "test@student.umn.ac.id"))
        override fun observeSession(): Flow<AppSession?> = session
        override suspend fun signInPassenger(email: String, password: String): SignInResult = SignInResult.Success(requireNotNull(session.value))
        override suspend fun signInDriver(phoneNumber: String): SignInResult = SignInResult.Rejected("unused")
        override suspend fun signOut() { session.value = null }
    }
}
