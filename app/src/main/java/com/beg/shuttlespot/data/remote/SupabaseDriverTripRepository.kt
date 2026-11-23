package com.beg.shuttlespot.data.remote

import com.beg.shuttlespot.core.model.DriverTripSnapshot
import com.beg.shuttlespot.core.model.TripLifecycle
import com.beg.shuttlespot.data.repository.DriverTripException
import com.beg.shuttlespot.data.repository.DriverTripRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject
import java.time.Instant

@Serializable
internal data class RemoteDriverTripDto(
    @SerialName("trip_id") val tripId: String,
    @SerialName("route_id") val routeId: String,
    @SerialName("vehicle_label") val vehicleLabel: String,
    val status: String,
    @SerialName("current_stop_name") val currentStopName: String? = null,
    @SerialName("next_stop_name") val nextStopName: String? = null,
    @SerialName("current_stop_sequence") val currentStopSequence: Int,
    @SerialName("total_stops") val totalStops: Int,
    @SerialName("waiting_passenger_count") val waitingPassengerCount: Int,
)

internal fun RemoteDriverTripDto.toDomain() = DriverTripSnapshot(
    tripId = tripId,
    routeId = routeId,
    vehicleLabel = vehicleLabel,
    status = when (status) {
        "not_started" -> TripLifecycle.NOT_STARTED
        "active" -> TripLifecycle.ACTIVE
        "completed" -> TripLifecycle.COMPLETED
        else -> error("Unsupported driver trip status: $status")
    },
    currentStopName = currentStopName,
    nextStopName = nextStopName,
    currentStopSequence = currentStopSequence,
    totalStops = totalStops,
    waitingPassengerCount = waitingPassengerCount,
)

@Singleton
class SupabaseDriverTripRepository @Inject constructor(
    private val clientProvider: Provider<SupabaseClient>,
) : DriverTripRepository {
    override suspend fun getAssignedTrip(): DriverTripSnapshot? = runRemote {
        clientProvider.get().postgrest
            .rpc("get_assigned_trip")
            .decodeAsOrNull<RemoteDriverTripDto>()
            ?.toDomain()
    }

    override suspend fun startTrip(tripId: String): DriverTripSnapshot =
        mutate("start_assigned_trip", tripId)

    override suspend fun advanceStop(tripId: String): DriverTripSnapshot =
        mutate("advance_trip_stop", tripId)

    override suspend fun finishTrip(tripId: String): DriverTripSnapshot =
        mutate("finish_assigned_trip", tripId)

    private suspend fun mutate(
        function: String,
        tripId: String,
    ): DriverTripSnapshot = runRemote {
        clientProvider.get().postgrest.rpc(
            function = function,
            parameters = buildJsonObject { put("p_trip_id", tripId) },
        ).decodeAs<RemoteDriverTripDto>().toDomain()
    }

    private suspend fun <T> runRemote(block: suspend () -> T): T = try {
        block()
    } catch (error: Exception) {
        throw DriverTripException(error.toDriverTripMessage())
    }

    override suspend fun publishLocation(
        tripId: String,
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float?,
        capturedAtEpochMillis: Long,
    ) {
        try {
            clientProvider.get().postgrest.rpc(
                function = "publish_vehicle_location",
                parameters = buildJsonObject {
                    put("p_trip_id", tripId)
                    put("p_latitude", latitude)
                    put("p_longitude", longitude)
                    put("p_accuracy_meters", accuracyMeters)
                    put("p_captured_at", Instant.ofEpochMilli(capturedAtEpochMillis).toString())
                },
            )
        } catch (error: Exception) {
            android.util.Log.w("DriverTrip", "publishLocation failed: ${error.message}")
        }
    }
}

internal fun Throwable.toDriverTripMessage(): String {
    val raw = message.orEmpty().lowercase()
    return when {
        "final stop" in raw -> "Capai halte terakhir sebelum menyelesaikan perjalanan."
        "only an active trip" in raw -> "Perjalanan belum aktif atau sudah selesai."
        "start once" in raw -> "Perjalanan ini sudah pernah dimulai."
        "assigned trip not found" in raw -> "Trip ini tidak ditugaskan kepada Anda."
        "active driver" in raw -> "Akun driver belum siap menjalankan trip."
        else -> "Data perjalanan belum dapat diperbarui. Silakan coba lagi."
    }
}
