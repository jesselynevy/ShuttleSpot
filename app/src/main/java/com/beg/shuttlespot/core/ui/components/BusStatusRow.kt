package com.beg.shuttlespot.core.ui.components

// Overview: Defines bus status row for the ui components area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.model.TripStatus
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

@Composable
// Renders the bus status row UI section.
fun BusStatusRow(
    type: TripStatus,
    /** Konteks halte: untuk [TripStatus.EN_ROUTE] = halte tujuan bus; untuk [TripStatus.WAITING_AT_OTHERSTOP] = halte tempat bus sedang berhenti. */
    stopName: String? = null,
    /** Estimasi tiba (dipakai [TripStatus.EN_ROUTE]). Null = pakai placeholder "XX:XX". */
    eta: String? = null,
    modifier: Modifier = Modifier
){
    val icon = when (type) {
        TripStatus.NO_ACTIVE_BUS, TripStatus.NOT_STARTED -> Icons.Default.Schedule
        TripStatus.MISSED -> Icons.Default.ErrorOutline
        else -> Icons.Default.Flag
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (type == TripStatus.MISSED) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (type == TripStatus.AT_PICKUP) LocalExtendedColors.current.secondary3.copy(alpha = 0.6f) else LocalExtendedColors.current.secondary3
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if(type == TripStatus.MISSED) MaterialTheme.colorScheme.onErrorContainer else LocalExtendedColors.current.secondary2,
                modifier = Modifier.size(16.dp)
            )

            val emphasisColor = MaterialTheme.colorScheme.onSurface
            val text = buildAnnotatedString {
                when (type) {
                    TripStatus.EN_ROUTE -> {
                        append("Bus dalam perjalanan ke ")
                        withStyle(SpanStyle(fontWeight = MaterialTheme.typography.titleSmall.fontWeight, color = emphasisColor)) { append(stopName ?: "haltemu") }
                        append(" \u00B7 estimasi tiba ")
                        withStyle(SpanStyle(fontWeight = MaterialTheme.typography.titleSmall.fontWeight, color = emphasisColor)) { append(eta ?: "XX:XX") }
                    }
                    TripStatus.AT_PICKUP -> {
                        append("Segera naik, bus sedang menunggu di ")
                        withStyle(SpanStyle(fontWeight = MaterialTheme.typography.titleSmall.fontWeight, color = emphasisColor)) { append("haltemu") }
                        append("!")
                    }
                    TripStatus.WAITING_AT_OTHERSTOP -> {
                        append("Bus sedang menunggu di ")
                        withStyle(SpanStyle(fontWeight = MaterialTheme.typography.titleSmall.fontWeight, color = emphasisColor)) { append(stopName ?: "halte lain") }
                    }
                    TripStatus.MISSED -> {
                        withStyle(SpanStyle(fontWeight = MaterialTheme.typography.titleSmall.fontWeight, color = MaterialTheme.colorScheme.onErrorContainer)) { append("Bus sudah melewati haltemu!") }
                    }
                    TripStatus.RIDING -> {

                    }
                    TripStatus.COMPLETED -> {
                        append("Perjalanan sudah selesai.")
                    }
                    TripStatus.NO_ACTIVE_BUS -> {
                        append("Jadwal berikutnya ")
                        withStyle(SpanStyle(fontWeight = MaterialTheme.typography.titleSmall.fontWeight, color = emphasisColor)) { append("TIME HERE") }
                    }
                    TripStatus.NOT_STARTED -> {
                        append("Bus belum mulai beroperasi hari ini.")
                    }
                }
            }

            Text(text = text,
                style = MaterialTheme.typography.bodySmall,
                color = LocalExtendedColors.current.secondary2)
        }
    }
}
