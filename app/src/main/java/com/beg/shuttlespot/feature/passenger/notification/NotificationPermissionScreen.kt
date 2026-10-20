package com.beg.shuttlespot.feature.passenger.notification

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.components.StudentOnboardingProgressHeader
import com.beg.shuttlespot.core.ui.components.StudentOnboardingStep
import com.beg.shuttlespot.core.ui.components.WaveBackground
import com.beg.shuttlespot.core.ui.components.WaveStyle
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

/**
 * Explains the value of notification access before invoking Android's native
 * POST_NOTIFICATIONS dialog. The permission remains optional for onboarding.
 */
@Composable
fun NotificationPermissionScreen(onContinue: () -> Unit) {
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onContinue() else permissionDenied = true
    }
    val colors = MaterialTheme.colorScheme
    val extended = LocalExtendedColors.current

    WaveBackground(WaveStyle.Auth) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 32.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            ) {
                StudentOnboardingProgressHeader(StudentOnboardingStep.NOTIFICATIONS)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).background(extended.blueContainer, MaterialTheme.shapes.extraLarge),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.NotificationsActive, null, tint = extended.onBlueContainer)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Izinkan akses notifikasi",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
                Spacer(Modifier.height(12.dp))
                NotificationBenefitsCard()
                if (permissionDenied) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Izin belum diberikan. Anda tetap dapat mengaktifkannya nanti dari pengaturan.",
                        color = colors.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
            ) { Text(if (permissionDenied) "Coba aktifkan notifikasi lagi" else "Aktifkan notifikasi") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Lewati untuk saat ini")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
@Composable
private fun NotificationBenefitsCard() {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column {
            NotificationBenefit(Icons.Outlined.DirectionsBus, "Notifikasi Status Bus", "Dapatkan kabar saat bus mendekati atau tiba di halte Anda.")
            HorizontalDivider(color = colors.outlineVariant)
            NotificationBenefit(Icons.Outlined.Campaign, "Notifikasi Pengumuman", "Dapatkan pengumuman dari driver atau admin.")
        }
    }
}
@Composable
private fun NotificationBenefit(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
) {
    val extended = LocalExtendedColors.current
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = extended.onBlueContainer)
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
