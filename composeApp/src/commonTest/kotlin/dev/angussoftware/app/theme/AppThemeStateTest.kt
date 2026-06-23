package dev.angussoftware.app.theme

import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode
import dev.angussoftware.app.preferences.ThemePreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class AppThemeStateTest {

    private fun createState(
        mode: ThemeMode = ThemeMode.SYSTEM,
        light: ColorTheme = ColorTheme.AngusLight,
        dark: ColorTheme = ColorTheme.AngusDark,
    ) = AppThemeState(ThemePreferences(themeMode = mode, lightTheme = light, darkTheme = dark))

    @Test
    fun activeColorThemeReturnsLightThemeWhenModeIsLight() {
        val state = createState(mode = ThemeMode.LIGHT, light = ColorTheme.AngusLight)
        assertEquals(ColorTheme.AngusLight, state.activeColorTheme)
    }

    @Test
    fun activeColorThemeReturnsDarkThemeWhenModeIsDark() {
        val state = createState(mode = ThemeMode.DARK, dark = ColorTheme.AngusDark)
        assertEquals(ColorTheme.AngusDark, state.activeColorTheme)
    }

    @Test
    fun activeColorThemeReturnsDarkThemeWhenModeIsSystem() {
        val state = createState(mode = ThemeMode.SYSTEM, dark = ColorTheme.AngusDark)
        assertEquals(ColorTheme.AngusDark, state.activeColorTheme)
    }

    @Test
    fun updateThemeModeChangesPrefs() {
        val state = createState(mode = ThemeMode.SYSTEM)
        state.updateThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, state.prefs.themeMode)
    }

    @Test
    fun updateLightThemeChangesPrefs() {
        val state = createState(light = ColorTheme.AngusLight)
        state.updateLightTheme(ColorTheme.AngusDark)
        assertEquals(ColorTheme.AngusDark, state.prefs.lightTheme)
    }

    @Test
    fun updateDarkThemeChangesPrefs() {
        val state = createState(dark = ColorTheme.AngusDark)
        state.updateDarkTheme(ColorTheme.AngusLight)
        assertEquals(ColorTheme.AngusLight, state.prefs.darkTheme)
    }

    @Test
    fun activeColorThemeReflectsUpdatedLightTheme() {
        val state = createState(mode = ThemeMode.LIGHT, light = ColorTheme.AngusLight)
        state.updateLightTheme(ColorTheme.AngusDark)
        assertEquals(ColorTheme.AngusDark, state.activeColorTheme)
    }

    @Test
    fun activeColorThemeReflectsUpdatedDarkTheme() {
        val state = createState(mode = ThemeMode.DARK, dark = ColorTheme.AngusDark)
        state.updateDarkTheme(ColorTheme.AngusLight)
        assertEquals(ColorTheme.AngusLight, state.activeColorTheme)
    }

    @Test
    fun activeColorThemeReflectsUpdatedMode() {
        val state = createState(mode = ThemeMode.LIGHT, light = ColorTheme.AngusLight, dark = ColorTheme.AngusDark)
        state.updateThemeMode(ThemeMode.DARK)
        assertEquals(ColorTheme.AngusDark, state.activeColorTheme)
    }
}

    @Test
    fun createStateWithDefaultPreferences() {
        // Triggers the default parameter: loadThemePreferences()
        val state = AppThemeState()
        assertEquals(ThemeMode.SYSTEM, state.prefs.themeMode)
    }
