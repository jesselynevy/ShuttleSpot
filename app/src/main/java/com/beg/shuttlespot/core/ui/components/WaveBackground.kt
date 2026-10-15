package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.beg.shuttlespot.R
enum class WaveStyle { None, Auth, Main, Profile, Splash }

@Composable
fun WaveBackground(style: WaveStyle, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val res = when (style) {
        WaveStyle.None -> null
        WaveStyle.Auth -> if(dark) R.drawable.sign_in_dark else R.drawable.sign_in_light
        WaveStyle.Main -> if(dark) R.drawable.main_dark else R.drawable.main_light
        WaveStyle.Profile -> if (dark) R.drawable.profile_dark else R.drawable.profile_light
        WaveStyle.Splash -> if (dark) R.drawable.splash_dark else R.drawable.splash_light
    }
    Box(modifier.fillMaxSize()) {
        if (res != null) {
            Image(
                painterResource(res), null,
                modifier = if (style == WaveStyle.Auth) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.fillMaxWidth().align(Alignment.TopCenter)
                },
                contentScale = if (style == WaveStyle.Auth) {
                    ContentScale.FillBounds
                } else {
                    ContentScale.FillWidth
                },
            )
        }
        content()
    }
}
