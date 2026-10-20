package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import com.beg.shuttlespot.core.ui.theme.AvatarShape
import java.time.LocalTime

/** "Selamat pagi/siang/sore/malam" sesuai jam perangkat. */
fun timeOfDayGreeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
    in 4..10 -> "Selamat pagi"
    in 11..14 -> "Selamat siang"
    in 15..17 -> "Selamat sore"
    else -> "Selamat malam"
}

/**
 * Header sapaan di atas wave: avatar + "Selamat pagi, <nama>".
 * Teks selalu putih karena ditaruh di atas latar biru (wave), baik mode terang maupun gelap.
 * Jika [avatar] null, ditampilkan lingkaran dengan inisial nama.
 */
@Composable
fun GreetingHeader(
    name: String,
    modifier: Modifier = Modifier,
    avatar: Painter? = null,
    greeting: String = timeOfDayGreeting(),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(AvatarShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (avatar != null) {
                Image(
                    painter = avatar,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
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
        Text(
            text = "$greeting, $name",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2BB5DD)
@Composable
private fun GreetingHeaderPreview() {
    ShuttleSpotTheme {
        GreetingHeader(name = "Angela Benedictin Sunny", modifier = Modifier.size(width = 320.dp, height = 60.dp))
    }
}
