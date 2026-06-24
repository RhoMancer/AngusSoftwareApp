package dev.angussoftware.app.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Test

class SettingsScreenInstrumentedTest : BaseScreenTest() {

    @Test
    fun settingsScreen_displaysScreen() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.onNodeWithTag(SETTINGS_SCREEN_TEST_TAG).assertExists()
    }

    @Test
    fun settingsScreen_showsAppearanceSection() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.onNodeWithText("Appearance").assertExists()
    }

    @Test
    fun settingsScreen_showsThemeModeSelector() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.onNodeWithText("System").assertExists()
        composeTestRule.onNodeWithText("Light").assertExists()
        composeTestRule.onNodeWithText("Dark").assertExists()
    }

    @Test
    fun settingsScreen_themeModeChips_areClickable() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.onNodeWithText("Light").performClick()
        composeTestRule.onNodeWithText("Dark").performClick()
        composeTestRule.onNodeWithText("System").performClick()
    }

    @Test
    fun settingsScreen_expandedLayout_displaysCorrectly() {
        setAdaptiveContent(WindowWidthSizeClass.EXPANDED) { SettingsScreen() }
        composeTestRule.onNodeWithTag(SETTINGS_SCREEN_TEST_TAG).assertExists()
        composeTestRule.onNodeWithText("Appearance").assertExists()
    }

    @Test
    fun settingsScreen_expandedLayout_showsChipsAndDropdowns() {
        setAdaptiveContent(WindowWidthSizeClass.EXPANDED) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("System").assertExists()
        composeTestRule.onNodeWithText("Light").assertExists()
        composeTestRule.onNodeWithText("Dark").assertExists()
        composeTestRule.onNodeWithText("Light Colors").assertExists()
        composeTestRule.onNodeWithText("Dark Colors").assertExists()
    }
}
