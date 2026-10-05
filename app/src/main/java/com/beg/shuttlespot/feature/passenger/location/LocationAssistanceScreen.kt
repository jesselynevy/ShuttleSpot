package com.beg.shuttlespot.feature.passenger.location

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

@Composable
fun LocationAssistanceScreen(onLocationGranted: () -> Unit, onChooseManually: () -> Unit) {
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onLocationGranted() else permissionDenied = true
    }

    BaseColumn("Cari halte terdekat") {
        Text("Lokasi hanya dipakai untuk merekomendasikan halte terdekat dan tidak disimpan sebagai pelacakan penumpang.")
        if (permissionDenied) ErrorState("Izin lokasi belum diberikan. Anda tetap dapat memilih halte secara manual atau mencoba mengaktifkan lokasi lagi.")
        Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (permissionDenied) "Coba aktifkan lokasi lagi" else "Aktifkan lokasi")
        }
        OutlinedButton(onClick = onChooseManually, modifier = Modifier.fillMaxWidth()) { Text("Pilih halte secara manual") }
    }
}
