package com.beg.shuttlespot.data.location

import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import com.beg.shuttlespot.data.local.TripSnapshotEntity
import com.beg.shuttlespot.data.remote.RemoteTripStatusDto
import com.beg.shuttlespot.data.remote.RemoteVehicleLocationDto
import com.beg.shuttlespot.data.repository.RouteRepository
import com.beg.shuttlespot.data.repository.SessionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

// user hanya punya 1 subscription aktif sesaat, jika mengubah atau trip selesai, maka channel ditutup dan subscription baru dibuka untuk konteks baru.
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class PassengerLocationStreamer @Inject constructor(
    private val sessions: SessionRepository,
    private val routes: RouteRepository,
    private val clientProvider: Provider<SupabaseClient>,
    private val dao: ShuttleSpotDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeJob: Job? = null

    // dipanggil sekali saat Application.onCreate()
    fun start() {
        if (activeJob?.isActive == true) return
        activeJob = scope.launch {
            sessions.observeSession()
                .flatMapLatest { session ->
                    if (session == null || session.role != UserRole.PASSENGER) {
                        flowOf(null)
                    } else {
                        routes.observePrimarySelection()
                    }
                }
                .distinctUntilChanged { a, b -> a?.routeId == b?.routeId }
                .collectLatest { selection ->
                    // collectLatest otomatis batalin blok sebelumnya kalau upstream emit
                    // nilai baru -- jadi subscription lama auto-close saat ganti route.
                    val routeId = selection?.routeId ?: return@collectLatest
                    streamForRoute(routeId)
                }
        }
    }

    // jaga channel realtime pada routeId
    // flow luar : perubahan status `trips`
    // flow dalam : GPS events `vehicle_locations` untuk trip yang sedang aktif.
    private suspend fun streamForRoute(routeId: String) {
        val client = clientProvider.get()

        val tripsChannel: RealtimeChannel = client.channel("passenger-trips:$routeId")
        val tripFlow = tripsChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "trips"
            filter("route_id", FilterOperator.EQ, routeId)
        }
        tripsChannel.subscribe()

        // lookup awal, untuk scenario trip sudah active ketika aplikasi dibuka
        // ketika itu terjadi, kita tidak dapat event INSERT/UPDATE untuk itu
        // jadi ini query sekali untuk dijadikan snapshot
        val currentActiveTripId: String? = runCatching {
            client.postgrest["trips"]
                .select {
                    filter {
                        eq("route_id", routeId)
                        eq("status", "active")
                    }
                    limit(1)
                }
                .decodeList<RemoteTripStatusDto>()
                .firstOrNull()?.id
        }.getOrNull()

        var currentJob: Job? = currentActiveTripId?.let { tripId ->
            scope.launch { streamLocationsForTrip(tripId, routeId, client) }
        }

        // Dengar event trips di masa depan
        try {
            tripFlow.collect { action ->
                val row = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<RemoteTripStatusDto>()
                    is PostgresAction.Update -> action.decodeRecord<RemoteTripStatusDto>()
                    else -> null
                }
                row ?: return@collect

                if (row.status == "active") {
                    if (currentJob?.isActive != true) {
                        currentJob = scope.launch { streamLocationsForTrip(row.id, routeId, client) }
                    }
                } else {
                    // completed / not_started lagi -> stop streaming
                    currentJob?.cancel()
                    currentJob = null
                }
            }
        } finally {
            currentJob?.cancel()
            tripsChannel.unsubscribe()
            client.realtime.removeChannel(tripsChannel)
        }
    }

    // subscribe ke INSERT vehicle_locations yang trip_idnya cocok
    // lalu write ke tiap row ke Room agar UI update real-time
    private suspend fun streamLocationsForTrip(
        tripId: String,
        routeId: String,
        client: SupabaseClient,
    ) {
        val channel = client.channel("passenger-location:$tripId")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "vehicle_locations"
            filter(FilterOperation("trip_id", FilterOperator.EQ, tripId))
        }
        channel.subscribe()

        try {
            flow.collect { action ->
                val row = action.decodeRecord<RemoteVehicleLocationDto>()
                writeToRoom(row, routeId)
            }
        } finally {
            channel.unsubscribe()
            client.realtime.removeChannel(channel)
        }
    }

    // update kolom location di TripSnapshotEntity.
    // kalau snapshot belum ada, dibuatlah baris placeholder terlebih dahulu
    // yang kemudian diisi lebih lengkap oleh sinkronisasi lain
    private suspend fun writeToRoom(row: RemoteVehicleLocationDto, routeId: String) {
        val capturedAtMillis = runCatching { Instant.parse(row.capturedAt).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())

        val snapshot = dao.getTripSnapshot(routeId)

        val entity = snapshot?.copy(
            latitude = row.latitude,
            longitude = row.longitude,
            capturedAtEpochMillis = capturedAtMillis,
            updatedAtEpochMillis = System.currentTimeMillis(),
        ) ?: TripSnapshotEntity(
            tripId = row.tripId,
            routeId = routeId,
            vehicleLabel = "Unknown",
            status = com.beg.shuttlespot.core.model.TripStatus.EN_ROUTE.name,
            progress = 0f,
            previousStopName = null,
            nextStopName = null,
            etaMinutes = null,
            latitude = row.latitude,
            longitude = row.longitude,
            capturedAtEpochMillis = capturedAtMillis,
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
        dao.upsertTripSnapshot(entity)
    }
}