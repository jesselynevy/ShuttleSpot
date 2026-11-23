package com.beg.shuttlespot.data.repository

// class for per passenger status derive functions

import com.beg.shuttlespot.core.model.GeoPoint
import com.beg.shuttlespot.core.model.TripStatus
class PassengerStatusDeriver {
    // halte siap pakai + routeProgress di Polyline yang sudah dihiutng skeali
    data class ResolvedStop(
        val id: String,
        val name: String,
        val point: GeoPoint,
        val routeProgress: Float,
    )

    // hasil derivasi
    data class DerivedTrip(
        val status: TripStatus,
        val progress: Float,
        val previousStopName: String,
        val nextStopName: String,
        val etaMinutes: Int,
        val point: GeoPoint,
    )

    @Volatile private var stickyStatus: TripStatus? = null
    @Volatile private var stickyForPickupStopId: String? = null

    //reset sticky status ketika reset/sudah selesai tripnya
    fun reset() {
        stickyStatus = null
        stickyForPickupStopId = null
    }

    // derivasi dari posisi gps mentah, sintesis isAtStop dari jarak bus ke halte terdekat ([STOP_RADIUS_METERS])
    // lalu panggil derive
    fun deriveFromPoint(
        busPoint: GeoPoint,
        stops: List<ResolvedStop>,
        pickupStopId: String?,
        confirmed: Boolean,
    ): DerivedTrip {
        val routeProgress = RoutePaths.bsdUmnProgressNear(busPoint)
        val nearest = stops.withIndex().minByOrNull { RoutePaths.haversineMeters(busPoint, it.value.point) }
        val isAtStop = nearest != null &&
            RoutePaths.haversineMeters(busPoint, nearest.value.point) <= STOP_RADIUS_METERS
        val arrivedStopIndex = if (isAtStop) nearest!!.index else null
        return derive(routeProgress, isAtStop, arrivedStopIndex, stops, pickupStopId, confirmed)
    }


    // arrivedStopIndex = index halte tempat berhenti bus sekarang, kalau null berarti sedang bergerak
    // confirmed = penumpang sudah konfirm naik di halte pilihannya
    fun derive(
        routeProgress: Float,
        isAtStop: Boolean,
        arrivedStopIndex: Int?,
        stops: List<ResolvedStop>,
        pickupStopId: String?,
        confirmed: Boolean,
    ): DerivedTrip {
        if (pickupStopId != stickyForPickupStopId) {
            // Ganti halte pilihan (atau baru pertama kali punya pilihan) -> status lama tidak relevan.
            stickyStatus = null
            stickyForPickupStopId = pickupStopId
        }

        val pickupIndex = pickupStopId
            ?.let { id -> stops.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 }
        val legIndex = currentLegIndex(routeProgress, stops)

        val status = when {
            stickyStatus != null -> stickyStatus!!
            pickupIndex == null -> if (isAtStop) TripStatus.AT_PICKUP else TripStatus.EN_ROUTE
            isAtStop && arrivedStopIndex == pickupIndex ->
                if (confirmed) TripStatus.RIDING.also { stickyStatus = it } else TripStatus.AT_PICKUP
            isAtStop -> TripStatus.WAITING_AT_OTHERSTOP
            routeProgress <= stops.first().routeProgress -> TripStatus.EN_ROUTE
            legIndex >= pickupIndex ->
                (if (confirmed) TripStatus.RIDING else TripStatus.MISSED).also { stickyStatus = it }
            else -> TripStatus.EN_ROUTE
        }

        return buildDerived(routeProgress, legIndex, stops, status)
    }

    private fun buildDerived(
        routeProgress: Float,
        legIndex: Int,
        stops: List<ResolvedStop>,
        status: TripStatus,
    ): DerivedTrip {
        val point = RoutePaths.bsdUmnPointAt(routeProgress)
        val legStart = stops[legIndex].routeProgress
        val legEnd = stops[legIndex + 1].routeProgress
        val legProgress = if (legEnd > legStart)
            ((routeProgress - legStart) / (legEnd - legStart)).coerceIn(0f, 1f) else 0f
        return DerivedTrip(
            status = status,
            progress = legProgress,
            previousStopName = stops[legIndex].name,
            nextStopName = stops[legIndex + 1].name,
            etaMinutes = ((1 - legProgress) * LEG_ETA_MINUTES).toInt().coerceAtLeast(1),
            point = point,
        )
    }

    private fun currentLegIndex(routeProgress: Float, stops: List<ResolvedStop>): Int {
        for (i in 0 until stops.lastIndex) {
            if (routeProgress <= stops[i + 1].routeProgress) return i
        }
        return (stops.lastIndex - 1).coerceAtLeast(0)
    }

    companion object {
        const val STOP_RADIUS_METERS = 40.0
        /** ETA kosmetik dalam ruas berjalan (legProgress 0 -> nilai ini menit, 1 -> 1 menit). */
        const val LEG_ETA_MINUTES = 3
    }
}
