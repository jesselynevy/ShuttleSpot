package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.BoardingIntent
import com.beg.shuttlespot.core.model.BoardingIntentStatus
import com.beg.shuttlespot.core.model.EtaEstimate
import com.beg.shuttlespot.core.model.GeoPoint
import com.beg.shuttlespot.core.model.LiveLocation
import com.beg.shuttlespot.core.model.RouteSelection
import com.beg.shuttlespot.core.model.TripStatus
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
class InMemoryPassengerTripRepository @Inject constructor(
    private val routeRepository: RouteRepository,
) : PassengerTripRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Satu halte yang sudah "siap-pakai" untuk animasi: id/name/koordinat dari
     * [RouteRepository]-nya, plus [routeProgress]-nya di polyline sudah dihitung sekali
     * via [RoutePaths.bsdUmnProgressNear] supaya [busTickFlow] bisa langsung mendeteksi
     * "crossing a stop" tanpa menghitung ulang setiap tick.
     */
    private data class ResolvedStop(
        val id: String,
        val name: String,
        val point: GeoPoint,
        val routeProgress: Float,
    )
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
    private data class BusTick(val routeProgress: Float, val isAtStop: Boolean, val arrivedStopIndex: Int?)

    /**
     * DEMO: posisi bus bergerak sendiri dari halte pertama sampai yang terakhir (UMN),
     * berhenti sejenak ([DEMO_STOP_DWELL_MILLIS]) di tiap halte di antaranya, lalu
     * berhenti total begitu sampai -- supaya ada animasi untuk ditunjukkan, tanpa
     * backend/GPS asli. Daftar halte diambil dari parameter [stops] (yang sendirinya
     * berasal dari [RouteRepository])
     * Ganti seluruh blok ini dengan collector dari sumber posisi bus sesungguhnya (WebSocket/polling) saat sudah ada.
     */
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

    // Status yang sudah "final" untuk user ini (MISSED/RIDING) tidak boleh berubah lagi walau
    // bus/konfirmasi terus berubah setelahnya -- lihat snapshotFor(). Direset kalau user ganti
    // halte pilihannya (pickupStopId berbeda dari sebelumnya), supaya tidak nyangkut salah konteks.
    @Volatile private var stickyStatus: TripStatus? = null
    @Volatile private var stickyForPickupStopId: String? = null

    /**
     * Flow utama. `flatMapLatest` di luar: setiap kali daftar halte berubah (biasanya
     * cuma sekali di awal), restart `busTickFlow` dengan daftar baru -- jadi tidak ada
     * tick yang pakai daftar halte stale. `combine` di dalam: setiap bus tick, perubahan
     * halte pilihan user, atau perubahan boarding intent memicu `snapshotFor()` baru.
     * `initialValue = null` karena nilai pertama butuh menunggu `stops` dari
     * [routeRepository] emit (yang di `InMemoryRouteRepository` sinkron, jadi dalam
     * praktek emisi pertama datang dalam ms, tapi tetap bukan sinkron secara formal --
     * sebelumnya ada `snapshotAt(0f)` sinkron karena stops hardcoded, sekarang tidak).
     */
    private val activeTrip: StateFlow<ActiveTripSnapshot?> =
        stopsFlow.flatMapLatest { stops ->
            if (stops.size < 2) flowOf<ActiveTripSnapshot?>(null)
            else combine(busTickFlow(stops), primarySelectionFlow, intent) { tick, selection, boardingIntent ->
                snapshotFor(tick, stops, selection, boardingIntent)
            }
        }.stateIn(repositoryScope, SharingStarted.Eagerly, null)

    /**
     * Menentukan [ActiveTripSnapshot.status] untuk user INI (bukan status bus secara generik),
     * berdasarkan posisi bus ([tick]), daftar halte rute ([stops]), halte pilihan user
     * ([selection]?.pickupStopId), dan apakah user sudah konfirmasi naik di halte itu
     * ([boardingIntent]):
     *
     * - Belum ada halte dipilih -> fallback generik: AT_PICKUP selagi bus berhenti di halte
     *   manapun, EN_ROUTE selama bergerak (perilaku lama, sebelum ada konsep "halte milik user").
     * - Bus berhenti PAS di halte pilihan user -> AT_PICKUP, atau RIDING kalau user sudah
     *   konfirmasi SEBELUM/SELAMA bus berhenti di situ.
     * - Bus berhenti di halte LAIN (bukan punya user) -> WAITING_AT_OTHERSTOP.
     * - Bus sudah betul-betul meninggalkan halte pilihan user (bukan cuma baru tiba) tanpa
     *   user konfirmasi -> MISSED, dan status ini "nempel" (sticky) seterusnya.
     * - Bus sudah lewat halte user DAN user sudah konfirmasi -> RIDING, juga sticky seterusnya.
     */
    private fun snapshotFor(
        tick: BusTick,
        stops: List<ResolvedStop>,
        selection: RouteSelection?,
        boardingIntent: BoardingIntent?,
    ): ActiveTripSnapshot {
        if (selection?.pickupStopId != stickyForPickupStopId) {
            // Ganti halte pilihan (atau baru pertama kali punya pilihan) -> status lama tidak relevan lagi.
            stickyStatus = null
            stickyForPickupStopId = selection?.pickupStopId
        }

        val pickupIndex = selection?.pickupStopId
            ?.let { id -> stops.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 }
        val confirmed = selection != null &&
            boardingIntent?.tripId == TRIP_ID &&
            boardingIntent.stopId == selection.pickupStopId &&
            boardingIntent.status == BoardingIntentStatus.CONFIRMED

        val legIndex = currentLegIndex(tick.routeProgress, stops)

        val status = when {
            stickyStatus != null -> stickyStatus!!
            pickupIndex == null -> if (tick.isAtStop) TripStatus.AT_PICKUP else TripStatus.EN_ROUTE
            tick.isAtStop && tick.arrivedStopIndex == pickupIndex ->
                if (confirmed) TripStatus.RIDING.also { stickyStatus = it } else TripStatus.AT_PICKUP
            tick.isAtStop -> TripStatus.WAITING_AT_OTHERSTOP
            tick.routeProgress <= stops.first().routeProgress -> TripStatus.EN_ROUTE
            legIndex >= pickupIndex ->
                (if (confirmed) TripStatus.RIDING else TripStatus.MISSED).also { stickyStatus = it }
            else -> TripStatus.EN_ROUTE
        }

        return buildSnapshot(tick.routeProgress, legIndex, stops, status)
    }

    /**
     * [legProgress] (0f..1f) dihitung lewat [currentLegIndex] -- posisi bus DI RUAS SEKARANG
     * SAJA (0f = baru berangkat dari halte sebelumnya, 1f = baru sampai di halte berikutnya) --
     * dipakai langsung sebagai [ActiveTripSnapshot.progress] karena itu yang dibaca
     * [com.beg.shuttlespot.core.ui.components.BusRouteProgress]. Untuk posisi di peta,
     * [routeProgress] (fraksi SEPANJANG SELURUH RUTE, bukan cuma satu ruas) dicari langsung
     * titik jalannya lewat [RoutePaths.bsdUmnPointAt].
     */
    private fun buildSnapshot(
        routeProgress: Float,
        legIndex: Int,
        stops: List<ResolvedStop>,
        status: TripStatus,
    ): ActiveTripSnapshot {
        val now = System.currentTimeMillis()
        val point = RoutePaths.bsdUmnPointAt(routeProgress)

        val legStart = stops[legIndex].routeProgress
        val legEnd = stops[legIndex + 1].routeProgress
        val legProgress = if (legEnd > legStart)
            ((routeProgress - legStart) / (legEnd - legStart)).coerceIn(0f, 1f) else 0f

        return ActiveTripSnapshot(
            tripId = TRIP_ID, routeId = ROUTE_ID, vehicleLabel = "D 8801 VBN",
            status = status,
            progress = legProgress,
            previousStopName = stops[legIndex].name,
            nextStopName = stops[legIndex + 1].name,
            eta = EtaEstimate(minutes = ((1 - legProgress) * DEMO_LEG_ETA_MINUTES).toInt().coerceAtLeast(1)),
            lastUpdatedEpochMillis = now,
            location = LiveLocation(point.latitude, point.longitude, now),
        )
    }

    private fun currentLegIndex(routeProgress: Float, stops: List<ResolvedStop>): Int {
        for (i in 0 until stops.lastIndex) {
            if (routeProgress <= stops[i + 1].routeProgress) return i
        }
        return (stops.lastIndex - 1).coerceAtLeast(0)
    }

    override fun observeActiveTrip(routeId: String): Flow<ActiveTripSnapshot?> =
        activeTrip.map { trip -> trip?.takeIf { it.routeId == routeId } }
    override fun observeBoardingIntent(tripId: String, stopId: String): Flow<BoardingIntent?> =
        intent.map { current -> current?.takeIf { it.tripId == tripId && it.stopId == stopId } }

    override suspend fun submitBoardingIntent(tripId: String, stopId: String, idempotencyKey: String): BoardingIntent {
        val existing = intent.value
        // One passenger can have only one intent for a trip and pickup stop. This makes
        // retries safe even if a caller creates a new idempotency key.
        if (existing?.tripId == tripId && existing.stopId == stopId) return existing
        return BoardingIntent(tripId, stopId, BoardingIntentStatus.CONFIRMED, idempotencyKey)
            .also { intent.value = it }
    }

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
        /** ETA kosmetik dalam ruas yang sedang dilalui (legProgress = 0 -> nilai ini, 1 -> 1 menit). */
        const val DEMO_LEG_ETA_MINUTES = 3
    }
}
