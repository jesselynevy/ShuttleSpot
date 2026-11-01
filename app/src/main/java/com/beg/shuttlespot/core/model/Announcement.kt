package com.beg.shuttlespot.core.model

// Overview: Defines announcement for the core model area of ShuttleSpot.

/** Jenis label pada pengumuman; warna chip ditentukan di lapisan UI. */
enum class AnnouncementTagKind { Route, MainRoute, General }

// Defines the announcement tag type.
data class AnnouncementTag(
    val label: String,
    val kind: AnnouncementTagKind,
)

// Defines the announcement type.
data class Announcement(
    val id: String,
    val sender: String,
    val message: String,
    val postedAtEpochMillis: Long,
    val isAutomatic: Boolean = false,
    /** Judul untuk daftar pengumuman; kosong = pakai [sender]. */
    val title: String = "",
    val tags: List<AnnouncementTag> = emptyList(),
    val isRead: Boolean = false,
)
