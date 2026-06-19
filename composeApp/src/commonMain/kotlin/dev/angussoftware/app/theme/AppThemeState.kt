package dev.angussoftware.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
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

    /** The color theme to pass to AngusTheme for the current mode. */
    val activeColorTheme: ColorTheme
        get() {
            return when (prefs.themeMode) {
                ThemeMode.LIGHT -> prefs.lightTheme
                ThemeMode.DARK -> prefs.darkTheme
                ThemeMode.SYSTEM -> prefs.darkTheme
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
 * CompositionLocal providing the app-wide theme state.
 */
val LocalAppThemeState = compositionLocalOf<AppThemeState> {
    error("AppThemeState not provided. Wrap your content in AppThemeProvider.")
}

/**
 * Provides app-wide theme state via CompositionLocal.
 * Call once at the top level of the app.
 */
@Composable
fun AppThemeProvider(content: @Composable () -> Unit) {
    val state = remember { AppThemeState() }
    androidx.compose.runtime.CompositionLocalProvider(LocalAppThemeState provides state) {
        content()
    }
}

/**
 * Access the app-wide theme state.
 */
@Composable
fun rememberAppThemeState(): AppThemeState {
    return androidx.compose.runtime.currentComposer.consume(LocalAppThemeState)
}

