package com.beg.shuttlespot.feature.shell

import androidx.compose.runtime.Composable
import com.beg.shuttlespot.core.ui.components.PlaceholderScreen

/** Tab Notifikasi. Sama untuk semua role. Daftar pengumuman ada di layar terpisah (PassengerFlowRoute.ANNOUNCEMENTS). */
@Composable
fun NotificationsTab() {
    PlaceholderScreen("Notifikasi", "Alert personal akan membuka detail perjalanan terkait.")
}
