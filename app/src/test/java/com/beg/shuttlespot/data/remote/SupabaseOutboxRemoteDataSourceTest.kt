package com.beg.shuttlespot.data.remote

import com.beg.shuttlespot.data.local.OutboxEntity
import com.beg.shuttlespot.data.local.OutboxOperation
import com.beg.shuttlespot.data.sync.MutationDeliveryResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SupabaseOutboxRemoteDataSourceTest {

    @Test
    fun `saved route selection is delivered using authenticated profile RPC`() = runTest {
        val remote = RecordingPassengerMutationRemote()
        val sink = SupabaseOutboxRemoteDataSource(remote)

        val result = sink.deliver(
            operation(
                type = OutboxOperation.SAVE_PRIMARY_SELECTION,
                payload = """{
                    "profileId":"ignored-client-profile",
                    "routeId":"route-1",
                    "pickupStopId":"stop-1",
                    "destinationStopId":"stop-2"
                }""".trimIndent(),
            ),
        )

        assertEquals(MutationDeliveryResult.Delivered, result)
        assertEquals(
            SavePassengerPreferenceMutation("route-1", "stop-1", "stop-2"),
            remote.savedPreference,
        )
    }

    @Test
    fun `boarding retry sends the stable idempotency key`() = runTest {
        val remote = RecordingPassengerMutationRemote()
        val sink = SupabaseOutboxRemoteDataSource(remote)

        val result = sink.deliver(
            operation(
                type = OutboxOperation.SUBMIT_BOARDING_INTENT,
                key = "stable-key",
                payload = """{
                    "profileId":"ignored-client-profile",
                    "tripId":"trip-1",
                    "stopId":"stop-1"
                }""".trimIndent(),
            ),
        )

        assertEquals(MutationDeliveryResult.Delivered, result)
        assertEquals(
            SubmitBoardingIntentMutation("trip-1", "stop-1", "stable-key"),
            remote.submittedIntent,
        )
    }

    @Test
    fun `unsupported payload version is permanently rejected without a network call`() = runTest {
        val remote = RecordingPassengerMutationRemote()
        val sink = SupabaseOutboxRemoteDataSource(remote)

        val result = sink.deliver(
            operation(
                type = OutboxOperation.SAVE_PRIMARY_SELECTION,
                payloadVersion = 99,
                payload = """{
                    "routeId":"route-1",
                    "pickupStopId":"stop-1",
                    "destinationStopId":"stop-2"
                }""".trimIndent(),
            ),
        )

        assertEquals(
            MutationDeliveryResult.PermanentFailure("Versi payload outbox tidak didukung."),
            result,
        )
        assertEquals(0, remote.callCount)
    }

    @Test
    fun `invalid payload is permanently rejected`() = runTest {
        val result = SupabaseOutboxRemoteDataSource(RecordingPassengerMutationRemote())
            .deliver(
                operation(
                    type = OutboxOperation.SUBMIT_BOARDING_INTENT,
                    payload = "{}",
                ),
            )

        assertEquals(
            MutationDeliveryResult.PermanentFailure("Payload outbox tidak valid."),
            result,
        )
    }

    private fun operation(
        type: OutboxOperation,
        key: String = "operation-key",
        payloadVersion: Int = 1,
        payload: String,
    ) = OutboxEntity(
        idempotencyKey = key,
        operationType = type.name,
        payloadVersion = payloadVersion,
        payload = payload,
        createdAtEpochMillis = 1L,
    )
}

private class RecordingPassengerMutationRemote : PassengerMutationRemote {
    var savedPreference: SavePassengerPreferenceMutation? = null
    var submittedIntent: SubmitBoardingIntentMutation? = null
    var callCount = 0

    override suspend fun savePreference(mutation: SavePassengerPreferenceMutation) {
        callCount += 1
        savedPreference = mutation
    }

    override suspend fun submitBoardingIntent(mutation: SubmitBoardingIntentMutation) {
        callCount += 1
        submittedIntent = mutation
    }

    override suspend fun cancelBoardingIntent(mutation: CancelBoardingIntentMutation) {
        callCount += 1
    }
}
