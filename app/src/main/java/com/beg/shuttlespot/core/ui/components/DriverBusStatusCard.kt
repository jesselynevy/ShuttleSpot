package com.beg.shuttlespot.core.ui.components

// Overview: Defines driver bus status card for the ui components area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.model.TripLifecycle
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.core.ui.theme.CircularShape
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

// Defines the driver stop state type.
enum class DriverStopState { DONE, CURRENT, NEXT, UPCOMING }

// One halte row in the driver route progress list. Substitutable from real data later.
data class DriverStopUi(
    val name: String,
    val scheduledTime: String,
    val passengerCount: Int,
    val state: DriverStopState,
    val note: String? = null,
)

@Composable
// Renders the driver bus status card UI section.
fun DriverBusStatusCard(
    status: TripLifecycle,
    stops: List<DriverStopUi>,
    modifier: Modifier = Modifier,
    nextStopName: String? = null,
    eta: String? = null,
    collapsedVisibleCount: Int = 2,
    initiallyExpanded: Boolean = false,
    startEnabled: Boolean = true,
    startLabel: String = "Mulai Perjalanan",
    onStartTrip: () -> Unit = {},
    onLiveTrip: () -> Unit = {},
) {
    val extended = LocalExtendedColors.current
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        modifier = modifier
            .background(extended.groupingColor, MaterialTheme.shapes.medium)
            .border(BorderStroke(1.dp, extended.secondary3), MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .background(extended.groupingColor)
                .border(BorderStroke(1.dp, extended.secondary3), MaterialTheme.shapes.medium)
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (status) {
                TripLifecycle.NOT_STARTED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BusIconWithStatus()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Bus belum berjalan", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Mulailah perjalanan ketika anda sudah siap dan sudah pada waktunya.",
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.secondary2,
                            )
                        }
                    }
                    AppButton(
                        text = startLabel,
                        enabled = startEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onStartTrip,
                    )
                }
                TripLifecycle.COMPLETED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BusIconWithStatus()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Perjalanan Selesai", style = MaterialTheme.typography.titleSmall)
                            StatusBadge(text = "Selesai", color = LabelColor.Success)
                        }
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Halte Berikutnya",
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.secondary2,
                            )
                            Text(nextStopName ?: "-", style = MaterialTheme.typography.titleSmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "ETA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(eta ?: "-", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    AppButton(
                        text = "Lihat Posisi Bus Realtime",
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Secondary,
                        onClick = onLiveTrip,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Progres Rute",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            val visibleStops = if (expanded) stops else stops.take(collapsedVisibleCount)
            Column {
                visibleStops.forEach { stop -> DriverStopRow(stop) }
            }

            if (stops.size > collapsedVisibleCount) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Pendekkan daftar" else "Panjangkan daftar",
                        tint = extended.secondary2,
                    )
                }
            }
        }
    }
}

@Composable
// Renders one halte row in the driver route progress list.
private fun DriverStopRow(stop: DriverStopUi) {
    val extended = LocalExtendedColors.current
    val filledDot = stop.state != DriverStopState.UPCOMING
    val dotColor = if (filledDot) MaterialTheme.colorScheme.primary else extended.secondary3

    val (badgeText, badgeColor, badgeStyle) = when (stop.state) {
        DriverStopState.DONE -> Triple("Selesai", LabelColor.Success, LabelStyle.Soft)
        DriverStopState.CURRENT -> Triple("Sekarang", LabelColor.Success, LabelStyle.Solid)
        DriverStopState.NEXT -> Triple("Berikutnya", LabelColor.Pending, LabelStyle.Soft)
        DriverStopState.UPCOMING -> Triple("Belum", LabelColor.Grey, LabelStyle.Soft)
    }
    val countLabel = if (stop.state == DriverStopState.DONE) "Naik" else "Menunggu"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(extended.secondary3),
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .clip(CircularShape)
                    .background(if (filledDot) dotColor else MaterialTheme.colorScheme.background)
                    .border(2.dp, dotColor, CircularShape),
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                stop.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stop.scheduledTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = extended.secondary2,
                )
                Text(
                    "${stop.passengerCount} $countLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = extended.secondary2,
                )
            }
            if (stop.note != null) {
                Spacer(Modifier.height(2.dp))
                StatusBadge(text = stop.note, color = LabelColor.Pending, style = LabelStyle.Soft)
            }
        }

        StatusBadge(text = badgeText, color = badgeColor, style = badgeStyle)
    }
}

private val previewStops = listOf(
    DriverStopUi("Halte Pasar Modern BSD", "06:05", 2, DriverStopState.DONE),
    DriverStopUi("Halte Seberang Telkom", "06:12", 3, DriverStopState.CURRENT),
    DriverStopUi("Halte WTC Villa Melati", "ETA 06:20", 1, DriverStopState.NEXT, note = "Kemacetan ringan +4 mnt"),
    DriverStopUi("Halte Assobirin", "06:28", 0, DriverStopState.UPCOMING),
    DriverStopUi("Halte Bundaran Alam Sutera", "06:35", 2, DriverStopState.UPCOMING),
    DriverStopUi("Gerbang Gading Serpong", "06:42", 0, DriverStopState.UPCOMING),
)

@Preview(showBackground = true)
@Composable
// Interactive preview: tap "Mulai Perjalanan" to start, tap the bottom chevron to lengthen/shorten the list.
fun DriverBusStatusCardPreview() {
    ShuttleSpotTheme {
        var status by remember { mutableStateOf(TripLifecycle.NOT_STARTED) }
        Column(modifier = Modifier.padding(16.dp)) {
            DriverBusStatusCard(
                status = status,
                stops = previewStops,
                nextStopName = "Halte Jababeka II",
                eta = "3 menit",
                onStartTrip = { status = TripLifecycle.ACTIVE },
                onLiveTrip = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
// Static preview of the running state, list expanded.
fun DriverBusStatusCardActivePreview() {
    ShuttleSpotTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DriverBusStatusCard(
                status = TripLifecycle.ACTIVE,
                stops = previewStops,
                nextStopName = "Halte Jababeka II",
                eta = "3 menit",
                initiallyExpanded = true,
            )
        }
    }
}
