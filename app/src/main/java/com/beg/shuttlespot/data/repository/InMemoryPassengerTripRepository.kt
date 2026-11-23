package com.beg.shuttlespot.data.repository

// Overview: Defines in memory passenger trip repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.BoardingIntent
import com.beg.shuttlespot.core.model.BoardingIntentStatus
import com.beg.shuttlespot.core.model.EtaEstimate
import com.beg.shuttlespot.core.model.GeoPoint
import com.beg.shuttlespot.core.model.LiveLocation
import com.beg.shuttlespot.core.model.RouteSelection
import com.beg.shuttlespot.data.repository.PassengerStatusDeriver.ResolvedStop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
// Defines the in memory passenger trip repository type.
class InMemoryPassengerTripRepository @Inject constructor(
    private val routeRepository: RouteRepository,
) : PassengerTripRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Logika derivasi status + progress + sticky dipisah ke kelas murni yang dibagi dengan
    // streamer realtime (PassengerLocationStreamer). Instansi sendiri -> sticky demo terpisah.
    private val deriver = PassengerStatusDeriver()

    /**
     * Satu halte yang sudah "siap-pakai" untuk animasi: id/name/koordinat dari
     * [RouteRepository]-nya, plus [ResolvedStop.routeProgress]-nya di polyline sudah dihitung
     * sekali via [RoutePaths.bsdUmnProgressNear] supaya [busTickFlow] bisa langsung mendeteksi
     * "crossing a stop" tanpa menghitung ulang setiap tick.
     */
    private val stopsFlow: Flow<List<ResolvedStop>> =
        routeRepository.observeRouteDetails(ROUTE_ID)
            .map { details ->
                details?.stops.orEmpty()
                    .sortedBy { it.sequence }
                    .mapNotNull { stop ->
                        val lat = stop.latitude ?: return@mapNotNull null
                        val lng = stop.longitude ?: return@mapNotNull null
                        val point = GeoPoint(lat, lng)
                        ResolvedStop(
                            id = stop.id,
                            name = stop.name,
                            point = point,
                            routeProgress = RoutePaths.bsdUmnProgressNear(point),
                        )
                    }
            }
            .distinctUntilChanged()
    // Defines the bus tick type.
    private data class BusTick(val routeProgress: Float, val isAtStop: Boolean, val arrivedStopIndex: Int?)

    /**
     * DEMO: posisi bus bergerak sendiri dari halte pertama sampai yang terakhir (UMN),
     * berhenti sejenak ([DEMO_STOP_DWELL_MILLIS]) di tiap halte di antaranya, lalu
     * berhenti total begitu sampai -- supaya ada animasi untuk ditunjukkan, tanpa
     * backend/GPS asli. Daftar halte diambil dari parameter [stops] (yang sendirinya
     * berasal dari [RouteRepository])
     * Ganti seluruh blok ini dengan collector dari sumber posisi bus sesungguhnya (WebSocket/polling) saat sudah ada.
     */
    // Handles the bus tick flow responsibility.
    private fun busTickFlow(stops: List<ResolvedStop>): Flow<BusTick> = flow {
        if (stops.size < 2) return@flow // tidak ada yang bisa dianimasikan
        var routeProgress = 0f
        // Fase pembuka demo: bus masih menuju halte pertama, lalu tiba dan menunggu
        // sebelum berangkat. Ini menjaga urutan status penumpang tetap jelas.
        emit(BusTick(routeProgress, isAtStop = false, arrivedStopIndex = null))
        delay(DEMO_INITIAL_APPROACH_MILLIS)
        emit(BusTick(routeProgress, isAtStop = true, arrivedStopIndex = 0))
        delay(DEMO_STOP_DWELL_MILLIS)
        while (true) {
            delay(DEMO_TICK_MILLIS)
            val next = routeProgress + (DEMO_TICK_MILLIS.toFloat() / DEMO_FULL_ROUTE_DURATION_MILLIS)

            val arrivedIndex = stops.indexOfFirst { stop ->
                routeProgress < stop.routeProgress && next >= stop.routeProgress
            }.takeIf { it >= 0 }

            if (arrivedIndex != null) {
                routeProgress = stops[arrivedIndex].routeProgress
                emit(BusTick(routeProgress, isAtStop = true, arrivedStopIndex = arrivedIndex))
                delay(DEMO_STOP_DWELL_MILLIS)
                if (routeProgress >= 1f) break
                continue
            }

            routeProgress = next
            if (routeProgress >= 1f) break
            emit(BusTick(routeProgress, isAtStop = false, arrivedStopIndex = null))
        }
    }

    private val primarySelectionFlow: Flow<RouteSelection?> =
        routeRepository.observePrimarySelection()

    private val intent = MutableStateFlow<BoardingIntent?>(null)

    /**
     * Flow utama. `flatMapLatest` di luar: setiap kali daftar halte berubah (biasanya
     * cuma sekali di awal), restart `busTickFlow` dengan daftar baru -- jadi tidak ada
     * tick yang pakai daftar halte stale. `combine` di dalam: setiap bus tick, perubahan
     * halte pilihan user, atau perubahan boarding intent memicu `snapshotFor()` baru.
     */
    private val activeTrip: StateFlow<ActiveTripSnapshot?> =
        stopsFlow.flatMapLatest { stops ->
            if (stops.size < 2) flowOf<ActiveTripSnapshot?>(null)
            else combine(busTickFlow(stops), primarySelectionFlow, intent) { tick, selection, boardingIntent ->
                snapshotFor(tick, stops, selection, boardingIntent)
            }
        }.stateIn(repositoryScope, SharingStarted.Eagerly, null)

    /**
     * Menyusun [ActiveTripSnapshot] untuk user demo: hitung apakah user sudah konfirmasi naik,
     * lalu delegasikan derivasi status/progress/ETA ke [PassengerStatusDeriver] (dibagi dengan
     * streamer realtime), dan bungkus hasilnya dengan identitas bus demo (tripId/vehicleLabel).
     */
    // Handles the snapshot for responsibility.
    private fun snapshotFor(
        tick: BusTick,
        stops: List<ResolvedStop>,
        selection: RouteSelection?,
        boardingIntent: BoardingIntent?,
    ): ActiveTripSnapshot {
        val confirmed = selection != null &&
            boardingIntent?.tripId == TRIP_ID &&
            boardingIntent.stopId == selection.pickupStopId &&
            boardingIntent.status == BoardingIntentStatus.CONFIRMED

        val derived = deriver.derive(
            routeProgress = tick.routeProgress,
            isAtStop = tick.isAtStop,
            arrivedStopIndex = tick.arrivedStopIndex,
            stops = stops,
            pickupStopId = selection?.pickupStopId,
            confirmed = confirmed,
        )

        val now = System.currentTimeMillis()
        return ActiveTripSnapshot(
            tripId = TRIP_ID, routeId = ROUTE_ID, vehicleLabel = "D 8801 VBN",
            status = derived.status,
            progress = derived.progress,
            previousStopName = derived.previousStopName,
            nextStopName = derived.nextStopName,
            eta = EtaEstimate(minutes = derived.etaMinutes),
            lastUpdatedEpochMillis = now,
            location = LiveLocation(derived.point.latitude, derived.point.longitude, now),
        )
    }

    // Implements framework behavior for observe active trip.
    override fun observeActiveTrip(routeId: String): Flow<ActiveTripSnapshot?> =
        activeTrip.map { trip -> trip?.takeIf { it.routeId == routeId } }
    // Implements framework behavior for observe boarding intent.
    override fun observeBoardingIntent(tripId: String, stopId: String): Flow<BoardingIntent?> =
        intent.map { current -> current?.takeIf { it.tripId == tripId && it.stopId == stopId } }

    // Implements framework behavior for submit boarding intent.
    override suspend fun submitBoardingIntent(tripId: String, stopId: String, idempotencyKey: String): BoardingIntent {
        val existing = intent.value
        // One passenger can have only one intent for a trip and pickup stop. This makes
        // retries safe even if a caller creates a new idempotency key.
        if (existing?.tripId == tripId && existing.stopId == stopId) return existing
        return BoardingIntent(tripId, stopId, BoardingIntentStatus.CONFIRMED, idempotencyKey)
            .also { intent.value = it }
    }

    // Implements framework behavior for cancel boarding intent.
    override suspend fun cancelBoardingIntent(tripId: String, stopId: String) {
        if (intent.value?.tripId == tripId && intent.value?.stopId == stopId) {
            intent.value = intent.value?.copy(status = BoardingIntentStatus.CANCELLED)
        }
    }

    private companion object {
        /** Rute satu-satunya yang punya animasi demo saat ini. */
        const val ROUTE_ID = "bsd_umn"
        const val TRIP_ID = "trip-bsd-1"
        /** Jeda antar tick animasi. Makin kecil = makin halus, makin sering recompute/redraw. */
        const val DEMO_TICK_MILLIS = 25L
        /** Lama waktu (ms) bus menempuh SELURUH rute, halte pertama sampai UMN (dipercepat untuk demo). */
        const val DEMO_FULL_ROUTE_DURATION_MILLIS = 70_000L
        /** Lama bus "berhenti" tiap kali sampai di satu halte sebelum lanjut jalan. */
        const val DEMO_STOP_DWELL_MILLIS = 2_000L
        /** Lama bus tampak menuju halte pertama sebelum tiba, untuk membuka demo. */
        const val DEMO_INITIAL_APPROACH_MILLIS = 8_000L
    }
}
