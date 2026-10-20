package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

@Composable
fun BusStatusRow(
    type: BusStatusType,
    haltCount: Int = 0,
    eta: String = "XX:XX",
    modifier: Modifier = Modifier
){
    val icon = when (type) {
        BusStatusType.NoBusOperating -> Icons.Default.Schedule
        BusStatusType.Passed -> Icons.Default.ErrorOutline
        else -> Icons.Default.Flag
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (type == BusStatusType.Passed) Color(0XFFD9A5AE) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (type == BusStatusType.WaitingAtStop) LocalExtendedColors.current.secondary3.copy(alpha = 0.6f) else LocalExtendedColors.current.secondary3
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
                tint = if(type == BusStatusType.Passed) Color(0XFF912A43) else LocalExtendedColors.current.secondary2,
                modifier = Modifier.size(16.dp)
            )

            val emphasisColor = MaterialTheme.colorScheme.onSurface
            val text = buildAnnotatedString {
                when (type) {
                    BusStatusType.PassedStops -> {
                        append("Melewati ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = emphasisColor)) { append("$haltCount halte") }
                        append(" lagi \u00B7 estimasi tiba ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = emphasisColor)) { append(eta) }
                    }
                    BusStatusType.WaitingAtStop -> {
                        append("Segera naik, bus sedang menunggu di ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = emphasisColor)) { append("haltemu") }
                        append("!")
                    }
                    BusStatusType.EnRoute -> {
                        append("Bus dalam perjalanan ke ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = emphasisColor)) { append("haltemu") }
                        append(" \u00B7 estimasi tiba ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = emphasisColor)) { append(eta) }
                    }
                    BusStatusType.Passed -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF912A43))) { append("Bus sudah melewati haltemu!") }
                    }
                    BusStatusType.NoBusOperating -> {
                        append("Jadwal berikutnya ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = emphasisColor)) { append("TIME HERE") }
                    }
                    BusStatusType.Riding -> {

                    }
                }
            }

            Text(text = text,
                style = MaterialTheme.typography.bodySmall,
                color = LocalExtendedColors.current.secondary2)
        }
    }
}
