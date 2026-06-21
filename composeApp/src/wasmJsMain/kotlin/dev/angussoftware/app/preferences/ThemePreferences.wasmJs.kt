package dev.angussoftware.app.preferences

import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode
import kotlinx.browser.window

private const val KEY_MODE = "angus_theme_mode"
private const val KEY_LIGHT = "angus_light_theme"
private const val KEY_DARK = "angus_dark_theme"

private fun safeGetItem(key: String): String? = try {
    window.localStorage.getItem(key)
} catch (e: Throwable) { null }

private fun parseLightTheme(raw: String?): ColorTheme {
    val parsed = runCatching { ColorTheme.valueOf(raw ?: "") }.getOrNull()
    return when (parsed) {
        null, ColorTheme.Angus -> ColorTheme.AngusLight
        else -> parsed
    }
}

private fun parseDarkTheme(raw: String?): ColorTheme {
    val parsed = runCatching { ColorTheme.valueOf(raw ?: "") }.getOrNull()
    return when (parsed) {
        null, ColorTheme.Angus -> ColorTheme.AngusDark
        else -> parsed
    }
}

actual fun loadThemePreferences(): ThemePreferences {
    val mode = safeGetItem(KEY_MODE)
    val light = safeGetItem(KEY_LIGHT)
    val dark = safeGetItem(KEY_DARK)
    return ThemePreferences(
        themeMode = runCatching { ThemeMode.valueOf(mode ?: ThemeMode.SYSTEM.name) }.getOrDefault(ThemeMode.SYSTEM),
        lightTheme = parseLightTheme(light),
        darkTheme = parseDarkTheme(dark),
    )
}

actual fun saveThemePreferences(prefs: ThemePreferences) {
    try {
        window.localStorage.setItem(KEY_MODE, prefs.themeMode.name)
        window.localStorage.setItem(KEY_LIGHT, prefs.lightTheme.name)
        window.localStorage.setItem(KEY_DARK, prefs.darkTheme.name)
    } catch (e: Throwable) { }
}
