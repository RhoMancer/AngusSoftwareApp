package dev.angussoftware.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.ComposeViewport
import com.angussoftware.theming.compose.ui.theme.AngusTheme
import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode
import com.angussoftware.theming.compose.ui.theme.initializeThemeMode
import dev.angussoftware.app.installprompt.DefaultInstallPromptPlatform
import dev.angussoftware.app.installprompt.InstallPromptController
import dev.angussoftware.app.installprompt.InstallPromptHost
import dev.angussoftware.app.i18n.setupLocale
import dev.angussoftware.app.screens.AngusSoftwareAppScreen
import dev.angussoftware.app.theme.rememberAppThemeState
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
internal fun main() {
    // Read browser locale and set it so Compose resources resolve correctly
    val browserLang = window.navigator.language ?: "en"
    val shortLang = browserLang.split("-_")[0]
    setupLocale(shortLang)

    ComposeViewport(document.body!!) {
        val themeState = rememberAppThemeState()
        initializeThemeMode(themeState.prefs.themeMode)

        val activeTheme = if (themeState.prefs.themeMode == ThemeMode.SYSTEM) {
            val isDark = isSystemInDarkTheme()
            if (isDark) themeState.prefs.darkTheme else themeState.prefs.lightTheme
        } else {
            themeState.activeColorTheme
        }
        AngusTheme(colorTheme = activeTheme) {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    val uriHandler = LocalUriHandler.current
    val controller = remember {
        val platform = DefaultInstallPromptPlatform { url ->
            uriHandler.openUri(url)
        }
        InstallPromptController(platform).also { it.initialize() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AngusSoftwareAppScreen()
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            InstallPromptHost(controller)
        }
    }
}
