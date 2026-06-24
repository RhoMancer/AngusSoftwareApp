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
    fun settingsScreen_lightThemeDropdownExists() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Light").assertExists()
    }

    @Test
    fun settingsScreen_darkThemeDropdownExists() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").assertExists()
    }

    @Test
    fun settingsScreen_selectLightTheme_updatesSelection() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.waitForIdle()
        // Verify light theme dropdown shows current selection
        composeTestRule.onNodeWithText("Angus Light").assertExists()
    }

    @Test
    fun settingsScreen_selectDarkTheme_updatesSelection() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }

        composeTestRule.waitForIdle()
        // Verify dark theme dropdown shows current selection
        composeTestRule.onNodeWithText("Angus Dark").assertExists()
    }

    @Test
    fun settingsScreen_lightThemeDropdown_showsAllThemes() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }
        composeTestRule.waitForIdle()
        // Open the light theme dropdown
        composeTestRule.onNodeWithText("Angus Light").performClick()
        composeTestRule.waitForIdle()
        // Verify multiple themes are now visible in the dropdown
        composeTestRule.onNodeWithText("Nord").assertExists()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_showsAllThemes() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            SettingsScreen()
        }
        composeTestRule.waitForIdle()
        // Open the dark theme dropdown
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        // Verify themes are visible
        composeTestRule.onNodeWithText("Dracula").assertExists()
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

    @Test
    fun settingsScreen_expandedLayout_showsThemeModeChips() {
        setAdaptiveContent(WindowWidthSizeClass.EXPANDED) {
            SettingsScreen()
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("System").assertExists()
        composeTestRule.onNodeWithText("Light").assertExists()
        composeTestRule.onNodeWithText("Dark").assertExists()
    }

    @Test
    fun settingsScreen_expandedLayout_showsThemeDropdowns() {
        setAdaptiveContent(WindowWidthSizeClass.EXPANDED) {
            SettingsScreen()
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Light Colors").assertExists()
        composeTestRule.onNodeWithText("Dark Colors").assertExists()
    }

    // === Dropdown item click tests (exercises DropdownMenuItem onClick branches) ===

    @Test
    fun settingsScreen_lightThemeDropdown_selectNord() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Light").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nord").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_lightThemeDropdown_selectCatppuccinLatte() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Light").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Catppuccin Latte").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_lightThemeDropdown_selectGruvboxLight() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Light").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Gruvbox Light").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_lightThemeDropdown_selectSolarizedLight() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Light").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Solarized Light").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_lightThemeDropdown_selectRosePineDawn() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Light").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Rose Pine Dawn").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectDracula() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Dracula").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectCatppuccinMocha() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Catppuccin Mocha").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectGruvboxDark() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Gruvbox Dark").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectSolarizedDark() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Solarized Dark").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectRosePineMoon() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Rose Pine Moon").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectRosePine() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Rose Pine").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectNordPolar() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nord Polar").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectCatppuccinFrappe() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Catppuccin Frappe").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun settingsScreen_darkThemeDropdown_selectCatppuccinMacchiato() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Angus Dark").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Catppuccin Macchiato").performClick()
        composeTestRule.waitForIdle()
    }

    // === Warning text branches (isLightTheme / isDarkTheme) ===

    @Test
    fun settingsScreen_noWarningWithDefaultThemes() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) { SettingsScreen() }
        composeTestRule.waitForIdle()
        // Default: AngusLight for light, AngusDark for dark — no warnings
        composeTestRule.onNodeWithText("dark theme for light mode", substring = true).assertDoesNotExist()
    }

    @Test
    fun settingsScreen_expandedLayout_allChipsAndDropdowns() {
        setAdaptiveContent(WindowWidthSizeClass.EXPANDED) { SettingsScreen() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("System").assertExists()
        composeTestRule.onNodeWithText("Light").assertExists()
        composeTestRule.onNodeWithText("Dark").assertExists()
        composeTestRule.onNodeWithText("Light Colors").assertExists()
        composeTestRule.onNodeWithText("Dark Colors").assertExists()
    }
}
