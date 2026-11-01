package com.beg.shuttlespot.data.repository

// Overview: Defines room passenger trip repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.BoardingIntent
import com.beg.shuttlespot.core.model.BoardingIntentStatus
import com.beg.shuttlespot.core.model.EtaEstimate
import com.beg.shuttlespot.core.model.LiveLocation
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.data.local.CachedBoardingIntentEntity
import com.beg.shuttlespot.data.local.OutboxEntity
import com.beg.shuttlespot.data.local.OutboxOperation
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import com.beg.shuttlespot.data.local.TripSnapshotEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** Local-first passenger trip state. A static demo snapshot seeds Room until realtime is connected. */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
// Defines the room passenger trip repository type.
class RoomPassengerTripRepository @Inject constructor(
    private val dao: ShuttleSpotDao,
    private val sessions: SessionRepository,
) : PassengerTripRepository {
    // Implements framework behavior for observe active trip.
    override fun observeActiveTrip(routeId: String): Flow<ActiveTripSnapshot?> =
        dao.observeTripSnapshot(routeId)
            .onStart { ensureDemoSnapshot(routeId) }
            .map { it?.toDomain() }

    // Implements framework behavior for observe boarding intent.
    override fun observeBoardingIntent(tripId: String, stopId: String): Flow<BoardingIntent?> =
        sessions.observeSession().flatMapLatest { session ->
            if (session == null) flowOf(null)
            else dao.observeBoardingIntent(session.profileId, tripId, stopId).map { it?.toDomain() }
        }

    // Implements framework behavior for submit boarding intent.
    override suspend fun submitBoardingIntent(tripId: String, stopId: String, idempotencyKey: String): BoardingIntent {
        val profileId = requireNotNull(sessions.observeSession().first()?.profileId) { "Sesi diperlukan untuk konfirmasi." }
        val existing = dao.getBoardingIntent(profileId, tripId, stopId)
        if (existing != null) return existing.toDomain()

        val now = System.currentTimeMillis()
        val intent = CachedBoardingIntentEntity(
            profileId = profileId,
            tripId = tripId,
            stopId = stopId,
            status = BoardingIntentStatus.PENDING_SYNC.name,
            idempotencyKey = idempotencyKey,
            updatedAtEpochMillis = now,
        )
        dao.upsertBoardingIntent(intent)
        dao.enqueue(
            OutboxEntity(
                idempotencyKey = idempotencyKey,
                operationType = OutboxOperation.SUBMIT_BOARDING_INTENT.name,
                payload = JSONObject()
                    .put("profileId", profileId)
                    .put("tripId", tripId)
                    .put("stopId", stopId)
                    .toString(),
                createdAtEpochMillis = now,
            )
        )
        return intent.toDomain()
    }

    // Implements framework behavior for cancel boarding intent.
    override suspend fun cancelBoardingIntent(tripId: String, stopId: String) {
        val profileId = requireNotNull(sessions.observeSession().first()?.profileId) { "Sesi diperlukan untuk membatalkan konfirmasi." }
        val existing = dao.getBoardingIntent(profileId, tripId, stopId) ?: return
        val cancelled = existing.copy(status = BoardingIntentStatus.CANCELLED.name, updatedAtEpochMillis = System.currentTimeMillis())
        dao.upsertBoardingIntent(cancelled)
        dao.enqueue(
            OutboxEntity(
                idempotencyKey = "${existing.idempotencyKey}:cancel",
                operationType = OutboxOperation.CANCEL_BOARDING_INTENT.name,
                payload = JSONObject().put("profileId", profileId).put("tripId", tripId).put("stopId", stopId).toString(),
                createdAtEpochMillis = cancelled.updatedAtEpochMillis,
            )
        )
    }

    // Handles the ensure demo snapshot responsibility.
    private suspend fun ensureDemoSnapshot(routeId: String) {
        if (routeId != DEMO_ROUTE_ID || dao.observeTripSnapshot(routeId).first() != null) return
        val now = System.currentTimeMillis()
        dao.upsertTripSnapshot(
            TripSnapshotEntity(
                tripId = DEMO_TRIP_ID,
                routeId = routeId,
                vehicleLabel = "D 8801 VBN",
                status = TripStatus.EN_ROUTE.name,
                progress = 0.15f,
                previousStopName = "Halte Pasar Modern BSD",
                nextStopName = "Halte Seberang Telkom",
                etaMinutes = 3,
                latitude = -6.294,
                longitude = 106.675,
                capturedAtEpochMillis = now,
                updatedAtEpochMillis = now,
            )
        )
    }

    // Handles the cached boarding intent entity responsibility.
    private fun CachedBoardingIntentEntity.toDomain() = BoardingIntent(
        tripId, stopId, BoardingIntentStatus.valueOf(status), idempotencyKey,
    )

    // Handles the trip snapshot entity responsibility.
    private fun TripSnapshotEntity.toDomain() = ActiveTripSnapshot(
        tripId = tripId,
        routeId = routeId,
        vehicleLabel = vehicleLabel,
        status = TripStatus.valueOf(status),
        progress = progress,
        previousStopName = previousStopName,
        nextStopName = nextStopName,
        eta = EtaEstimate(etaMinutes, isStale = updatedAtEpochMillis < System.currentTimeMillis() - STALE_AFTER_MILLIS),
        lastUpdatedEpochMillis = updatedAtEpochMillis,
        location = if (latitude == null || longitude == null || capturedAtEpochMillis == null) null else LiveLocation(latitude, longitude, capturedAtEpochMillis),
    )

    private companion object {
        const val DEMO_ROUTE_ID = "bsd_umn"
        const val DEMO_TRIP_ID = "trip-bsd-1"
        const val STALE_AFTER_MILLIS = 60_000L
    }
}
