package com.beg.shuttlespot.feature.shell

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.feature.auth.SessionUiState
import com.beg.shuttlespot.feature.auth.SessionViewModel
import com.beg.shuttlespot.feature.foundation.BaseColumn

/** Tab Profil (plan.md layar 18). Sign-out menghapus session; shell lalu mengembalikan pengguna ke auth. */
@Composable
fun ProfileTab(role: UserRole, viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val session = (state as? SessionUiState.Ready)?.session

    BaseColumn("Profil") {
        Text(session?.displayName ?: "-")
        Text(if (role == UserRole.DRIVER) "Driver" else "Mahasiswa")
        session?.contact?.let { Text(it) }
        OutlinedButton(onClick = viewModel::signOut, modifier = Modifier.fillMaxWidth()) { Text("Keluar") }
    }
}
