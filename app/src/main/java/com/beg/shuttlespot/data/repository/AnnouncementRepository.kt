package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.Announcement
import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {
    /** Pengumuman terbaru lebih dulu. */
    fun observeAnnouncements(): Flow<List<Announcement>>
}
