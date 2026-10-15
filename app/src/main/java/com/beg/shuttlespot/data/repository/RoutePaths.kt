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
