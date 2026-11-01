package com.beg.shuttlespot

// Overview: Defines main activity for the beg shuttlespot area of ShuttleSpot.

// Dependencies used by this file.
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
// Dependencies used by this file.
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
// Dependencies used by this file.
import androidx.navigation.compose.rememberNavController
import com.beg.shuttlespot.core.ui.components.WaveBackground
import com.beg.shuttlespot.core.ui.components.WaveStyle
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import com.beg.shuttlespot.feature.theme.ThemeViewModel
import com.beg.shuttlespot.navigation.AuthRoute
import com.beg.shuttlespot.navigation.MainRoute
import com.beg.shuttlespot.navigation.OnboardingRoute
// Dependencies used by this file.
import com.beg.shuttlespot.navigation.RootRoute
import com.beg.shuttlespot.navigation.ShuttleSpotNavGraph
import dagger.hilt.android.AndroidEntryPoint
import org.maplibre.android.MapLibre

@AndroidEntryPoint
// Defines the main activity type.
class MainActivity : ComponentActivity() {
    // Implements framework behavior for on create.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(applicationContext)
        enableEdgeToEdge()
        setContent {
            ShuttleSpotApp()
        }
    }
}

@Composable
// Renders the shuttle spot app UI section.
private fun ShuttleSpotApp(
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isDarkModeOverride by themeViewModel.isDarkMode.collectAsState()
    val darkTheme = isDarkModeOverride ?: isSystemInDarkTheme()

    ShuttleSpotTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val signInFlow = setOf(AuthRoute.GRAPH, OnboardingRoute.GRAPH)
            val navController = rememberNavController()
            val entry by navController.currentBackStackEntryAsState()
            var hierarchy = entry?.destination?.hierarchy

            val style = when {
                hierarchy == null -> WaveStyle.None
                hierarchy.any { it.route == AuthRoute.WELCOME } -> WaveStyle.Splash
                hierarchy.any { it.route in signInFlow } -> WaveStyle.Auth
                hierarchy.any { it.route == RootRoute.GATE } -> WaveStyle.None
                hierarchy.any { it.route == MainRoute.SHELL } -> WaveStyle.None
                else -> WaveStyle.Main
            }
            WaveBackground(style) { ShuttleSpotNavGraph(navController = navController) }
        }
    }
}

@Preview(showBackground = true)
@Composable
// Renders the shuttle spot app preview UI section.
private fun ShuttleSpotAppPreview() {
    ShuttleSpotApp()
}
