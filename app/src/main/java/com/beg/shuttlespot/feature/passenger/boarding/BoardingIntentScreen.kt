 package com.beg.shuttlespot.feature.passenger.boarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.model.BoardingIntentStatus

@Composable
fun BoardingIntentScreen(onDone: () -> Unit, viewModel: BoardingIntentViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var showConfirm by remember { mutableStateOf(false) }
    var showCancel by remember { mutableStateOf(false) }
    val confirmed = state.intent?.status == BoardingIntentStatus.CONFIRMED

    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.spacedBy(16.dp)) {
        Text("Konfirmasi akan naik", style = MaterialTheme.typography.headlineMedium)
        when {
            state.tripId == null -> Text("Tidak ada perjalanan aktif untuk dikonfirmasi.")
            confirmed -> {
                Text("Minat naik Anda sudah dikonfirmasi untuk bus ini.")
                Text("Satu konfirmasi disimpan untuk perjalanan dan halte pilihan Anda.", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { showCancel = true }, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) { Text("Batalkan konfirmasi") }
            }
            state.intent?.status == BoardingIntentStatus.CANCELLED -> Text("Konfirmasi naik telah dibatalkan.")
            else -> {
                Text("Beri tahu driver bahwa Anda menunggu di halte pilihan.")
                Button(onClick = { showConfirm = true }, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth()) { Text("Konfirmasi akan naik") }
            }
        }
        if (state.isSubmitting) CircularProgressIndicator()
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Kembali ke pelacak") }
    }
    if (showConfirm) AlertDialog(
        onDismissRequest = { showConfirm = false },
        title = { Text("Konfirmasi minat naik?") },
        text = { Text("Driver akan melihat satu minat naik untuk perjalanan dan halte ini.") },
        confirmButton = { Button(onClick = { showConfirm = false; viewModel.confirm() }) { Text("Konfirmasi") } },
        dismissButton = { OutlinedButton(onClick = { showConfirm = false }) { Text("Kembali") } }
    )
    if (showCancel) AlertDialog(
        onDismissRequest = { showCancel = false },
        title = { Text("Batalkan konfirmasi?") },
        text = { Text("Driver tidak lagi melihat minat naik Anda untuk perjalanan ini.") },
        confirmButton = { Button(onClick = { showCancel = false; viewModel.cancel() }) { Text("Batalkan") } },
        dismissButton = { OutlinedButton(onClick = { showCancel = false }) { Text("Kembali") } }
    )
}
