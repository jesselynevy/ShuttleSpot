package com.beg.shuttlespot.core.ui.theme

import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.staticCompositionLocalOf

data class ExtendedColors(
    val secondary1 : Color,
    val secondary2 : Color,
    val secondary3 : Color,
    val backgroundColor : Color,
    val groupingColor : Color,
    val successContainer : Color,
    val onSuccessContainer : Color,
    val pendingContainer : Color,
    val onPendingContainer : Color,
    val blueContainer : Color,
    val onBlueContainer : Color,
    val purpleContainer : Color,
    val onPurpleContainer : Color,
    val greyContainer : Color,
    val onGreyContainer : Color
    )

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors (
        secondary1 = Color.Unspecified,
        secondary2 = Color.Unspecified,
        secondary3 = Color.Unspecified,
        backgroundColor = Color.Unspecified,
        groupingColor =  Color.Unspecified,
        successContainer = Color.Unspecified,
        onSuccessContainer = Color.Unspecified,
        pendingContainer = Color.Unspecified,
        onPendingContainer = Color.Unspecified,
        blueContainer = Color.Unspecified,
        onBlueContainer = Color.Unspecified,
        purpleContainer = Color.Unspecified,
        onPurpleContainer = Color.Unspecified,
        greyContainer = Color.Unspecified,
        onGreyContainer = Color.Unspecified
    )
}

val extendedLight = ExtendedColors(
    secondary1 = ArabicGrey,
    secondary2 = SuperGrey,
    secondary3 = KawaiiSilver,
    backgroundColor = BackgroundWhite,
    groupingColor = FullWhite,
    successContainer = SuccessContainer,
    onSuccessContainer = OnSuccessContainer,
    pendingContainer = PendingContainer,
    onPendingContainer = OnPendingContainer,
    blueContainer = BlueContainer,
    onBlueContainer = OnBlueContainer,
    purpleContainer = PurpleContainer,
    onPurpleContainer = OnPurpleContainer,
    greyContainer = KawaiiSilver,
    onGreyContainer = ArabicGrey
)

val extendedDark = ExtendedColors(
    secondary1 = LightestGray,
    secondary2 = MidGray,
    secondary3 = DarkestGray,
    backgroundColor = DarkestColor,
    groupingColor = LighterDark,
    successContainer = SuccessContainer,
    onSuccessContainer = OnSuccessContainer,
    pendingContainer = PendingContainer,
    onPendingContainer = OnPendingContainer,
    blueContainer = BlueContainer,
    onBlueContainer = OnBlueContainer,
    purpleContainer = PurpleContainer,
    onPurpleContainer = OnPurpleContainer,
    greyContainer = KawaiiSilver,
    onGreyContainer = ArabicGrey
)