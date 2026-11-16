package com.beg.shuttlespot.feature.auth

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RoleSelectionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun driver_role_can_continue_to_driver_login() {
        var driverSelected = false
        composeRule.setContent {
            RoleSelectionScreen(
                onPassenger = {},
                onDriver = { driverSelected = true },
            )
        }

        composeRule.onNodeWithText("Supir Bus").performClick()
        composeRule.onNodeWithText("Pilih & lanjut")
            .assertIsEnabled()
            .performClick()

        assertTrue(driverSelected)
    }
}
