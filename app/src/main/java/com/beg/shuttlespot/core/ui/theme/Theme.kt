package com.beg.shuttlespot.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SpringSky,
    onPrimary = DarkestColor,
    primaryContainer = UMNBlue,
    onPrimaryContainer = FullWhite,
    background = DarkestColor,
    onBackground = FullWhite,
    surface = LighterDark,
    onSurface = FullWhite,
    onSurfaceVariant = LightestGray,
    outline = MidGray,
    outlineVariant = DarkestGray,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = SpringSky,
    onPrimary = FullWhite,
    primaryContainer = UMNBlue,
    onPrimaryContainer = FullWhite,
    background = BackgroundWhite,
    onBackground = DarkestColor,
    surface = FullWhite,
    onSurface = DarkestColor,
    onSurfaceVariant = ArabicGrey,
    outline = SuperGrey,
    outlineVariant = KawaiiSilver,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
)

@Composable
fun ShuttleSpotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val extendedColors = if (darkTheme) extendedDark else extendedLight
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography
        ) {
            // Default text color for any Text without an explicit color
            // (FullWhite in dark mode, DarkestColor in light mode).
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onBackground,
                content = content
            )
        }
    }

}