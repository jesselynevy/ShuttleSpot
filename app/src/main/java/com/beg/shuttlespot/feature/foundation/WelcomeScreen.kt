package com.beg.shuttlespot.feature.foundation

// Overview: Defines welcome screen for the feature foundation area of ShuttleSpot.

// Dependencies used by this file.
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.R

/**
 * First screen of the app. The action deliberately leads to role selection so both
 * passenger and driver access remain available in the approved onboarding flow.
 */
@Composable
// Renders the welcome screen UI section.
fun WelcomeScreen(onEnter: () -> Unit) {
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // welcome_bg has a transparent export fringe around its outside edge.
            // Use a matching blue behind it so the app surface never appears there.
            .background(MaterialTheme.colorScheme.primary)
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
                .padding(
                    start = 32.dp,
                    top = if (isLandscape) 24.dp else 120.dp,
                    end = 24.dp,
                ),
        ) {
            Text(
                text = stringResource(R.string.welcome_greeting),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = stringResource(R.string.welcome_tagline),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Image(
            painter = painterResource(R.drawable.bus_image),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(
                    x = maxWidth * if (isLandscape) 0.18f else 0.28f,
                    y = if (isLandscape) 28.dp else (-16).dp,
                )
                .width(maxWidth * if (isLandscape) 1.9f else 2.7f),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = if (isLandscape) 20.dp else 112.dp),
        ) {
            Button(
                onClick = onEnter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (!isLandscape) {
            Text(
                text = stringResource(R.string.welcome_language),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
