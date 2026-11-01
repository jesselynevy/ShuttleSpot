package com.beg.shuttlespot.data.repository

// Overview: Defines notification repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.AppNotification
import com.beg.shuttlespot.core.model.NotificationTarget
import com.beg.shuttlespot.data.local.CachedNotificationEntity
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Defines the notification repository type.
interface NotificationRepository {
    // Prepares or returns observe notifications.
    fun observeNotifications(profileId: String): Flow<List<AppNotification>>
    // Handles the mark read action.
    suspend fun markRead(id: String)
    // Handles the clear action.
    suspend fun clear(profileId: String)
}

@Singleton
// Defines the room notification repository type.
class RoomNotificationRepository @Inject constructor(
    private val dao: ShuttleSpotDao,
) : NotificationRepository {
    // Implements framework behavior for observe notifications.
    override fun observeNotifications(profileId: String): Flow<List<AppNotification>> =
        dao.observeNotifications(profileId).map { cached ->
            cached.map { it.toDomain() }
        }

    // Implements framework behavior for mark read.
    override suspend fun markRead(id: String) = dao.markNotificationRead(id)
    // Implements framework behavior for clear.
    override suspend fun clear(profileId: String) = dao.clearNotifications(profileId)

    // Handles the cached notification entity responsibility.
    private fun CachedNotificationEntity.toDomain() = AppNotification(
        id, profileId, title, body, NotificationTarget.valueOf(target), createdAtEpochMillis, isRead,
    )
}
