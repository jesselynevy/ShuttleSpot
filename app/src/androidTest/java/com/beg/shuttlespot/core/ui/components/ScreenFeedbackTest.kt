package com.beg.shuttlespot.core.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme
import org.junit.Rule
import org.junit.Test

class ScreenFeedbackTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun offlineStateExplainsThatCachedDataMayBeStale() {
        composeRule.setContent { ShuttleSpotTheme { OfflineState() } }

        composeRule.onNodeWithText("Anda sedang offline").assertIsDisplayed()
        composeRule.onNodeWithText("Menampilkan data tersimpan yang mungkin belum terbaru.").assertIsDisplayed()
    }
}
