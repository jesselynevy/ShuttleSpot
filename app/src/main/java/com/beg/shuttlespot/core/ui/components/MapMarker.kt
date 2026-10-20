package com.beg.shuttlespot.core.ui.components

enum class MarkerKind { Stop, Pickup, Destination, Bus }

data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val kind: MarkerKind = MarkerKind.Stop,
    /** Judul pop up saat marker diklik. */
    val title: String = "",
    /** Jadwal yang ditampilkan di pop up. Null = baris jadwal disembunyikan. */
    val schedule: String? = null,
)
