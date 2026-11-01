package com.beg.shuttlespot.feature.passenger.boarding

// Overview: Defines boarding intent dialog for the passenger boarding area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.beg.shuttlespot.core.model.BoardingIntentStatus
import com.beg.shuttlespot.core.ui.components.AppButton
import com.beg.shuttlespot.core.ui.components.ButtonVariant
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

/**
 * Pop up "Konfirmasi naik bus". Menggantikan layar BoardingIntentScreen lama:
 * tidak ada route baru, cukup tampilkan saat state `showBoardingIntent` true.
 */
@Composable
// Renders the boarding intent dialog UI section.
fun BoardingIntentDialog(
    onDismiss: () -> Unit,
    viewModel: BoardingIntentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val confirmed = state.intent?.status in setOf(BoardingIntentStatus.CONFIRMED, BoardingIntentStatus.PENDING_SYNC)
    val containerColor = LocalExtendedColors.current.groupingColor

    when {
        state.tripId == null -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = containerColor,
            title = { Text("Konfirmasi naik bus") },
            text = { Text("Tidak ada perjalanan aktif untuk dikonfirmasi.") },
            confirmButton = { AppButton(text = "Tutup", onClick = onDismiss) },
        )

        confirmed -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = containerColor,
            title = { Text("Sudah dikonfirmasi") },
            text = { Text(if (state.intent?.status == BoardingIntentStatus.PENDING_SYNC) "Minat naik tersimpan di perangkat dan akan dikirim saat terhubung." else "Minat naik Anda sudah dikonfirmasi untuk bus ini.") },
            confirmButton = {
                AppButton(text = "Batalkan konfirmasi", onClick = { viewModel.cancel(); onDismiss() })
            },
            dismissButton = {
                AppButton(text = "Tutup", variant = ButtonVariant.Secondary, onClick = onDismiss)
            },
        )

        else -> AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = containerColor,
            title = { Text("Konfirmasi minat naik?") },
            text = { Text("Driver akan melihat satu minat naik untuk perjalanan dan halte ini.") },
            confirmButton = {
                // viewModel.confirm() sudah mengabaikan panggilan saat isSubmitting.
                AppButton(text = "Konfirmasi", onClick = { viewModel.confirm(); onDismiss() })
            },
            dismissButton = {
                AppButton(text = "Kembali", variant = ButtonVariant.Secondary, onClick = onDismiss)
            },
        )
    }
}
