package com.beg.shuttlespot.feature.passenger.announcement

// Overview: Defines announcement list item for the passenger announcement area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.model.AnnouncementTag
import com.beg.shuttlespot.core.model.AnnouncementTagKind
import com.beg.shuttlespot.core.ui.components.LabelColor
import com.beg.shuttlespot.core.ui.components.StatusBadge
import com.beg.shuttlespot.core.ui.theme.CircularShape
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** Satu baris pengumuman: label, judul + titik belum-dibaca + jam, lalu cuplikan isi. */
@Composable
// Renders the announcement list item UI section.
fun AnnouncementListItem(
    item: Announcement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val titleColor = if (item.isRead) scheme.onSurfaceVariant else scheme.onBackground
    val bodyColor = if (item.isRead) scheme.onSurfaceVariant.copy(alpha = 0.7f) else scheme.onSurfaceVariant

    Column(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (item.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.tags.forEach { tag -> StatusBadge(tag.label, tag.badgeColor()) }
                }
            }
            Row(verticalAlignment = Alignment.Top) {
                if (!item.isRead) {
                    Box(
                        Modifier
                            .padding(top = 7.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(CircularShape)
                            .background(scheme.primary)
                    )
                }
                Text(
                    text = item.title.ifBlank { item.sender },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = titleColor,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = formatPostedAt(item.postedAtEpochMillis),
                    style = MaterialTheme.typography.labelLarge,
                    color = titleColor,
                )
            }
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                color = bodyColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HorizontalDivider(color = scheme.outlineVariant)
    }
}

/** Warna chip: rute utama biru, umum kuning, rute lain bergantian hijau/ungu menurut namanya. */
private fun AnnouncementTag.badgeColor(): LabelColor = when (kind) {
    AnnouncementTagKind.MainRoute -> LabelColor.Blue
    AnnouncementTagKind.General -> LabelColor.Pending
    AnnouncementTagKind.Route -> if (abs(label.hashCode()) % 2 == 0) LabelColor.Success else LabelColor.Purple
}

// File-level constants and shared definitions.
private val timeFormatter = DateTimeFormatter.ofPattern("HH.mm")
private val dateFormatter = DateTimeFormatter.ofPattern("d MMM")

/** Hari ini: jam (09.20); hari lain: tanggal (12 Okt). */
private fun formatPostedAt(epochMillis: Long): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(epochMillis).atZone(zone)
    return if (dateTime.toLocalDate() == LocalDate.now(zone)) timeFormatter.format(dateTime) else dateFormatter.format(dateTime)
}
