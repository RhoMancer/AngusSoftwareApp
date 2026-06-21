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
    val lightRaw = prefs.getString(KEY_LIGHT, null)
    val darkRaw = prefs.getString(KEY_DARK, null)
    val lightParsed = runCatching { ColorTheme.valueOf(lightRaw ?: "") }.getOrNull()
    val darkParsed = runCatching { ColorTheme.valueOf(darkRaw ?: "") }.getOrNull()
    return ThemePreferences(
        themeMode = runCatching { ThemeMode.valueOf(prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name)!!) }.getOrDefault(ThemeMode.SYSTEM),
        lightTheme = when (lightParsed) { null, ColorTheme.Angus -> ColorTheme.AngusLight; else -> lightParsed },
        darkTheme = when (darkParsed) { null, ColorTheme.Angus -> ColorTheme.AngusDark; else -> darkParsed },
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
