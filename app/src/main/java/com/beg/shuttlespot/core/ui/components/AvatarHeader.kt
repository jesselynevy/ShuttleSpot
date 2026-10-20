package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.ui.theme.AvatarShape
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import java.time.LocalTime

/** "Selamat pagi/siang/sore/malam" sesuai jam perangkat. */
fun timeOfDayGreeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
    in 4..10 -> "Selamat pagi"
    in 11..14 -> "Selamat siang"
    in 15..17 -> "Selamat sore"
    else -> "Selamat malam"
}

/**
 * Header sapaan / profil: avatar + nama + role.
 * Bila [centerContent] true, seluruh komponen ditaruh di tengah layar (contoh: di Tab Profil).
 * Bila [centerContent] false, avatar di kiri dan teks mengisi sisa ruang (contoh: di Dashboard).
 */
@Composable
fun AvatarHeader(
    name: String,
    role: UserRole,
    modifier: Modifier = Modifier,
    avatar: Painter? = null,
    isGreeting: Boolean = true,
    centerContent: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centerContent) Arrangement.Center else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(AvatarShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (avatar != null) {
                Image(
                    painter = avatar,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = name.trim().firstOrNull()?.uppercase().orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = if (centerContent) Modifier else Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (isGreeting) "${timeOfDayGreeting()}, $name" else name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = if (role == UserRole.PASSENGER) "Mahasiswa" else "Driver",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2BB5DD)
@Composable
private fun AvatarHeaderPreview() {
    ShuttleSpotTheme {
        AvatarHeader(
            name = "Gavriel Donovan",
            role = UserRole.PASSENGER,
            isGreeting = true,
            centerContent = true
        )
    }
}
