package com.beg.shuttlespot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
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
        val navController = rememberNavController()
        ShuttleSpotNavGraph(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
private fun ShuttleSpotAppPreview() {
    ShuttleSpotApp()
}
