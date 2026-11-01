package com.beg.shuttlespot.core.ui.components

// Overview: Defines profile block group for the ui components area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

@Composable
// Renders the profile block group UI section.
fun ProfileBlockGroup(
    titleHeader: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (titleHeader.isNotBlank()) {
            Text(
                text = titleHeader,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = LocalExtendedColors.current.groupingColor,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
// Renders the profile block group demo UI section.
fun ProfileBlockGroupDemo() {
    ShuttleSpotTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileBlockGroup("Informasi Pribadi") {
                ProfileDataBlock(
                    icon = Icons.Outlined.Person,
                    informationTitle = "Email",
                    informationData = "angelabenedictins@gmail.com",
                    showDivider = true
                )
                ProfileDataBlock(
                    icon = Icons.Outlined.Phone,
                    informationTitle = "Nomor telepon",
                    informationData = "628888888888888",
                    actionType = ProfileDataBlockActionType.Edit,
                    showDivider = true
                )
                ProfileDataBlock(
                    icon = Icons.Outlined.Route,
                    informationTitle = "Rute Utama",
                    informationData = "Rute BSD",
                    showDivider = true
                )
                ProfileDataBlock(
                    icon = Icons.Outlined.DirectionsBus,
                    informationTitle = "Halte kamu",
                    informationData = "Halte ABCD",
                    showDivider = false
                )
            }

            ProfileBlockGroup("Pengaturan", modifier = Modifier.padding(top = 16.dp)) {
                ProfileDataBlock(
                    icon = Icons.Outlined.AccessTime,
                    informationTitle = "Notifikasi ETA",
                    informationData = "10 menit · Sen Sel Kam",
                    actionType = ProfileDataBlockActionType.Chevron,
                    showDivider = true
                )
                ProfileDataBlock(
                    icon = Icons.Outlined.Language,
                    informationTitle = "Bahasa",
                    informationData = "Bahasa Indonesia",
                    actionType = ProfileDataBlockActionType.Chevron,
                    showDivider = true
                )
                ProfileDataBlock(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    informationTitle = "Bantuan",
                    actionType = ProfileDataBlockActionType.Chevron,
                    showDivider = false
                )
            }

            ProfileBlockGroup("Lainnya", modifier = Modifier.padding(top = 16.dp)) {
                ProfileDataBlock(
                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                    informationTitle = "Keluar",
                    actionType = ProfileDataBlockActionType.Button,
                    showDivider = false
                )
            }
        }
    }
}
