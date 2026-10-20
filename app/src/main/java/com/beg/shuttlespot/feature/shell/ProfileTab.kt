package com.beg.shuttlespot.feature.shell

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.components.AvatarHeader
import com.beg.shuttlespot.core.ui.components.ProfileDataBlock
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel
import com.beg.shuttlespot.feature.foundation.BaseColumn

/** Tab Profil (plan.md layar 18). Sign-out menghapus session; shell lalu mengembalikan pengguna ke auth. */
@Composable
fun ProfileTab(role: UserRole, viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val session = (state as? SessionUiState.Ready)?.session

    BaseColumn("Profil") {
        AvatarHeader(name = session?.displayName ?: "-", role = role, isGreeting = false)

        Text(
            "Informasi Pribadi",
            style = MaterialTheme.typography.titleMedium
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RectangleShape)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ProfileDataBlock(icon = Icons.Default.Email, informationTitle = "Email", informationData = session?.)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color.Gray))
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Nama: ${session?.displayName ?: "-"}")
            }
        }

        OutlinedButton(onClick = viewModel::signOut, modifier = Modifier.fillMaxWidth()) { Text("Keluar") }
    }
}
