package dev.angussoftware.app.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Test

class SettingsScreenInstrumentedTest : BaseScreenTest() {

    @Test
    fun settingsScreen_displaysScreen() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.onNodeWithTag(SETTINGS_SCREEN_TEST_TAG).assertExists()
    }

    @Test
    fun settingsScreen_showsAppearanceSection() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.onNodeWithText("Appearance").assertExists()
    }

    @Test
    fun settingsScreen_showsThemeModeSelector() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.onNodeWithText("System").assertExists()
        composeTestRule.onNodeWithText("Light").assertExists()
        composeTestRule.onNodeWithText("Dark").assertExists()
    }

    @Test
    fun settingsScreen_showsLightThemeDropdown() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.onNodeWithText("Light Colors").assertExists()
    }

    @Test
    fun settingsScreen_showsDarkThemeDropdown() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.onNodeWithText("Dark Colors").assertExists()
    }

    @Test
    fun settingsScreen_clickLightThemeDropdown_showsOptions() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        // Click the first OutlinedButton (light theme dropdown)
        composeTestRule.onNodeWithText("Angus Light").performClick()

        // Dropdown should show other themes
        composeTestRule.onNodeWithText("Nord").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_clickDarkThemeDropdown_showsOptions() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        // Click the dark theme dropdown (shows Angus Dark by default)
        composeTestRule.onNodeWithText("Angus Dark").performClick()

        composeTestRule.onNodeWithText("Dracula").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_selectLightTheme_updatesSelection() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        // Open light theme dropdown
        composeTestRule.onNodeWithText("Angus Light").performClick()

        // Select a different theme
        composeTestRule.onNodeWithText("Nord").performClick()
    }

    @Test
    fun settingsScreen_selectDarkTheme_updatesSelection() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        // Open dark theme dropdown
        composeTestRule.onNodeWithText("Angus Dark").performClick()

        // Select a different theme
        composeTestRule.onNodeWithText("Gruvbox Dark").performClick()
    }

    @Test
    fun settingsScreen_themeModeChips_areClickable() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        // Click each theme mode chip
        composeTestRule.onNodeWithText("Light").performClick()
        composeTestRule.onNodeWithText("Dark").performClick()
        composeTestRule.onNodeWithText("System").performClick()
    }

    @Test
    fun settingsScreen_expandedLayout_displaysCorrectly() {
        setAdaptiveContent(WindowWidthSizeClass.EXPANDED) {
            SettingsScreen()
        }

        composeTestRule.onNodeWithTag(SETTINGS_SCREEN_TEST_TAG).assertExists()
        composeTestRule.onNodeWithText("Appearance").assertExists()
    }
}
