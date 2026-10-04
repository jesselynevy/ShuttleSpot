package com.beg.shuttlespot.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_routes")
data class CachedRouteEntity(@PrimaryKey val id: String, val name: String)

@Entity(tableName = "cached_stops", primaryKeys = ["id", "routeId"])
data class CachedStopEntity(val id: String, val routeId: String, val name: String, val sequence: Int, val latitude: Double, val longitude: Double)

@Entity(tableName = "primary_selection")
data class PrimarySelectionEntity(@PrimaryKey val profileId: String, val routeId: String, val pickupStopId: String, val destinationStopId: String)

@Entity(tableName = "trip_snapshots")
data class TripSnapshotEntity(@PrimaryKey val tripId: String, val routeId: String, val status: String, val updatedAtEpochMillis: Long)

@Entity(tableName = "cached_announcements")
data class CachedAnnouncementEntity(@PrimaryKey val id: String, val title: String, val body: String, val routeId: String?, val publishedAtEpochMillis: Long)

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey val idempotencyKey: String,
    val operationType: String,
    val payload: String,
    val createdAtEpochMillis: Long,
    val retryCount: Int = 0
)
