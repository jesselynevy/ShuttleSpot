package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

@Composable fun DriverStatusStrip(
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

@Composable private fun DriverStatusDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(LocalExtendedColors.current.secondary3),
    )
}