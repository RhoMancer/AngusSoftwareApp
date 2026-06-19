package dev.angussoftware.app.preferences

import android.content.Context
import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode

private lateinit var appContext: Context

fun initThemePreferences(context: Context) {
    appContext = context.applicationContext
}

private const val PREFS_NAME = "angus_theme_prefs"
private const val KEY_MODE = "theme_mode"
private const val KEY_LIGHT = "light_theme"
private const val KEY_DARK = "dark_theme"

actual fun loadThemePreferences(): ThemePreferences {
    if (!::appContext.isInitialized) return ThemePreferences()

    val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return ThemePreferences(
        themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name)!!)
        }.getOrDefault(ThemeMode.SYSTEM),
        lightTheme = runCatching {
            ColorTheme.valueOf(prefs.getString(KEY_LIGHT, ColorTheme.Angus.name)!!)
        }.getOrDefault(ColorTheme.Angus),
        darkTheme = runCatching {
            ColorTheme.valueOf(prefs.getString(KEY_DARK, ColorTheme.Angus.name)!!)
        }.getOrDefault(ColorTheme.Angus),
    )
}

actual fun saveThemePreferences(prefs: ThemePreferences) {
    if (!::appContext.isInitialized) return

    appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_MODE, prefs.themeMode.name)
        .putString(KEY_LIGHT, prefs.lightTheme.name)
        .putString(KEY_DARK, prefs.darkTheme.name)
        .apply()
}
