package com.beg.shuttlespot.feature.passenger.notification

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.beg.shuttlespot.core.ui.components.ErrorState
import com.beg.shuttlespot.feature.foundation.BaseColumn

/**
 * Meminta izin POST_NOTIFICATIONS (hanya ada di Android 13+; pemanggil melewati layar ini di versi lebih lama).
 * Izin ini opsional: pengguna tetap bisa lanjut walau menolak atau memilih lewati.
 */
@Composable
fun NotificationPermissionScreen(onContinue: () -> Unit) {
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onContinue() else permissionDenied = true
    }

    BaseColumn("Aktifkan notifikasi") {
        Text("Notifikasi dipakai untuk memberi tahu saat bus sudah dekat atau sudah tiba di halte Anda.")
        if (permissionDenied) ErrorState("Izin notifikasi belum diberikan. Anda tetap dapat melanjutkan dan mengaktifkannya nanti dari pengaturan.")
        Button(
            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (permissionDenied) "Coba aktifkan notifikasi lagi" else "Aktifkan notifikasi")
        }
        OutlinedButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Lewati untuk sekarang") }
    }
}
