package com.beg.shuttlespot.data.sync

// Overview: Defines outbox processor for the data sync area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.data.local.OutboxEntity
import com.beg.shuttlespot.data.local.OutboxOperation
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Remote mutation boundary; Supabase implementations are installed only outside demo mode. */
interface RemoteMutationSink {
    suspend fun deliver(operation: OutboxEntity): MutationDeliveryResult
}

sealed interface MutationDeliveryResult {
    data object Delivered : MutationDeliveryResult
    data class RetryableFailure(val reason: String) : MutationDeliveryResult
    data class PermanentFailure(val reason: String) : MutationDeliveryResult
}

internal sealed interface OutboxAction {
    data object Delete : OutboxAction
    data class Retry(val reason: String) : OutboxAction
    data class FailPermanently(val reason: String) : OutboxAction
}

internal fun decideOutboxAction(
    result: MutationDeliveryResult,
    currentRetryCount: Int,
): OutboxAction = when (result) {
    MutationDeliveryResult.Delivered -> OutboxAction.Delete
    is MutationDeliveryResult.PermanentFailure ->
        OutboxAction.FailPermanently(result.reason)
    is MutationDeliveryResult.RetryableFailure ->
        if (currentRetryCount + 1 >= MAX_OUTBOX_ATTEMPTS) {
            OutboxAction.FailPermanently("Batas percobaan sinkronisasi tercapai.")
        } else {
            OutboxAction.Retry(result.reason)
        }
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
            when (
                val action = decideOutboxAction(
                    result = remote.deliver(operation),
                    currentRetryCount = operation.retryCount,
                )
            ) {
                OutboxAction.Delete -> {
                    if (operation.operationType == OutboxOperation.SUBMIT_BOARDING_INTENT.name) {
                        dao.markBoardingIntentSynced(operation.idempotencyKey)
                    }
                    dao.deleteOutbox(operation.idempotencyKey)
                }

                is OutboxAction.FailPermanently ->
                    dao.markOutboxFailed(operation.idempotencyKey, action.reason)

                is OutboxAction.Retry -> {
                    dao.recordOutboxRetry(operation.idempotencyKey, action.reason)
                    return false
                }
            }
        }
        return true
    }
}

private const val MAX_OUTBOX_ATTEMPTS = 5
