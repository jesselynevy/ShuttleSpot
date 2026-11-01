package com.beg.shuttlespot.core.ui.components

// Overview: Defines pengumuman card for the ui components area of ShuttleSpot.

// Dependencies used by this file.
import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

@Composable
// Renders the pengumuman inside card UI section.
fun PengumumanInsideCard(item: Announcement, modifier: Modifier = Modifier) {
    val senderLabel = if (item.isAutomatic) "[Pesan Otomatis]" else item.sender
    val timeLabel = DateUtils.getRelativeTimeSpanString(
        item.postedAtEpochMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = LocalExtendedColors.current.groupingColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = senderLabel,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.alignByBaseline(),
                )
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Text(text = item.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
// Renders the pengumuman home card UI section.
fun PengumumanHomeCard(showedList: List<Announcement>) {
    Column(
        modifier = Modifier
            .background(LocalExtendedColors.current.groupingColor)
            .border(
                border = BorderStroke(1.dp, color = LocalExtendedColors.current.secondary3),
                shape = MaterialTheme.shapes.medium,
            )
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            showedList.forEach { item -> PengumumanInsideCard(item) }
        }
    }
}

// File-level constants and shared definitions.
private val previewAnnouncements = listOf(
    Announcement("1", "Sistem", "Bus baru saja jalan!", System.currentTimeMillis() - 10 * 60_000, isAutomatic = true),
    Announcement("2", "Pak Wahyu", "Hari ini Bus akan telat sekian menit karena ada yang lelet!", System.currentTimeMillis() - 10 * 60_000),
)

@Preview(showBackground = true)
@Composable
// Renders the pengumuman card preview UI section.
private fun PengumumanCardPreview() {
    ShuttleSpotTheme(darkTheme = false) { PengumumanHomeCard(previewAnnouncements) }
}
