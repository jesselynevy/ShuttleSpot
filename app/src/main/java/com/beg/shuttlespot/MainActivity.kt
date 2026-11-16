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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClientProvider: Provider<SupabaseClient>

    private val authDeepLinks = MutableSharedFlow<Intent>(
        replay = 1,
        extraBufferCapacity = 1,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAuthDeepLink(intent)
        MapLibre.getInstance(applicationContext)
        enableEdgeToEdge()
        setContent {
            ShuttleSpotApp(authDeepLinks = authDeepLinks)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthDeepLink(intent)
    }

    private fun handleAuthDeepLink(intent: Intent?) {
        if (intent?.data?.scheme != "shuttlespot") return
        if (intent.data?.host != "auth") return

        if (!BuildConfig.USE_DEMO_DATA) {
            supabaseClientProvider.get().handleDeeplinks(intent)
        }

        authDeepLinks.tryEmit(intent)
    }
}

@Composable
// Renders the shuttle spot app UI section.
private fun ShuttleSpotApp(
    authDeepLinks: Flow<Intent> = emptyFlow(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isDarkModeOverride by themeViewModel.isDarkMode.collectAsStateWithLifecycle()
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

            LaunchedEffect(navController, authDeepLinks) {
                authDeepLinks.collect { deepLinkIntent ->
                    navController.handleDeepLink(deepLinkIntent)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
// Renders the shuttle spot app preview UI section.
private fun ShuttleSpotAppPreview() {
    ShuttleSpotApp()
}
