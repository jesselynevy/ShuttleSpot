package com.beg.shuttlespot.core.ui.theme

// Overview: Defines shape for the ui theme area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Shared component geometry. Screens must consume these tokens through MaterialTheme. */
// File-level constants and shared definitions.
val ShuttleSpotShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** Semantic shape for circular avatars, markers, and icon buttons. */
val CircularShape: Shape = CircleShape

/** Alias for circular identity imagery. */
val AvatarShape: Shape = CircularShape

/** Semantic pill shape for compact status labels. */
val StatusPillShape: Shape = RoundedCornerShape(50)
