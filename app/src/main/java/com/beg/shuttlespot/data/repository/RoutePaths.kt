package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.GeoPoint

/**
 * Jalur jalan hasil OSRM (profil driving) yang disederhanakan (Douglas-Peucker, toleransi 2 m),
 * disimpan sebagai Encoded Polyline presisi 6 digit agar ringkas.
 */
internal object RoutePaths {
    /** Jalur rute bsd_umn: Pasar Modern BSD -> ... -> UMN (±20,3 km). */
    val BSD_UMN: List<GeoPoint> by lazy { decodePolyline(BSD_UMN_ENCODED) }

    private const val BSD_UMN_ENCODED = """rbx_KappnjEsl@hZI`BvPr^qDpF~KbTeDxAie@s}@{E{AoMrAyjIp`Eo^jLyf@nKiW~HqdBtw@}_D`rAikBty@caExdCkXpTs]l^gVzZoVr_@iXhl@y{@hxCeO|`AyAxg@f@n|@fJvlB~BrSdKlI|g@cCzRJh|@pF~J`Lf@pDmBhKuKvJom@zMkf@lCyoAmAmRdEkT`JgaA|{@wy@tn@wsAzy@{nBbbAsi@l^w[~YiQ~Uk`Aj{Ao_@d_@}KxNyCd@cA{AHeB~MwMjJgPrPyNzFsHzw@oqAjRcW|Zs[bRkOlUkNl_C_mAnk@g\ba@gXrqBgcBli@y]zH{KhKeTzCyBhHoCxz@}Dl^n@vp@lEtH`HbBzEo@jKyChEkH`Gom@zMkf@lCyoAmAmRdEkT`JgaA|{@wy@tn@wsAzy@{nBbbAsi@l^wNvL_LfLiQ~Ucr@biAw_@`e@{Qn[wHpIkJnCc`@lDymB~}@alBzaA{_@vLcNhBiVl@efG}F{SdAk\`H}oAd_@iwAbf@q}@b\egBhx@g\bJuyFzp@cmAzLcfB`UibAzJq{Gdx@_UbB}e@rGmj@zKi}M~bF_w@hh@q`@zZi{@z_AkQ~LuSrKgkCf~@qf@vVjTz|@[hHrb@~{AjAlKoD~IuM|H{j@nd@on@~t@eKbQmp@|wAS`CvAbGnLrLjAvEvBpBzyT~}FdPhH`c@nWjfC|d@~Er@hFkA~Gh@dU`Ut[nQ`oBjyA`OhKhShKp]xMfc@nKdqAvMdd@jGnoA``@bJcTzGkUxHwz@xEk[~D_Q|LqWwUsFvUrFfCyExKkLnClEeRnUmQbi@iMbqAiGxVqIbTfWjM|VhPxf@hb@nzAv_Bx`A|u@fgAbp@drBlx@I`FmFzAqbA_^mf@qUuH|Qi@hNhCd{@tD`l@bOl@fGfBgA`IgOeD"""
    // add the other routes here later, used OSRM

    /** Jarak kumulatif (meter) sepanjang [BSD_UMN], indeks sejajar dengan titiknya. Dihitung sekali. */
    val BSD_UMN_CUMULATIVE_METERS: DoubleArray by lazy { cumulativeMeters(BSD_UMN) }

    /**
     * Titik di jalan untuk fraksi [progress] (0f..1f) sepanjang rute, dengan interpolasi linear
     * antar titik terdekat. Dipakai untuk mengamimasikan posisi bus demo di sepanjang jalan,
     * bukan hanya di titik-titik halte.
     */
    fun bsdUmnPointAt(progress: Float): GeoPoint = pointAt(BSD_UMN, BSD_UMN_CUMULATIVE_METERS, progress)

    /** Jarak Haversine (meter) antar dua titik. */
    private fun haversineMeters(a: GeoPoint, b: GeoPoint): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = Math.sin(dLat / 2).let { it * it } +
            Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude)) *
            Math.sin(dLng / 2).let { it * it }
        return 2 * r * Math.asin(Math.sqrt(h.coerceIn(0.0, 1.0)))
    }

    /** Jarak kumulatif (meter) dari titik pertama ke tiap titik berikutnya. */
    internal fun cumulativeMeters(points: List<GeoPoint>): DoubleArray {
        val result = DoubleArray(points.size)
        for (i in 1 until points.size) {
            result[i] = result[i - 1] + haversineMeters(points[i - 1], points[i])
        }
        return result
    }

    /**
     * Interpolasi linear sepanjang [points] (urut) berdasarkan [cumulative] jarak meter-nya,
     * untuk fraksi total jarak [progress] (0f = titik pertama, 1f = titik terakhir).
     */
    internal fun pointAt(points: List<GeoPoint>, cumulative: DoubleArray, progress: Float): GeoPoint {
        if (points.isEmpty()) return GeoPoint(0.0, 0.0)
        if (points.size == 1 || cumulative.last() == 0.0) return points.first()
        val target = cumulative.last() * progress.coerceIn(0f, 1f)
        for (i in 1 until cumulative.size) {
            if (target <= cumulative[i]) {
                val segStart = cumulative[i - 1]
                val segLen = cumulative[i] - segStart
                val t = if (segLen == 0.0) 0.0 else (target - segStart) / segLen
                val a = points[i - 1]
                val b = points[i]
                return GeoPoint(
                    latitude = a.latitude + (b.latitude - a.latitude) * t,
                    longitude = a.longitude + (b.longitude - a.longitude) * t,
                )
            }
        }
        return points.last()
    }

    /**
     * Fraksi progress (0f..1f) di [BSD_UMN] untuk titik di polyline yang paling dekat dengan
     * [target]. Dipakai supaya kode pemanggil bisa bilang "progress di koordinat halte sekian"
     * tanpa perlu tahu indeksnya di polyline yang sudah disederhanakan.
     */
    fun bsdUmnProgressNear(target: GeoPoint): Float {
        if (BSD_UMN.isEmpty()) return 0f
        var bestIndex = 0
        var bestDistance = Double.MAX_VALUE
        BSD_UMN.forEachIndexed { index, point ->
            val distance = haversineMeters(point, target)
            if (distance < bestDistance) {
                bestDistance = distance
                bestIndex = index
            }
        }
        val total = BSD_UMN_CUMULATIVE_METERS.last()
        if (total <= 0.0) return 0f
        return (BSD_UMN_CUMULATIVE_METERS[bestIndex] / total).toFloat().coerceIn(0f, 1f)
    }

    /** Decoder Encoded Polyline Algorithm (presisi 1e6). */
    internal fun decodePolyline(encoded: String): List<GeoPoint> {
        val result = ArrayList<GeoPoint>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            for (axis in 0..1) {
                var shift = 0
                var acc = 0
                var b: Int
                do {
                    b = encoded[index++].code - 63
                    acc = acc or ((b and 0x1f) shl shift)
                    shift += 5
                } while (b >= 0x20)
                val delta = if (acc and 1 != 0) (acc shr 1).inv() else acc shr 1
                if (axis == 0) lat += delta else lng += delta
            }
            result.add(GeoPoint(lat / 1e6, lng / 1e6))
        }
        return result
    }
}
