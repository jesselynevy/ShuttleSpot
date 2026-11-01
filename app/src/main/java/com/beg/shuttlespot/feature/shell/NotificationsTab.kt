package com.beg.shuttlespot.feature.shell

// Overview: Defines notifications tab for the feature shell area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
// Dependencies used by this file.
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
// Dependencies used by this file.
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.beg.shuttlespot.core.model.AppNotification
// Dependencies used by this file.
import com.beg.shuttlespot.core.ui.components.EmptyState
import com.beg.shuttlespot.feature.passenger.notification.NotificationsViewModel

/** Personal/activity notifications are persisted separately from operational announcements. */
@Composable
// Renders the notifications tab UI section.
fun NotificationsTab(viewModel: NotificationsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Notifikasi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = viewModel::clear, enabled = state.items.isNotEmpty()) {
                Icon(Icons.Outlined.Delete, contentDescription = "Hapus semua notifikasi")
            }
        }
        if (state.items.isEmpty()) {
            EmptyState("Belum ada notifikasi", "Alert perjalanan personal akan muncul di sini.")
        } else {
            // Displays the scrollable content for this section.
            LazyColumn(contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.items, key = { it.id }) { notification ->
                    NotificationRow(notification, onClick = { viewModel.markRead(notification.id) })
                }
            }
        }
    }
}

@Composable
// Renders the notification row UI section.
private fun NotificationRow(item: AppNotification, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold)
        Text(item.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
