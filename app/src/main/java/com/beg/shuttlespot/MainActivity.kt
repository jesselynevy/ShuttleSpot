package com.beg.shuttlespot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.beg.shuttlespot.core.ui.components.WaveBackground
import com.beg.shuttlespot.core.ui.components.WaveStyle
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import com.beg.shuttlespot.navigation.AuthRoute
import com.beg.shuttlespot.navigation.MainRoute
import com.beg.shuttlespot.navigation.OnboardingRoute
import com.beg.shuttlespot.navigation.RootRoute
import com.beg.shuttlespot.navigation.ShuttleSpotNavGraph
import org.maplibre.android.MapLibre
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
private fun ShuttleSpotApp() {

    ShuttleSpotTheme {
        // Cat latar dari tema; tanpa ini layar memakai warna window dari themes.xml (terang) walau teks mengikuti mode gelap.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val signInFlow = setOf(AuthRoute.GRAPH, OnboardingRoute.GRAPH)
            val navController = rememberNavController()
            val entry by navController.currentBackStackEntryAsState()
            var hierarchy = entry?.destination?.hierarchy

            val style = when {
                hierarchy == null -> WaveStyle.None
                hierarchy.any {it.route == AuthRoute.WELCOME} -> WaveStyle.Splash
                hierarchy.any {it.route in signInFlow } -> WaveStyle.Auth
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
private fun ShuttleSpotAppPreview() {
    ShuttleSpotApp()
}
