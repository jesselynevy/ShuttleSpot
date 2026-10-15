package com.beg.shuttlespot.core.model

/** Jenis label pada pengumuman; warna chip ditentukan di lapisan UI. */
enum class AnnouncementTagKind { Route, MainRoute, General }

data class AnnouncementTag(
    val label: String,
    val kind: AnnouncementTagKind,
)

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
