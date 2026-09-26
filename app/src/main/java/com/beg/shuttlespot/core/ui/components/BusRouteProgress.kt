package com.beg.shuttlespot.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.estimateAnimationDurationMillis
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

@Composable
fun BusRouteProgress(
    progress: Float,
    modifier: Modifier = Modifier
){
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "busProgress"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        val trackWidth = maxWidth

        // garis abu-abu
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(top = 7.dp)
                .fillMaxWidth()
                .height(2.dp)
                .background(LocalExtendedColors.current.secondary3)
        )

        // garis biru
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(top = 7.dp)
                .fillMaxWidth(animatedProgress)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary)
        )

        // dot halte awal
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 3.dp)
            ,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .background(MaterialTheme.colorScheme.background)
            )
            Text(text = "Halte Sebelum Sekarang",
                style = MaterialTheme.typography.labelMedium,
                color = LocalExtendedColors.current.secondary2)
        }

        // dot halte tujuan
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 3.dp)
            ,
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .border(2.dp, LocalExtendedColors.current.secondary3, CircleShape)
                    .background(MaterialTheme.colorScheme.background)
            )
            Text(text = "Halte Setelah",
                style = MaterialTheme.typography.labelMedium,
                color = LocalExtendedColors.current.secondary2

            )
        }


        // icon bus bergerak sesuai progress
        Box(
            modifier = Modifier
                .offset(x = trackWidth * animatedProgress - 12.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ){
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusRouteProgressPreview() {
    ShuttleSpotTheme()
    {
        BusRouteProgress(progress = 0.5f)
    }
}