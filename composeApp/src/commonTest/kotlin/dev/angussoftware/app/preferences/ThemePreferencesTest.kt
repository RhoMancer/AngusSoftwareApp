package dev.angussoftware.app.preferences

import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemePreferencesTest {

    @Test
    fun defaultPreferencesUseSystemMode() {
        val prefs = ThemePreferences()
        assertEquals(ThemeMode.SYSTEM, prefs.themeMode)
    }

    @Test
    fun defaultPreferencesUseAngusLight() {
        val prefs = ThemePreferences()
        assertEquals(ColorTheme.AngusLight, prefs.lightTheme)
    }

    @Test
    fun defaultPreferencesUseAngusDark() {
        val prefs = ThemePreferences()
        assertEquals(ColorTheme.AngusDark, prefs.darkTheme)
    }

    @Test
    fun copyWithNewMode() {
        val prefs = ThemePreferences()
        val updated = prefs.copy(themeMode = ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, updated.themeMode)
        assertEquals(ColorTheme.AngusLight, updated.lightTheme)
        assertEquals(ColorTheme.AngusDark, updated.darkTheme)
    }

    @Test
    fun copyWithNewLightTheme() {
        val prefs = ThemePreferences()
        val updated = prefs.copy(lightTheme = ColorTheme.AngusDark)
        assertEquals(ColorTheme.AngusDark, updated.lightTheme)
    }

    @Test
    fun copyWithNewDarkTheme() {
        val prefs = ThemePreferences()
        val updated = prefs.copy(darkTheme = ColorTheme.AngusLight)
        assertEquals(ColorTheme.AngusLight, updated.darkTheme)
    }
}
