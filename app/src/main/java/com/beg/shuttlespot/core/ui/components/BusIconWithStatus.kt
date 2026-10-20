package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.Icons
import androidx.compose.ui.tooling.preview.Preview
import com.beg.shuttlespot.core.ui.theme.AvatarShape
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

@Composable
fun BusIconWithStatus(
    modifier : Modifier = Modifier,
    showStatusDot : Boolean = true
    ){
    Box(modifier = modifier.size(48.dp)){
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(AvatarShape)
                .background(LocalExtendedColors.current.blueContainer),
            contentAlignment = Alignment.Center
        ){
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusIconWithStatusPreview(){
    BusIconWithStatus();
}
