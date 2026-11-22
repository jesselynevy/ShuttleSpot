package com.beg.shuttlespot.data.local

// Overview: Defines shuttle spot dao for the data local area of ShuttleSpot.

// Dependencies used by this file.
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
// Defines the shuttle spot dao type.
interface ShuttleSpotDao {
    @Query("SELECT * FROM cached_routes ORDER BY name") fun observeRoutes(): Flow<List<CachedRouteEntity>>
    @Query("SELECT * FROM cached_routes WHERE id = :routeId") fun observeRoute(routeId: String): Flow<CachedRouteEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertRoutes(routes: List<CachedRouteEntity>)
    @Query("SELECT * FROM cached_stops WHERE routeId = :routeId ORDER BY sequence") fun observeStops(routeId: String): Flow<List<CachedStopEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertStops(stops: List<CachedStopEntity>)
    @Query("SELECT * FROM cached_route_points WHERE routeId = :routeId ORDER BY sequence") fun observeRoutePoints(routeId: String): Flow<List<CachedRoutePointEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertRoutePoints(points: List<CachedRoutePointEntity>)
    @Query("SELECT * FROM cached_timetable_entries WHERE routeId = :routeId ORDER BY sequence") fun observeTimetableEntries(routeId: String): Flow<List<CachedTimetableEntryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTimetableEntries(entries: List<CachedTimetableEntryEntity>)
    @Query("SELECT * FROM primary_selection WHERE profileId = :profileId") fun observeSelection(profileId: String): Flow<PrimarySelectionEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSelection(selection: PrimarySelectionEntity)
    @Query("SELECT * FROM outbox WHERE failedPermanently = 0 ORDER BY createdAtEpochMillis") fun observeOutbox(): Flow<List<OutboxEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun enqueue(operation: OutboxEntity)
    @Query("DELETE FROM outbox WHERE idempotencyKey = :key") suspend fun deleteOutbox(key: String)
    @Query("UPDATE outbox SET retryCount = retryCount + 1, lastError = :error WHERE idempotencyKey = :key") suspend fun recordOutboxRetry(key: String, error: String)
    @Query("UPDATE outbox SET failedPermanently = 1, lastError = :error WHERE idempotencyKey = :key") suspend fun markOutboxFailed(key: String, error: String)
    @Query("SELECT * FROM trip_snapshots WHERE routeId = :routeId ORDER BY updatedAtEpochMillis DESC LIMIT 1") suspend fun getTripSnapshot(routeId: String): TripSnapshotEntity?
    @Query("SELECT * FROM trip_snapshots WHERE routeId = :routeId ORDER BY updatedAtEpochMillis DESC LIMIT 1") fun observeTripSnapshot(routeId: String): Flow<TripSnapshotEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTripSnapshot(snapshot: TripSnapshotEntity)
    @Query("SELECT * FROM cached_boarding_intents WHERE profileId = :profileId AND tripId = :tripId AND stopId = :stopId") fun observeBoardingIntent(profileId: String, tripId: String, stopId: String): Flow<CachedBoardingIntentEntity?>
    @Query("SELECT * FROM cached_boarding_intents WHERE profileId = :profileId AND tripId = :tripId AND stopId = :stopId") suspend fun getBoardingIntent(profileId: String, tripId: String, stopId: String): CachedBoardingIntentEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertBoardingIntent(intent: CachedBoardingIntentEntity)
    @Query("UPDATE cached_boarding_intents SET status = 'CONFIRMED' WHERE idempotencyKey = :idempotencyKey AND status = 'PENDING_SYNC'")
    suspend fun markBoardingIntentSynced(idempotencyKey: String)

    @Query("SELECT * FROM cached_sessions LIMIT 1") fun observeSession(): Flow<CachedSessionEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSession(session: CachedSessionEntity)
    @Query("DELETE FROM cached_sessions") suspend fun clearSession()

    @Query("SELECT * FROM cached_notifications WHERE profileId = :profileId ORDER BY createdAtEpochMillis DESC") fun observeNotifications(profileId: String): Flow<List<CachedNotificationEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertNotifications(items: List<CachedNotificationEntity>)
    @Query("UPDATE cached_notifications SET isRead = 1 WHERE id = :id") suspend fun markNotificationRead(id: String)
    @Query("DELETE FROM cached_notifications WHERE profileId = :profileId") suspend fun clearNotifications(profileId: String)
}
