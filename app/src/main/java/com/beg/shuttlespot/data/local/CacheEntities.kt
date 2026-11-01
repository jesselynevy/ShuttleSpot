package com.beg.shuttlespot.data.local

// Overview: Defines cache entities for the data local area of ShuttleSpot.

// Dependencies used by this file.
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Stores the route list locally so passengers can browse it without a network connection. */
@Entity(tableName = "cached_routes")
// Defines the cached route entity type.
data class CachedRouteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val serviceNotice: String? = null,
    val updatedAtEpochMillis: Long = 0,
)

/** Stores the ordered stops and map coordinates that belong to each cached route. */
@Entity(tableName = "cached_stops", primaryKeys = ["id", "routeId"])
// Defines the cached stop entity type.
data class CachedStopEntity(
    val id: String,
    val routeId: String,
    val name: String,
    val sequence: Int,
    val latitude: Double?,
    val longitude: Double?,
    val scheduledTime: String? = null,
)

@Entity(tableName = "cached_route_points", primaryKeys = ["routeId", "sequence"])
// Defines the cached route point entity type.
data class CachedRoutePointEntity(
    val routeId: String,
    val sequence: Int,
    val latitude: Double,
    val longitude: Double,
)

@Entity(tableName = "cached_timetable_entries", primaryKeys = ["routeId", "sequence"])
// Defines the cached timetable entry entity type.
data class CachedTimetableEntryEntity(
    val routeId: String,
    val sequence: Int,
    val period: String,
    val time: String,
    val description: String,
)

/** Stores one passenger's saved primary route, pickup stop, and destination stop. */
@Entity(tableName = "primary_selection")
// Defines the primary selection entity type.
data class PrimarySelectionEntity(@PrimaryKey val profileId: String, val routeId: String, val pickupStopId: String, val destinationStopId: String)

/** Stores the most recently known state of a trip for offline and stale-data display. */
@Entity(tableName = "trip_snapshots")
// Defines the trip snapshot entity type.
data class TripSnapshotEntity(
    @PrimaryKey val tripId: String,
    val routeId: String,
    val vehicleLabel: String,
    val status: String,
    val progress: Float,
    val previousStopName: String?,
    val nextStopName: String?,
    val etaMinutes: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val capturedAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long,
)

/** Stores announcements locally so recent service information remains readable offline. */
@Entity(tableName = "cached_announcements")
// Defines the cached announcement entity type.
data class CachedAnnouncementEntity(@PrimaryKey val id: String, val title: String, val body: String, val routeId: String?, val publishedAtEpochMillis: Long)

/** Queues retryable changes made offline until they can be sent to the remote service. */
@Entity(tableName = "outbox")
// Defines the outbox entity type.
data class OutboxEntity(
    @PrimaryKey val idempotencyKey: String,
    val operationType: String,
    val payload: String,
    val createdAtEpochMillis: Long,
    val retryCount: Int = 0
)

/** Stores the non-secret signed-in profile details needed to restore the local session. */
@Entity(tableName = "cached_sessions")
// Defines the cached session entity type.
data class CachedSessionEntity(
    @PrimaryKey val profileId: String,
    val role: String,
    val displayName: String,
    val contact: String,
    val updatedAtEpochMillis: Long,
)

/** Stores personal activity notifications and whether the passenger has read each one. */
@Entity(tableName = "cached_notifications")
// Defines the cached notification entity type.
data class CachedNotificationEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val title: String,
    val body: String,
    val target: String,
    val createdAtEpochMillis: Long,
    val isRead: Boolean = false,
)

@Entity(tableName = "cached_boarding_intents", primaryKeys = ["profileId", "tripId", "stopId"])
// Defines the cached boarding intent entity type.
data class CachedBoardingIntentEntity(
    val profileId: String,
    val tripId: String,
    val stopId: String,
    val status: String,
    val idempotencyKey: String,
    val updatedAtEpochMillis: Long,
)
