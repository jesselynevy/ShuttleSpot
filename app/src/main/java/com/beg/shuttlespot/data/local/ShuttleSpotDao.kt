package com.beg.shuttlespot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShuttleSpotDao {
    @Query("SELECT * FROM cached_routes ORDER BY name") fun observeRoutes(): Flow<List<CachedRouteEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertRoutes(routes: List<CachedRouteEntity>)
    @Query("SELECT * FROM primary_selection WHERE profileId = :profileId") fun observeSelection(profileId: String): Flow<PrimarySelectionEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSelection(selection: PrimarySelectionEntity)
    @Query("SELECT * FROM outbox ORDER BY createdAtEpochMillis") fun observeOutbox(): Flow<List<OutboxEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun enqueue(operation: OutboxEntity)
    @Query("DELETE FROM outbox WHERE idempotencyKey = :key") suspend fun deleteOutbox(key: String)
}
