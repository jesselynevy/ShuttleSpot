package com.beg.shuttlespot.data.repository

// Overview: Defines announcement repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.Announcement
import kotlinx.coroutines.flow.Flow

// Defines the announcement repository type.
interface AnnouncementRepository {
    /** Pengumuman terbaru lebih dulu. */
    fun observeAnnouncements(): Flow<List<Announcement>>
}
