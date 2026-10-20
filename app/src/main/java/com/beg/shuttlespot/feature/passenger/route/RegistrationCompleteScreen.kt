package com.beg.shuttlespot.feature.passenger.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.components.WaveBackground
import com.beg.shuttlespot.core.ui.components.WaveStyle
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import io.github.vinceglb.confettikit.compose.ConfettiKit
import io.github.vinceglb.confettikit.core.Angle
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.emitter.Emitter
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RegistrationCompleteScreen(onFinished: () -> Unit) {
    WaveBackground(WaveStyle.Auth) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
        ) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Confetti(Modifier.fillMaxWidth().height(240.dp))
                Text(
                    text = stringResource(R.string.registration_complete_message),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
            RoutePrimaryButton(
                text = stringResource(R.string.registration_complete_action),
                onClick = onFinished,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Confetti(modifier: Modifier = Modifier) {
    val extended = LocalExtendedColors.current
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.onErrorContainer,
        extended.onPendingContainer,
        extended.onSuccessContainer,
        extended.onPurpleContainer,
    ).map { it.toArgb() }
    val parties = remember(colors) {
        listOf(
            Party(
                angle = Angle.BOTTOM,
                spread = 70,
                speed = 2f,
                maxSpeed = 8f,
                colors = colors,
                timeToLive = 2500,
                position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0)),
                emitter = Emitter(duration = 500.milliseconds).max(40),
            ),
        )
    }
    ConfettiKit(modifier = modifier, parties = parties)
}

@Preview(showBackground = true)
@Composable
private fun RegistrationCompleteLightPreview() {
    ShuttleSpotTheme(darkTheme = false) { RegistrationCompleteScreen {} }
}

@Preview(showBackground = true)
@Composable
private fun RegistrationCompleteDarkPreview() {
    ShuttleSpotTheme(darkTheme = true) { RegistrationCompleteScreen {} }
}
