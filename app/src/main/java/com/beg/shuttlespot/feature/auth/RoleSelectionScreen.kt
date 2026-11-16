package com.beg.shuttlespot.feature.auth

// Overview: Defines role selection screen for the feature auth area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

@Composable
// Renders the role selection screen UI section.
fun RoleSelectionScreen(
    onPassenger: () -> Unit,
    onDriver: () -> Unit,
) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    val colors = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 32.dp, vertical = 28.dp),
    ) {
        val compactLayout = maxHeight < 600.dp
        Column(
            Modifier.fillMaxWidth().heightIn(min = maxHeight).verticalScroll(scrollState),
        ) {
            Spacer(Modifier.height(if (compactLayout) 24.dp else 72.dp))
            Text(stringResource(R.string.role_selection_title), Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(if (compactLayout) 20.dp else 40.dp))
            RoleCard(
                title = stringResource(R.string.role_student),
                description = stringResource(R.string.role_student_description),
                avatarRes = R.drawable.mahasiswa_avatar,
                selected = selectedRole == UserRole.PASSENGER,
                tags = listOf(stringResource(R.string.role_student_tag_track), stringResource(R.string.role_student_tag_eta), stringResource(R.string.role_student_tag_notifications)),
                onClick = { selectedRole = UserRole.PASSENGER },
            )
            Spacer(Modifier.height(14.dp))
            RoleCard(
                title = stringResource(R.string.role_driver),
                description = stringResource(R.string.role_driver_description),
                avatarRes = R.drawable.driver_avatar,
                selected = selectedRole == UserRole.DRIVER,
                tags = listOf(stringResource(R.string.role_driver_tag_route), stringResource(R.string.role_driver_tag_schedule), stringResource(R.string.role_driver_tag_passengers)),
                onClick = { selectedRole = UserRole.DRIVER },
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = {
                    when (selectedRole) {
                        UserRole.PASSENGER -> onPassenger()
                        UserRole.DRIVER -> onDriver()
                        null -> Unit
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                enabled = selectedRole != null,
            ) { Text(stringResource(R.string.role_continue), style = MaterialTheme.typography.labelLarge) }
            Spacer(Modifier.height(24.dp))
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.welcome_language), Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
// Renders the role card UI section.
private fun RoleCard(title: String, description: String, avatarRes: Int, selected: Boolean, tags: List<String>, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) colors.primary.copy(alpha = 0.16f) else colors.surface, contentColor = colors.onSurface),
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(avatarRes), contentDescription = null, modifier = Modifier.size(96.dp), contentScale = ContentScale.Crop)
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                RoleTags(tags)
            }
        }
    }
}

@Composable
// Renders the role tags UI section.
private fun RoleTags(tags: List<String>) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalExtendedColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        RoleTag(tags[0], colors.errorContainer, colors.onErrorContainer)
        RoleTag(tags[1], extended.pendingContainer, extended.onPendingContainer)
    }
    Spacer(Modifier.height(4.dp))
    RoleTag(tags[2], extended.successContainer, extended.onSuccessContainer)
}

@Composable
// Renders the role tag UI section.
private fun RoleTag(text: String, containerColor: Color, contentColor: Color) {
    Surface(color = containerColor, contentColor = contentColor, shape = MaterialTheme.shapes.small) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}
