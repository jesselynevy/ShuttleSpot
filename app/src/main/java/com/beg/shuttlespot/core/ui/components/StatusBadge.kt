package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        shape = RoundedCornerShape(50),
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

private fun getLabelColors(color : LabelColor, style : LabelStyle) : Pair<Color, Color> {
    val (soft, content)  = when(color) {
        LabelColor.Success -> Color(0xFFC8F7D4) to Color(0xFF1B7A3D)
        LabelColor.Pending -> Color(0xFFFCE7A6) to Color(0xFF8A5A00)
        LabelColor.Failed -> Color(0xFFFAD2D2) to Color(0xFFB3261E)
        LabelColor.Blue -> Color(0xFFDEF4FF) to Color(0xFF1484C0)
        LabelColor.Purple -> Color(0xFFEBE0FF) to Color(0xFF7A40B8)
        LabelColor.Grey -> Color(0xFFDEDEDE) to Color(0xFF868686)
    }

    return if(style == LabelStyle.Soft) soft to content else content to soft;
}