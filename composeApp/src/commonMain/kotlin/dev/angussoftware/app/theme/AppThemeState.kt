package dev.angussoftware.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode
import com.angussoftware.theming.compose.ui.theme.setThemeMode
import dev.angussoftware.app.preferences.ThemePreferences
import dev.angussoftware.app.preferences.loadThemePreferences
import dev.angussoftware.app.preferences.saveThemePreferences

class AppThemeState(initialPrefs: ThemePreferences = loadThemePreferences()) {
    var prefs by mutableStateOf(initialPrefs)
        private set

    val activeColorTheme: ColorTheme
        get() = when (prefs.themeMode) {
            ThemeMode.LIGHT -> prefs.lightTheme
            ThemeMode.DARK -> prefs.darkTheme
            ThemeMode.SYSTEM -> prefs.darkTheme
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

private val appThemeState by lazy { AppThemeState() }

@Composable
fun rememberAppThemeState(): AppThemeState = appThemeState
