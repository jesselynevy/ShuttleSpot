package com.beg.shuttlespot.data.location

import com.beg.shuttlespot.core.model.BoardingIntentStatus
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
import com.beg.shuttlespot.core.model.GeoPoint
import com.beg.shuttlespot.core.model.RouteSelection
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.data.repository.PassengerStatusDeriver
import kotlinx.coroutines.flow.first
import com.beg.shuttlespot.data.repository.RoutePaths
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
    private val deriver = PassengerStatusDeriver()
    @Volatile private var currentSelection: RouteSelection? = null

    private fun blankEntity(row: RemoteVehicleLocationDto, routeId: String) = TripSnapshotEntity(
        tripId = row.tripId,
        routeId = routeId,
        vehicleLabel = "Unknown",
        status = TripStatus.EN_ROUTE.name,
        progress = 0f,
        previousStopName = null,
        nextStopName = null,
        etaMinutes = null,
        latitude = row.latitude,
        longitude = row.longitude,
        capturedAtEpochMillis = System.currentTimeMillis(),
        updatedAtEpochMillis = System.currentTimeMillis(),
    )

    // dipanggil sekali saat Application.onCreate()
    fun start() {
        if (activeJob?.isActive == true) return
        // log untuk kelihatan apakah WebSocket beneran CONNECTED atau stuck.
        scope.launch {
            val client = clientProvider.get()
            runCatching {
                android.util.Log.d("Streamer", "calling realtime.connect()...")
                client.realtime.connect()
                android.util.Log.d("Streamer", "realtime.connect() returned, status=${client.realtime.status.value}")
            }.onFailure { android.util.Log.e("Streamer", "realtime.connect() FAILED", it) }
            client.realtime.status.collect { status ->
                android.util.Log.d("Streamer", "REALTIME STATUS -> $status")
            }
        }

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
                    android.util.Log.d("Streamer", "selection changed: routeId=${selection?.routeId}")
                    currentSelection = selection

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
        android.util.Log.d("Streamer", "streamForRoute START route=$routeId, realtime.status=${client.realtime.status.value}")

        // Pastikan socket realtime nyambung dulu sebelum subscribe channel.
        runCatching { client.realtime.connect() }
            .onFailure { android.util.Log.e("Streamer", "realtime.connect() in streamForRoute FAILED", it) }
        android.util.Log.d("Streamer", "after connect() in streamForRoute, status=${client.realtime.status.value}")

        val tripsChannel: RealtimeChannel = client.channel("passenger-trips:$routeId")
        val tripFlow = tripsChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "trips"
            filter("route_id", FilterOperator.EQ, routeId)
        }
        android.util.Log.d("Streamer", "subscribing to trips channel...")
        tripsChannel.subscribe()
        android.util.Log.d("Streamer", "trips channel subscribed OK. realtime.status=${client.realtime.status.value}")

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
        }.onFailure { android.util.Log.e("Streamer", "initial trips query FAILED", it) }
            .getOrNull()
        android.util.Log.d("Streamer", "initial active trip lookup: tripId=$currentActiveTripId")

        var currentJob: Job? = currentActiveTripId?.let { tripId ->
            scope.launch { streamLocationsForTrip(tripId, routeId, client) }
        }

        // Dengar event trips di masa depan
        try {
            tripFlow.collect { action ->
                android.util.Log.d("Streamer", "trips event: ${action::class.simpleName}")
                val row = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<RemoteTripStatusDto>()
                    is PostgresAction.Update -> action.decodeRecord<RemoteTripStatusDto>()
                    else -> null
                }
                row ?: return@collect

                val mapped = when (row.status) {
                    "not_started" -> TripStatus.NOT_STARTED
                    "active" -> TripStatus.EN_ROUTE
                    "completed" -> TripStatus.COMPLETED
                    else -> TripStatus.NO_ACTIVE_BUS
                }

                when (row.status) {
                    // memeriksa apakah ada job yang sudah jalan pada rute aktif supaya menhindari
                    // double streaming
                    "active" -> if (currentJob?.isActive != true) {
                        currentJob = scope.launch { streamLocationsForTrip(row.id, routeId, client) }
                    }
                    // ketika status tidak aktif, bersihkan sticky status ( riding, terlewati )
                    else -> {
                        deriver.reset()
                        dao.getTripSnapshot(routeId)?.let {
                            dao.upsertTripSnapshot(it.copy(status = mapped.name, updatedAtEpochMillis = System.currentTimeMillis()))
                        }
                        currentJob?.cancel()
                        currentJob = null
                    }
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("Streamer", "streamForRoute trips.collect error", e)
            throw e
        } finally {
            android.util.Log.d("Streamer", "streamForRoute CLEANUP route=$routeId")
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
        val stops = routes.observeRouteDetails(routeId).first()?.stops.orEmpty()
            .sortedBy { it.sequence }
            .mapNotNull { stop ->
                val lat = stop.latitude ?: return@mapNotNull null
                val lng = stop.longitude ?: return@mapNotNull null
                val point = GeoPoint(lat, lng)
                PassengerStatusDeriver.ResolvedStop(stop.id, stop.name, point, RoutePaths.bsdUmnProgressNear(point))
            }
            .sortedBy { it.routeProgress }

        android.util.Log.d("Streamer", "streamLocationsForTrip START trip=$tripId")
        val channel = client.channel("passenger-location:$tripId")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "vehicle_locations"
            filter(FilterOperation("trip_id", FilterOperator.EQ, tripId))
        }
        channel.subscribe()
        android.util.Log.d("Streamer", "vehicle_locations channel subscribed OK trip=$tripId")

        try {
            flow.collect { action ->
                android.util.Log.d("Streamer", "LOCATION event: ${action.record}")
                val row = action.decodeRecord<RemoteVehicleLocationDto>()
                writeToRoom(row, routeId, stops)
                android.util.Log.d("Streamer", "wrote location to Room: lat=${row.latitude} lng=${row.longitude}")
            }
        } catch (e: Throwable) {
            android.util.Log.e("Streamer", "streamLocationsForTrip error trip=$tripId", e)
            throw e
        } finally {
            android.util.Log.d("Streamer", "streamLocationsForTrip CLEANUP trip=$tripId")
            channel.unsubscribe()
            client.realtime.removeChannel(channel)
        }
    }

    private suspend fun writeToRoom(
        row: RemoteVehicleLocationDto, routeId: String, stops: List<PassengerStatusDeriver.ResolvedStop>,
    ) {
        val capturedAtMillis = runCatching { Instant.parse(row.capturedAt).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())
        val now = System.currentTimeMillis()

        val selection = currentSelection?.takeIf { it.routeId == routeId }
        val confirmed = isBoardingConfirmed(row.tripId, selection?.pickupStopId)
        val derived = deriver.deriveFromPoint(
            GeoPoint(row.latitude, row.longitude),
            stops,
            selection?.pickupStopId,
            confirmed,
        )

        val base = dao.getTripSnapshot(routeId)
        dao.upsertTripSnapshot((base ?: blankEntity(row, routeId)).copy(
            status = derived.status.name,
            progress = derived.progress,
            previousStopName = derived.previousStopName,
            nextStopName = derived.nextStopName,
            etaMinutes = derived.etaMinutes,
            latitude = row.latitude, longitude = row.longitude,
            capturedAtEpochMillis = capturedAtMillis, updatedAtEpochMillis = now,
        ))
    }

    private suspend fun isBoardingConfirmed(tripId: String, pickupStopId: String?): Boolean {
        pickupStopId ?: return false
        val profileId = sessions.observeSession().first()?.profileId ?: return false
        val intent = dao.getBoardingIntent(profileId, tripId, pickupStopId) ?: return false
        return intent.status == BoardingIntentStatus.CONFIRMED.name
    }
}
