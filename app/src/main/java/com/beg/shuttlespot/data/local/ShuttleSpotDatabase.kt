package com.beg.shuttlespot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedRouteEntity::class, CachedStopEntity::class, PrimarySelectionEntity::class, TripSnapshotEntity::class, CachedAnnouncementEntity::class, OutboxEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ShuttleSpotDatabase : RoomDatabase() {
    abstract fun shuttleSpotDao(): ShuttleSpotDao
}
