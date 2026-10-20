package com.beg.shuttlespot.feature.shell

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Route
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.AvatarHeader
import com.beg.shuttlespot.core.ui.components.ProfileBlockGroup
import com.beg.shuttlespot.core.ui.components.ProfileDataBlock
import com.beg.shuttlespot.core.ui.components.ProfileDataBlockActionType
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel
import com.beg.shuttlespot.feature.foundation.BaseColumn

/** Tab Profil (plan.md layar 18). Sign-out menghapus session; shell lalu mengembalikan pengguna ke auth. */
@Composable
fun ProfileTab(role: UserRole, viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val session = (state as? SessionUiState.Ready)?.session

    BaseColumn("Profil") {
        AvatarHeader(
            name = session?.displayName ?: "-",
            role = role,
            isGreeting = false,
            centerContent = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        ProfileBlockGroup("Informasi Pribadi") {
            ProfileDataBlock(
                icon = Icons.Outlined.Person,
                informationTitle = "Email",
                informationData = session?.contact?.takeIf { it.isNotBlank() } ?: "angelabenedictins@gmail.com",
                showDivider = true
            )
            ProfileDataBlock(
                icon = Icons.Outlined.Phone,
                informationTitle = "Nomor telepon",
                informationData = "628888888888888",
                actionType = ProfileDataBlockActionType.Edit,
                onActionClick = { /* Handle edit phone */ },
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

        Spacer(modifier = Modifier.height(16.dp))

        ProfileBlockGroup("Pengaturan") {
            ProfileDataBlock(
                icon = Icons.Outlined.AccessTime,
                informationTitle = "Notifikasi ETA",
                informationData = "10 menit · Sen Sel Kam",
                actionType = ProfileDataBlockActionType.Chevron,
                onClick = { /* Handle ETA notification settings */ },
                showDivider = true
            )
            ProfileDataBlock(
                icon = Icons.Outlined.Language,
                informationTitle = "Bahasa",
                informationData = "Bahasa Indonesia",
                actionType = ProfileDataBlockActionType.Chevron,
                onClick = { /* Handle language settings */ },
                showDivider = true
            )
            ProfileDataBlock(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                informationTitle = "Bantuan",
                actionType = ProfileDataBlockActionType.Chevron,
                onClick = { /* Handle help */ },
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ProfileBlockGroup("Lainnya") {
            ProfileDataBlock(
                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                informationTitle = "Keluar",
                actionType = ProfileDataBlockActionType.Button,
                onClick = viewModel::signOut,
                showDivider = false
            )
        }
    }
}
