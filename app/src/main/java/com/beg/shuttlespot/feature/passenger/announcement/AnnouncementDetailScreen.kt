package com.beg.shuttlespot.feature.passenger.announcement

// Overview: Defines announcement detail screen for the passenger announcement area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
// Renders the announcement detail screen UI section.
fun AnnouncementDetailScreen(id: String, onBack: () -> Unit, viewModel: AnnouncementViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val item = state.items.firstOrNull { it.id == id }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
        if (item == null) {
            Text("Pengumuman tidak ditemukan", style = MaterialTheme.typography.titleLarge)
        } else {
            Text(item.title.ifBlank { item.sender }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(item.sender, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.message, Modifier.padding(top = 20.dp), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
