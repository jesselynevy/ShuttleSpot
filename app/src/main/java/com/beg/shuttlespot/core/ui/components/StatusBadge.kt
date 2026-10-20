package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.StatusPillShape

enum class LabelColor{
    Success, Pending, Failed, Blue, Purple, Grey
}

enum class LabelStyle{
    Soft,
    Solid
}

@Composable
fun StatusBadge(text: String, color : LabelColor, style : LabelStyle = LabelStyle.Soft){
    val (containerColor, contentColor) = getLabelColors(color, style)
    Surface(
        color = containerColor,
        shape = StatusPillShape,
        contentColor = contentColor,
        border = if (style == LabelStyle.Soft) {
            BorderStroke(1.dp, contentColor.copy(alpha = 0.25f))
        } else null
    ){
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun getLabelColors(color : LabelColor, style : LabelStyle) : Pair<Color, Color> {
    val extended = LocalExtendedColors.current
    val scheme = MaterialTheme.colorScheme
    val (soft, content)  = when(color) {
        LabelColor.Success -> extended.successContainer to extended.onSuccessContainer
        LabelColor.Pending -> extended.pendingContainer to extended.onPendingContainer
        LabelColor.Failed -> scheme.errorContainer to scheme.onErrorContainer
        LabelColor.Blue -> extended.blueContainer to extended.onBlueContainer
        LabelColor.Purple -> extended.purpleContainer to extended.onPurpleContainer
        LabelColor.Grey -> extended.greyContainer to extended.onGreyContainer
    }

    return if(style == LabelStyle.Soft) soft to content else content to soft;
}
