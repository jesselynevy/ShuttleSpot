package com.beg.shuttlespot.core.ui.components

// Overview: Defines trip summary card for the ui components area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

/** Status keikutsertaan penumpang hari ini. */
enum class TodayRideStatus(val label: String, val color: LabelColor) {
    Riding("Naik", LabelColor.Success),
    NotRiding("Tidak Naik", LabelColor.Failed),
    Undecided("Belum Konfirmasi", LabelColor.Pending),
}

/**
 * Kartu ringkas tiga kolom: Rute Utama | Pickup Utama | Hari ini naik?
 * Stateless: semua data dikirim dari pemanggil.
 */
@Composable
// Renders the trip summary card UI section.
fun TripSummaryCard(
    routeName: String,
    pickupStop: String,
    todayStatus: TodayRideStatus,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = LocalExtendedColors.current.groupingColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryColumn("Rute Utama", Modifier.weight(1f)) {
                Text(routeName, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
            SummaryDivider()
            SummaryColumn("Pickup Utama", Modifier.weight(1.2f)) {
                Text(pickupStop, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
            SummaryDivider()
            SummaryColumn("Hari ini naik?", Modifier.weight(1.2f)) {
                StatusBadge(text = todayStatus.label, color = todayStatus.color)
            }
        }
    }
}

@Composable
// Renders the summary column UI section.
private fun SummaryColumn(title: String, modifier: Modifier, value: @Composable () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
        value()
    }
}

@Composable
// Renders the summary divider UI section.
private fun SummaryDivider() {
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Preview(showBackground = true)
@Composable
// Renders the trip summary card preview UI section.
private fun TripSummaryCardPreview() {
    ShuttleSpotTheme {
        TripSummaryCard(
            routeName = "BSD",
            pickupStop = "Halte ABCD",
            todayStatus = TodayRideStatus.NotRiding,
            modifier = Modifier.padding(16.dp),
        )
    }
}
