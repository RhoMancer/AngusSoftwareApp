package dev.angussoftware.app.preferences

import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode

data class ThemePreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lightTheme: ColorTheme = ColorTheme.AngusLight,
    val darkTheme: ColorTheme = ColorTheme.AngusDark,
)

expect fun loadThemePreferences(): ThemePreferences
expect fun saveThemePreferences(prefs: ThemePreferences)
