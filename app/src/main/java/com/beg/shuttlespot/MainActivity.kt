package com.beg.shuttlespot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
<<<<<<< Updated upstream
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
=======
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
>>>>>>> Stashed changes
import androidx.navigation.compose.rememberNavController
import com.beg.shuttlespot.navigation.ShuttleSpotNavGraph
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(applicationContext)
        enableEdgeToEdge()
        setContent {
            ShuttleSpotTheme {
                MainScreenSide()
            }
        }
    }
}

/** Empty drawer shell used to verify the open/close interaction before adding navigation items. */
@Composable
private fun DrawerContent() {
    ModalDrawerSheet {
        // Intentionally left empty for now.
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenSide() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContent() }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("ShuttleSpot") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open navigation menu"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                ShuttleSpotNavGraph(navController = navController)
            }
        }
    }
}
<<<<<<< Updated upstream

@Preview(showBackground = true)
@Composable
fun ShuttleSpotAppPreview(){
    ShuttleSpotTheme() {
        val navController = rememberNavController()
        ShuttleSpotNavGraph(navController = navController)
    }
}
=======
>>>>>>> Stashed changes
