package com.beg.shuttlespot.feature.passenger.route

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.components.AppButton
import com.beg.shuttlespot.core.ui.components.WaveBackground
import com.beg.shuttlespot.core.ui.components.WaveStyle
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

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
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
            AppButton(
                text = stringResource(R.string.registration_complete_action),
                modifier = Modifier.fillMaxWidth(),
                onClick = onFinished,
            )
        }
    }
}

@Composable
private fun Confetti(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }
    val extended = LocalExtendedColors.current
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.onErrorContainer,
        extended.onPendingContainer,
        extended.onSuccessContainer,
        extended.onPurpleContainer,
    )
    // Keep the center clear for the message; positions are fractions of the available canvas.
    val positions = listOf(
        0.08f to 0.16f, 0.21f to 0.08f, 0.11f to 0.43f, 0.24f to 0.30f,
        0.78f to 0.13f, 0.91f to 0.35f, 0.82f to 0.62f, 0.94f to 0.76f,
        0.15f to 0.76f, 0.27f to 0.91f, 0.70f to 0.88f, 0.88f to 0.96f,
    )
    Canvas(modifier) {
        positions.forEachIndexed { index, (x, y) ->
            val finalY = size.height * y
            val animatedY = finalY - size.height * 0.18f * (1f - progress.value)
            val center = Offset(size.width * x, animatedY)
            rotate(degrees = index * 37f + (1f - progress.value) * 180f, pivot = center) {
                drawRect(
                    color = colors[index % colors.size],
                    topLeft = Offset(center.x - 3.dp.toPx(), center.y - 5.dp.toPx()),
                    size = Size(6.dp.toPx(), 10.dp.toPx()),
                    alpha = progress.value,
                )
            }
        }
    }
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
