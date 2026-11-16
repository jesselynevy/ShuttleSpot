package com.beg.shuttlespot.data.remote

import com.beg.shuttlespot.data.local.OutboxEntity
import com.beg.shuttlespot.data.local.OutboxOperation
import com.beg.shuttlespot.data.sync.MutationDeliveryResult
import com.beg.shuttlespot.data.sync.RemoteMutationSink
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

data class SavePassengerPreferenceMutation(
    val routeId: String,
    val pickupStopId: String,
    val destinationStopId: String,
)

data class SubmitBoardingIntentMutation(
    val tripId: String,
    val stopId: String,
    val idempotencyKey: String,
)

data class CancelBoardingIntentMutation(
    val tripId: String,
    val stopId: String,
)

interface PassengerMutationRemote {
    suspend fun savePreference(mutation: SavePassengerPreferenceMutation)
    suspend fun submitBoardingIntent(mutation: SubmitBoardingIntentMutation)
    suspend fun cancelBoardingIntent(mutation: CancelBoardingIntentMutation)
}

@Singleton
class SupabaseOutboxRemoteDataSource @Inject constructor(
    private val remote: PassengerMutationRemote,
) : RemoteMutationSink {

    override suspend fun deliver(operation: OutboxEntity): MutationDeliveryResult {
        if (operation.payloadVersion != SUPPORTED_PAYLOAD_VERSION) {
            return MutationDeliveryResult.PermanentFailure(
                "Versi payload outbox tidak didukung.",
            )
        }

        return try {
            val payload = Json.parseToJsonElement(operation.payload) as? JsonObject
                ?: throw SerializationException("Outbox payload must be an object")
            when (OutboxOperation.valueOf(operation.operationType)) {
                OutboxOperation.SAVE_PRIMARY_SELECTION ->
                    remote.savePreference(
                        SavePassengerPreferenceMutation(
                            routeId = payload.requiredString("routeId"),
                            pickupStopId = payload.requiredString("pickupStopId"),
                            destinationStopId = payload.requiredString("destinationStopId"),
                        ),
                    )

                OutboxOperation.SUBMIT_BOARDING_INTENT ->
                    remote.submitBoardingIntent(
                        SubmitBoardingIntentMutation(
                            tripId = payload.requiredString("tripId"),
                            stopId = payload.requiredString("stopId"),
                            idempotencyKey = operation.idempotencyKey,
                        ),
                    )

                OutboxOperation.CANCEL_BOARDING_INTENT ->
                    remote.cancelBoardingIntent(
                        CancelBoardingIntentMutation(
                            tripId = payload.requiredString("tripId"),
                            stopId = payload.requiredString("stopId"),
                        ),
                    )
            }
            MutationDeliveryResult.Delivered
        } catch (_: SerializationException) {
            MutationDeliveryResult.PermanentFailure("Payload outbox tidak valid.")
        } catch (_: IllegalArgumentException) {
            MutationDeliveryResult.PermanentFailure("Operasi outbox tidak dikenal.")
        } catch (error: PostgrestRestException) {
            if (error.statusCode == 408 || error.statusCode == 429 || error.statusCode >= 500) {
                MutationDeliveryResult.RetryableFailure(
                    "Server sementara tidak tersedia.",
                )
            } else {
                MutationDeliveryResult.PermanentFailure(
                    "Mutasi ditolak oleh server.",
                )
            }
        } catch (_: HttpRequestTimeoutException) {
            MutationDeliveryResult.RetryableFailure("Koneksi ke server timeout.")
        } catch (_: HttpRequestException) {
            MutationDeliveryResult.RetryableFailure("Jaringan belum tersedia.")
        } catch (_: IOException) {
            MutationDeliveryResult.RetryableFailure("Jaringan belum tersedia.")
        } catch (_: Exception) {
            MutationDeliveryResult.RetryableFailure("Sinkronisasi belum berhasil.")
        }
    }

    private fun JsonObject.requiredString(name: String): String =
        get(name)?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)
            ?: throw SerializationException("Missing $name")

    private companion object {
        const val SUPPORTED_PAYLOAD_VERSION = 1
    }
}

@Singleton
class SupabasePassengerMutationRemote @Inject constructor(
    private val clientProvider: Provider<SupabaseClient>,
) : PassengerMutationRemote {

    override suspend fun savePreference(mutation: SavePassengerPreferenceMutation) {
        clientProvider.get().postgrest.rpc(
            function = "save_passenger_preference",
            parameters = buildJsonObject {
                put("p_route_id", mutation.routeId)
                put("p_pickup_stop_id", mutation.pickupStopId)
                put("p_destination_stop_id", mutation.destinationStopId)
            },
        )
    }

    override suspend fun submitBoardingIntent(mutation: SubmitBoardingIntentMutation) {
        clientProvider.get().postgrest.rpc(
            function = "submit_boarding_intent",
            parameters = buildJsonObject {
                put("p_trip_id", mutation.tripId)
                put("p_stop_id", mutation.stopId)
                put("p_idempotency_key", mutation.idempotencyKey)
            },
        )
    }

    override suspend fun cancelBoardingIntent(mutation: CancelBoardingIntentMutation) {
        clientProvider.get().postgrest.rpc(
            function = "cancel_boarding_intent",
            parameters = buildJsonObject {
                put("p_trip_id", mutation.tripId)
                put("p_stop_id", mutation.stopId)
            },
        )
    }
}
