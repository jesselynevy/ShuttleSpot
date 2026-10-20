package com.beg.shuttlespot.feature.shell

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.components.FilterDropdown
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import com.beg.shuttlespot.feature.foundation.BaseColumn

@Composable
fun NotificationsTab() {
    var selectedTimeFilter by remember { mutableStateOf("Semua") }
    val timeOptions = listOf("Semua", "Hari ini", "Minggu ini")

    BaseColumn ("Notifikasi") {
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
                onClick = { /* Clear notifications */ }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Hapus Notifikasi",
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Empty State Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier
                    .size(148.dp),
                painter = painterResource(R.drawable.mailbox),
                contentDescription = "Mailbox"
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
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsTabPreview() {
    ShuttleSpotTheme {
        NotificationsTab()
    }
}
