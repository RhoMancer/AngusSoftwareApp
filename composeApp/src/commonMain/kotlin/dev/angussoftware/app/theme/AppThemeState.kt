package dev.angussoftware.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode
import com.angussoftware.theming.compose.ui.theme.setThemeMode
import dev.angussoftware.app.preferences.ThemePreferences
import dev.angussoftware.app.preferences.loadThemePreferences
import dev.angussoftware.app.preferences.saveThemePreferences

/**
 * Observable theme state shared across the app.
 * Uses Compose's mutableStateOf for reactivity.
 */
class AppThemeState(initialPrefs: ThemePreferences = loadThemePreferences()) {
    var prefs by mutableStateOf(initialPrefs)
        private set

    val activeColorTheme: ColorTheme
        get() {
            return when (prefs.themeMode) {
                ThemeMode.LIGHT -> prefs.lightTheme
                ThemeMode.DARK -> prefs.darkTheme
                ThemeMode.SYSTEM -> ColorTheme.Angus
            }
        }

    fun updateThemeMode(mode: ThemeMode) {
        prefs = prefs.copy(themeMode = mode)
        setThemeMode(mode)
        saveThemePreferences(prefs)
    }

    fun updateLightTheme(theme: ColorTheme) {
        prefs = prefs.copy(lightTheme = theme)
        saveThemePreferences(prefs)
    }

    fun updateDarkTheme(theme: ColorTheme) {
        prefs = prefs.copy(darkTheme = theme)
        saveThemePreferences(prefs)
    }
}

/**
 * Singleton theme state — one instance for the entire app.
 * Avoids CompositionLocalProvider which can interfere with
 * touch event handling on Compose Web (WasmJs canvas).
 */
private val appThemeState = AppThemeState()

/**
 * Returns the singleton theme state.
 */
@Composable
fun rememberAppThemeState(): AppThemeState {
    return appThemeState
}
