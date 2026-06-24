package dev.angussoftware.app.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsScreenRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_dropdownClick_opensAndShowsThemes() {
        composeTestRule.setContent {
            SettingsScreen()
        }
        composeTestRule.waitForIdle()

        // Click the light theme dropdown button
        composeTestRule.onNodeWithText("Angus Light").performClick()
        composeTestRule.waitForIdle()

        // Dropdown items should now be visible
        composeTestRule.onNodeWithText("Nord").assertExists()
    }
}
