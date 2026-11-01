package com.beg.shuttlespot.data.local

// Overview: Defines shuttle spot database for the data local area of ShuttleSpot.

// Dependencies used by this file.
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedRouteEntity::class, CachedStopEntity::class, CachedRoutePointEntity::class, CachedTimetableEntryEntity::class, PrimarySelectionEntity::class, TripSnapshotEntity::class, CachedAnnouncementEntity::class, OutboxEntity::class, CachedSessionEntity::class, CachedNotificationEntity::class, CachedBoardingIntentEntity::class],
    version = 3,
    exportSchema = false
)
// Defines the shuttle spot database type.
abstract class ShuttleSpotDatabase : RoomDatabase() {
    abstract fun shuttleSpotDao(): ShuttleSpotDao
}
