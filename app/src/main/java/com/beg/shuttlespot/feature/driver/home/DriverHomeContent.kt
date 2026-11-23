package com.beg.shuttlespot.feature.driver.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.AvatarHeader
import com.beg.shuttlespot.core.ui.components.DriverBusStatusCard
import com.beg.shuttlespot.core.ui.components.DriverStopState
import com.beg.shuttlespot.core.ui.components.DriverStopUi
import com.beg.shuttlespot.core.ui.components.LabelColor
import com.beg.shuttlespot.core.ui.components.StatusBadge
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

@Composable
// Renders the driver home content UI section. All data arrives via parameters so it can be
// wired to a ViewModel later without changing this layout.
fun DriverHomeContent(
    driverName: String,
    plate: String,
    routeName: String,
    dutyStatus: String,
    shift: String,
    tripIndex: Int,
    tripTotal: Int,
    tripStatus: TripStatus,
    stops: List<DriverStopUi>,
    modifier: Modifier = Modifier,
    nextStopName: String? = null,
    eta: String? = null,
    avatar: Painter? = null,
    onStartTrip: () -> Unit = {},
    onLiveTrip: () -> Unit = {},
) {
    val extended = LocalExtendedColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AvatarHeader(name = driverName, role = UserRole.DRIVER, avatar = avatar)
            Text(
                text = "Plat $plate · Rute $routeName",
                style = MaterialTheme.typography.bodySmall,
                color = extended.secondary2,
            )
        }

        DriverStatusStrip(
            dutyStatus = dutyStatus,
            shift = shift,
            tripIndex = tripIndex,
            tripTotal = tripTotal,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Pantauan Perjalanan Anda", style = MaterialTheme.typography.titleSmall)
            DriverBusStatusCard(
                status = tripStatus,
                stops = stops,
                nextStopName = nextStopName,
                eta = eta,
                onStartTrip = onStartTrip,
                onLiveTrip = onLiveTrip,
            )
        }
    }
}

@Composable
// Renders the three-column driver status strip (Status / Shift / Trip ke-).
private fun DriverStatusStrip(
    dutyStatus: String,
    shift: String,
    tripIndex: Int,
    tripTotal: Int,
) {
    val extended = LocalExtendedColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(extended.groupingColor, MaterialTheme.shapes.medium)
            .border(BorderStroke(1.dp, extended.secondary3), MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DriverStatusCell(label = "Status") {
            StatusBadge(text = dutyStatus, color = LabelColor.Success)
        }
        DriverStatusDivider()
        DriverStatusCell(label = "Shift") {
            Text(shift, style = MaterialTheme.typography.titleSmall)
        }
        DriverStatusDivider()
        DriverStatusCell(label = "Trip ke-") {
            Text("$tripIndex / $tripTotal", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
// Renders one labelled cell inside the driver status strip.
private fun RowScope.DriverStatusCell(label: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = LocalExtendedColors.current.secondary2,
        )
        content()
    }
}

@Composable
// Renders the vertical separator between status cells.
private fun DriverStatusDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(LocalExtendedColors.current.secondary3),
    )
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
// Renders the driver home content preview (not started, list collapsed; tap to start / expand).
private fun DriverHomeContentPreview() {
    ShuttleSpotTheme {
        DriverHomeContent(
            driverName = "Pak Wahyu",
            plate = "B 7985 IG",
            routeName = "BSD",
            dutyStatus = "Bertugas",
            shift = "06:00 - 14:00",
            tripIndex = 4,
            tripTotal = 8,
            tripStatus = TripStatus.NOT_STARTED,
            stops = previewStops,
            nextStopName = "Halte Jababeka II",
            eta = "3 menit",
            avatar = painterResource(R.drawable.avatar),
        )
    }
}

@Preview(showBackground = true)
@Composable
// Renders the driver home content preview in the running state.
private fun DriverHomeContentActivePreview() {
    ShuttleSpotTheme {
        DriverHomeContent(
            driverName = "Pak Wahyu",
            plate = "B 7985 IG",
            routeName = "BSD",
            dutyStatus = "Bertugas",
            shift = "06:00 - 14:00",
            tripIndex = 4,
            tripTotal = 8,
            tripStatus = TripStatus.EN_ROUTE,
            stops = previewStops,
            nextStopName = "Halte Jababeka II",
            eta = "3 menit",
            avatar = painterResource(R.drawable.avatar),
        )
    }
}
