package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.BoardingIntent
import com.beg.shuttlespot.core.model.BoardingIntentStatus
import com.beg.shuttlespot.core.model.EtaEstimate
import com.beg.shuttlespot.core.model.GeoPoint
import com.beg.shuttlespot.core.model.LiveLocation
import com.beg.shuttlespot.core.model.TripStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryPassengerTripRepository @Inject constructor() : PassengerTripRepository {
    // Skop sendiri karena repository ini singleton seumur aplikasi (tidak dikaitkan ke
    // lifecycle layar manapun); tidak pernah dibatalkan karena memang tidak ada titik
    // berhentinya selama proses hidup.
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private data class DemoStop(val name : String, val point : GeoPoint)

    private val DEMO_STOPS = listOf(
        DemoStop("Halte Pasar Modern BSD", GeoPoint(-6.3045281, 106.6848223)),
        DemoStop("Halte Seberang Telkom", GeoPoint(-6.277908133873632, 106.66122527545618)),
        DemoStop("Halte depan Mall WTC Villa Melati Mas", GeoPoint(-6.271424137283743, 106.6554922228125)),
        DemoStop("Halte Assobirin", GeoPoint(-6.258629712030424, 106.65182741364757)),
        DemoStop("Halte Bundaran Alam Sutera", GeoPoint(-6.24412, 106.64886)),
        DemoStop("Gerbang Gading Serpong", GeoPoint(-6.23077, 106.64136)),
        DemoStop("Paramount", GeoPoint(-6.2307640413527325, 106.64040084798098)),
        DemoStop("Dormitory", GeoPoint(-6.251652290980524, 106.62814256878931)),
        DemoStop("UMN", GeoPoint(-6.2567379, 106.6183029)),
    )

    private val DEMO_STOP_ROUTE_PROGRESS: List<Float> =
        DEMO_STOPS.map { RoutePaths.bsdUmnProgressNear(it.point) }

    /**
     * DEMO: posisi bus bergerak sendiri bolak-balik di SATU ruas rute dulu -- dari halte
     * [DEMO_LEG_START_POINT] (Halte Assobirin) ke [DEMO_LEG_END_POINT] (Halte Bundaran Alam
     * Sutera) -- supaya ada animasi untuk ditunjukkan, tanpa backend/GPS asli. Belum seluruh
     * rute (lihat docs/bus-demo-animation.md). Ganti blok ini dengan collector dari sumber
     * posisi bus sesungguhnya (WebSocket/polling) saat sudah ada.
     */
    private val activeTrip: StateFlow<ActiveTripSnapshot?> = flow {
        // Nilai awal sudah dikirim lewat `initialValue` di bawah (stateIn), jadi di sini
        // tunggu satu tick dulu sebelum maju, baru emit -- supaya tidak mengirim ulang
        // titik awal yang sama persis.
        var routeProgress  = 0f
        var direction = 1f

        while (true) {
            delay(DEMO_TICK_MILLIS)
            val next = routeProgress + direction * (DEMO_TICK_MILLIS.toFloat() / DEMO_FULL_ROUTE_DURATION_MILLIS)

            val arrivedStopProgress = DEMO_STOP_ROUTE_PROGRESS.firstOrNull { stopP ->
                (direction > 0f && routeProgress < stopP && next >= stopP) || (direction < 0f && routeProgress > stopP && next <= stopP)
            }

            if (arrivedStopProgress != null) {
                routeProgress = arrivedStopProgress
                emit(snapshotAt(routeProgress))
                delay( 2_000L)               // jeda diam
                if(routeProgress >= 1f) break;
                continue
            }

            routeProgress  += direction * (DEMO_TICK_MILLIS.toFloat() / DEMO_FULL_ROUTE_DURATION_MILLIS)
            when {
                routeProgress  >= 1f -> { break; }
            }
            emit(snapshotAt(routeProgress))
        }
    }.stateIn(repositoryScope, SharingStarted.Eagerly, snapshotAt(0f))

    /**
     * Dibuat secara sinkron (bukan di dalam coroutine) supaya `activeTrip` SELALU punya nilai
     * awal yang valid sejak detik pertama -- collector (termasuk test yang memanggil `.first()`)
     * tidak perlu menunggu tick pertama dari coroutine di [repositoryScope] yang berjalan di
     * thread lain, jadi tidak ada race.
     *
     * [legProgress] (0f..1f) adalah posisi bus DI RUAS INI SAJA -- dipakai langsung sebagai
     * [ActiveTripSnapshot.progress] karena itu yang dibaca [BusRouteProgress] (0f = di titik
     * "Halte Sebelum Sekarang", 1f = di titik "Halte Setelah"). Untuk posisi di peta, nilai ini
     * dikonversi dulu ke progress SEPANJANG RUTE (bukan cuma ruas ini) lewat interpolasi antara
     * [DEMO_LEG_START_PROGRESS] dan [DEMO_LEG_END_PROGRESS], baru dicari titik jalannya lewat
     * [RoutePaths.bsdUmnPointAt].
     */
    private fun snapshotAt(routeProgress: Float): ActiveTripSnapshot {
        val now = System.currentTimeMillis()
        val point = RoutePaths.bsdUmnPointAt(routeProgress)

        val legIndex = currentLegIndex(routeProgress)
        val legStart = DEMO_STOP_ROUTE_PROGRESS[legIndex]
        val legEnd = DEMO_STOP_ROUTE_PROGRESS[legIndex + 1]
        val legProgress = if (legEnd > legStart)
            ((routeProgress - legStart) / (legEnd - legStart)).coerceIn(0f, 1f) else 0f

        return ActiveTripSnapshot(
            tripId = "trip-bsd-1", routeId = "bsd_umn", vehicleLabel = "D 8801 VBN",
            status = TripStatus.EN_ROUTE,
            progress = legProgress,
            nextStopName = DEMO_STOPS[legIndex + 1].name,
            eta = EtaEstimate(minutes = ((1 - legProgress) * DEMO_LEG_ETA_MINUTES).toInt().coerceAtLeast(1)),
            lastUpdatedEpochMillis = now,
            location = LiveLocation(point.latitude, point.longitude, now),
        )
    }

    private fun currentLegIndex(routeProgress : Float) : Int {
        for ( i in 0 until DEMO_STOP_ROUTE_PROGRESS.lastIndex){
            if(routeProgress <= DEMO_STOP_ROUTE_PROGRESS[i + 1]) return i
        }
        return DEMO_STOP_ROUTE_PROGRESS.lastIndex - 1;
    }

    private val intent = MutableStateFlow<BoardingIntent?>(null)

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
        /** Jeda antar tick animasi. Makin kecil = makin halus, makin sering recompute/redraw. */
        const val DEMO_TICK_MILLIS = 25L
        const val DEMO_FULL_ROUTE_DURATION_MILLIS = 70_000L
        /** Lama waktu (ms) bus menempuh SATU RUAS ini (dipercepat untuk demo). */
        const val DEMO_LEG_DURATION_MILLIS = 16_000L
        /** ETA kosmetik kalau bus baru berangkat dari halte awal ruas ini (legProgress = 0). */
        const val DEMO_LEG_ETA_MINUTES = 3
        /** Nama halte tujuan ruas demo ini -- cocokkan manual kalau titiknya diganti. */
        const val DEMO_LEG_END_STOP_NAME = "Halte Bundaran Alam Sutera"
        val DEMO_LEG_START_POINT = GeoPoint(-6.258629712030424, 106.65182741364757)
        val DEMO_LEG_END_POINT = GeoPoint(-6.24412, 106.64886)

        /**
         * Posisi [DEMO_LEG_START_POINT]/[DEMO_LEG_END_POINT] sebagai fraksi progress SEPANJANG
         * RUTE (bukan cuma ruas ini) -- dihitung sekali lewat pencarian titik terdekat di
         * polyline jalan, bukan ditulis manual, supaya tetap akurat kalau [RoutePaths.BSD_UMN]
         * berubah.
         */
        val DEMO_LEG_START_PROGRESS: Float = RoutePaths.bsdUmnProgressNear(DEMO_LEG_START_POINT)
        val DEMO_LEG_END_PROGRESS: Float = RoutePaths.bsdUmnProgressNear(DEMO_LEG_END_POINT)
    }
}
