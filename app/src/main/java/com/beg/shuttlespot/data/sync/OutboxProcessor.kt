package com.beg.shuttlespot.data.sync

// Overview: Defines outbox processor for the data sync area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.data.local.OutboxEntity
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Remote mutation boundary; Supabase implementations are installed only outside demo mode. */
interface RemoteMutationSink {
    // Handles the deliver responsibility.
    suspend fun deliver(operation: OutboxEntity): Boolean
}

@Singleton
// Defines the outbox processor type.
class OutboxProcessor @Inject constructor(
    private val dao: ShuttleSpotDao,
    private val remote: RemoteMutationSink,
) {
    /** Returns false when a mutation remains retryable. Processing stops in order. */
    suspend fun processAll(): Boolean {
        for (operation in dao.observeOutbox().first()) {
            if (remote.deliver(operation)) dao.deleteOutbox(operation.idempotencyKey)
            else {
                dao.incrementOutboxRetry(operation.idempotencyKey)
                return false
            }
        }
        return true
    }
}
