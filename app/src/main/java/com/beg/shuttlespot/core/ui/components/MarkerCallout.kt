package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

/**
 * Pop up kecil di samping marker: panah menunjuk ke marker (kiri), kartu berisi judul dan jadwal.
 * Penempatannya diatur oleh pemanggil (lihat OpenStreetMap).
 */
@Composable
fun MarkerCallout(
    title: String,
    schedule: String?,
    modifier: Modifier = Modifier,
) {
    val background = LocalExtendedColors.current.groupingColor
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(width = 8.dp, height = 16.dp)) {
            val arrow = Path().apply {
                moveTo(size.width, 0f)
                lineTo(0f, size.height / 2f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(arrow, color = background)
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = background,
            shadowElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (schedule != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = muted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Jadwal : ", style = MaterialTheme.typography.bodyMedium, color = muted)
                        Text(schedule, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkerCalloutPreview() {
    ShuttleSpotTheme(darkTheme = false) {
        MarkerCallout(title = "Halte Pasar Modern BSD", schedule = "06.15", modifier = Modifier.padding(16.dp))
    }
}
