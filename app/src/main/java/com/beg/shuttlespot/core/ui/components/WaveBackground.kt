package com.beg.shuttlespot.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.theme.DarkestColor
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

enum class WaveStyle { None, Auth, Main, Profile, Splash }

@Composable
fun WaveBackground(style: WaveStyle, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dark = LocalExtendedColors.current.backgroundColor == DarkestColor

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = Pair(style, dark),
            animationSpec = tween(250),
            label = "WaveBackgroundCrossfade"
        ) { (currentStyle, isDark) ->
            val res = when (currentStyle) {
                WaveStyle.None -> null
                WaveStyle.Auth -> if (isDark) R.drawable.sign_in_dark else R.drawable.sign_in_light
                WaveStyle.Main -> if (isDark) R.drawable.main_dark else R.drawable.main_light
                WaveStyle.Profile -> if (isDark) R.drawable.profile_dark else R.drawable.profile_light
                WaveStyle.Splash -> if (isDark) R.drawable.splash_dark else R.drawable.splash_light
            }

            if (res != null) {
                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    modifier = if (currentStyle == WaveStyle.Auth) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.fillMaxWidth().align(Alignment.TopCenter)
                    },
                    contentScale = if (currentStyle == WaveStyle.Auth) {
                        ContentScale.FillBounds
                    } else {
                        ContentScale.FillWidth
                    },
                )
            }
        }

        content()
    }
}
