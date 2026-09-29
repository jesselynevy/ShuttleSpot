package com.beg.shuttlespot.core.ui.theme

import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.staticCompositionLocalOf

data class ExtendedColors(
    val secondary1 : Color,
    val secondary2 : Color,
    val secondary3 : Color,
    val backgroundColor : Color,
    val groupingColor : Color
    )

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors (
        secondary1 = Color.Unspecified,
        secondary2 = Color.Unspecified,
        secondary3 = Color.Unspecified,
        backgroundColor = Color.Unspecified,
        groupingColor =  Color.Unspecified
    )
}

val extendedLight = ExtendedColors(
    secondary1 = ArabicGrey,
    secondary2 = SuperGrey,
    secondary3 = KawaiiSilver,
    backgroundColor = BackgroundWhite,
    groupingColor = FullWhite
)

val extendedDark = ExtendedColors(
    secondary1 = DarkestGray,
    secondary2 = MidGray,
    secondary3 = LightestGray,
    backgroundColor = DarkestColor,
    groupingColor = LighterDark
)