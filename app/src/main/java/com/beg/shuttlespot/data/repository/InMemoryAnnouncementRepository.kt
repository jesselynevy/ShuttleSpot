package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.model.AnnouncementTag
import com.beg.shuttlespot.core.model.AnnouncementTagKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Data demo sampai pengumuman tersambung ke backend. */
@Singleton
class InMemoryAnnouncementRepository @Inject constructor() : AnnouncementRepository {
    private val now = System.currentTimeMillis()
    private val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."

    private val announcements = MutableStateFlow(
        listOf(
            Announcement(
                id = "1", sender = "Admin", title = "Bus BSD akan mengalami pelambatan!", message = lorem,
                postedAtEpochMillis = now - 5 * MINUTE,
                tags = listOf(
                    AnnouncementTag("Rute BSD", AnnouncementTagKind.Route),
                    AnnouncementTag("Rute Utama", AnnouncementTagKind.MainRoute),
                ),
            ),
            Announcement(
                id = "2", sender = "Admin", title = "Pesan dari Admin blablabla", message = lorem,
                postedAtEpochMillis = now - 25 * MINUTE,
                tags = listOf(
                    AnnouncementTag("Rute TangCity", AnnouncementTagKind.Route),
                    AnnouncementTag("Umum", AnnouncementTagKind.General),
                ),
            ),
            Announcement(
                id = "3", sender = "Admin", title = "Pesan dari Admin blablabla", message = lorem,
                postedAtEpochMillis = now - 40 * MINUTE,
                tags = listOf(AnnouncementTag("Umum", AnnouncementTagKind.General)),
                isRead = true,
            ),
            Announcement(
                id = "4", sender = "Admin", title = "Pesan dari Admin [UDAHDIBACA]", message = lorem,
                postedAtEpochMillis = now - 90 * MINUTE,
                tags = listOf(AnnouncementTag("Umum", AnnouncementTagKind.General)),
                isRead = true,
            ),
        )
    )

    override fun observeAnnouncements(): Flow<List<Announcement>> = announcements.asStateFlow()

    private companion object {
        const val MINUTE = 60_000L
    }
}
