package com.beg.shuttlespot.core.ui.components

// Overview: Defines map marker for the ui components area of ShuttleSpot.

enum class MarkerKind { Stop, Pickup, Destination, Bus }

// Defines the map marker type.
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
