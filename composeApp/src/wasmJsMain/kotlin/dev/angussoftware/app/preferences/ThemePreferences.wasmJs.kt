package dev.angussoftware.app.preferences

import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode
import kotlinx.browser.window

private const val KEY_MODE = "angus_theme_mode"
private const val KEY_LIGHT = "angus_light_theme"
private const val KEY_DARK = "angus_dark_theme"

actual fun loadThemePreferences(): ThemePreferences {
    val mode = window.localStorage.getItem(KEY_MODE)
    val light = window.localStorage.getItem(KEY_LIGHT)
    val dark = window.localStorage.getItem(KEY_DARK)

    return ThemePreferences(
        themeMode = runCatching {
            ThemeMode.valueOf(mode ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM),
        lightTheme = runCatching {
            ColorTheme.valueOf(light ?: ColorTheme.Angus.name)
        }.getOrDefault(ColorTheme.Angus),
        darkTheme = runCatching {
            ColorTheme.valueOf(dark ?: ColorTheme.Angus.name)
        }.getOrDefault(ColorTheme.Angus),
    )
}

actual fun saveThemePreferences(prefs: ThemePreferences) {
    window.localStorage.setItem(KEY_MODE, prefs.themeMode.name)
    window.localStorage.setItem(KEY_LIGHT, prefs.lightTheme.name)
    window.localStorage.setItem(KEY_DARK, prefs.darkTheme.name)
}
