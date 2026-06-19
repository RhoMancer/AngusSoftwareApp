package dev.angussoftware.app.preferences

import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode

/**
 * Persisted theme preference state.
 * Stored per-platform via expect/actual (SharedPreferences on Android, localStorage on WasmJs).
 */
data class ThemePreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lightTheme: ColorTheme = ColorTheme.Angus,
    val darkTheme: ColorTheme = ColorTheme.Angus,
)

/**
 * Loads theme preferences from persistent storage.
 */
expect fun loadThemePreferences(): ThemePreferences

/**
 * Saves theme preferences to persistent storage.
 */
expect fun saveThemePreferences(prefs: ThemePreferences)
