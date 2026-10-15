package com.beg.shuttlespot.feature.foundation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.R
import com.beg.shuttlespot.core.ui.theme.SpringSky

/**
 * First screen of the app. The action deliberately leads to role selection so both
 * passenger and driver access remain available in the approved onboarding flow.
 */
@Composable
fun WelcomeScreen(onEnter: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // welcome_bg has a transparent export fringe around its outside edge.
            // Use a matching blue behind it so the app surface never appears there.
            .background(Color(0xFF20B6DD))
            .clipToBounds(),
    ) {
        Image(
            painter = painterResource(R.drawable.splash_light),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 32.dp, top = 120.dp, end = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.welcome_greeting),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = stringResource(R.string.welcome_tagline),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        Image(
            painter = painterResource(R.drawable.bus_image),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = maxWidth * 0.28f, y = (-16).dp)
                .width(maxWidth * 2.7f),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = 112.dp),
        ) {
            Button(
                onClick = onEnter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpringSky,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.welcome_enter),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.welcome_access_note),
                color = Color(0xFF7E7E7E),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Text(
            text = stringResource(R.string.welcome_language),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp),
            color = Color(0xFF1A1A1A),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
