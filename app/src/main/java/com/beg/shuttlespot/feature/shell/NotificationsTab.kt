package com.beg.shuttlespot.feature.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.model.AnnouncementTag
import com.beg.shuttlespot.core.model.AnnouncementTagKind
import com.beg.shuttlespot.core.ui.components.FilterDropdown
import com.beg.shuttlespot.feature.passenger.announcement.AnnouncementListItem

private fun createInitialDummyNotifications(): List<Announcement> {
    val now = System.currentTimeMillis()
    return listOf(
        Announcement(
            id = "1",
            sender = "Admin Operations",
            message = "Dikarenakan perbaikan jalan di jalur BSD, beberapa bus shuttle akan mengalami keterlambatan sekitar 10-15 menit.",
            postedAtEpochMillis = now - 5 * 60_000L,
            title = "Bus BSD mengalami keterlambatan",
            tags = listOf(
                AnnouncementTag("Rute BSD", AnnouncementTagKind.Route),
                AnnouncementTag("Rute Utama", AnnouncementTagKind.MainRoute),
            ),
            isRead = false,
        ),
        Announcement(
            id = "2",
            sender = "Admin",
            message = "Untuk mengantisipasi lonjakan penumpang jam pulang kuliah, 1 armada bus tambahan telah dikerahkan ke Rute Serpong.",
            postedAtEpochMillis = now - 60 * 60_000L,
            title = "Penambahan Shuttle Rute Serpong",
            tags = listOf(
                AnnouncementTag("Rute Serpong", AnnouncementTagKind.Route),
            ),
            isRead = false,
        ),
        Announcement(
            id = "3",
            sender = "Sistem",
            message = "ShuttleSpot beroperasi setiap hari kerja (Senin - Jumat) pukul 06.30 hingga 18.00 WIB.",
            postedAtEpochMillis = now - 24 * 60 * 60_000L,
            title = "Jadwal Operasional Shuttle Campus",
            tags = listOf(
                AnnouncementTag("Umum", AnnouncementTagKind.General),
            ),
            isRead = true,
        ),
        Announcement(
            id = "4",
            sender = "Sistem",
            message = "Fitur pemantauan posisi bus real-time dan notifikasi ETA kini sudah aktif untuk seluruh rute kampus.",
            postedAtEpochMillis = now - 48 * 60 * 60_000L,
            title = "Pembaruan Layanan ShuttleSpot",
            tags = listOf(
                AnnouncementTag("Informasi", AnnouncementTagKind.General),
            ),
            isRead = true,
        ),
    )
}

@Composable
fun NotificationsTab() {
    var notifications by remember { mutableStateOf(createInitialDummyNotifications()) }
    var selectedTimeFilter by remember { mutableStateOf("Semua") }
    val timeOptions = listOf("Semua", "Hari ini", "Minggu ini")

    val filteredNotifications = remember(notifications, selectedTimeFilter) {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60_000L
        val oneWeek = 7 * oneDay
        when (selectedTimeFilter) {
            "Hari ini" -> notifications.filter { now - it.postedAtEpochMillis <= oneDay }
            "Minggu ini" -> notifications.filter { now - it.postedAtEpochMillis <= oneWeek }
            else -> notifications
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header Title over Wave
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Notifikasi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter and Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterDropdown(
                label = "Waktu",
                selected = selectedTimeFilter,
                options = timeOptions,
                optionLabel = { it },
                onSelected = { selectedTimeFilter = it }
            )

            IconButton(
                onClick = {
                    // Functional delete action for demo purposes
                    notifications = emptyList()
                },
                enabled = notifications.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Hapus Semua Notifikasi",
                    tint = if (notifications.isNotEmpty()) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredNotifications.isEmpty()) {
            // Empty State Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.mailbox),
                    contentDescription = "Mailbox Kosong",
                    modifier = Modifier.size(148.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Belum ada notifikasi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Notifikasi kamu akan muncul di sini saat kamu\nsudah menerima mereka",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            // Notification List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                items(filteredNotifications, key = { it.id }) { item ->
                    AnnouncementListItem(
                        item = item,
                        onClick = {
                            // Mark item as read on click
                            notifications = notifications.map {
                                if (it.id == item.id) it.copy(isRead = true) else it
                            }
                        }
                    )
                }
            }
        }
    }
}
