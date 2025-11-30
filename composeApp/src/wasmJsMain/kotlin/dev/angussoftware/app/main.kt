package dev.angussoftware.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.ComposeViewport
import com.angussoftware.theming.compose.ui.theme.AngusTheme
import dev.angussoftware.app.installprompt.DefaultInstallPromptPlatform
import dev.angussoftware.app.installprompt.InstallPromptController
import dev.angussoftware.app.installprompt.InstallPromptHost
import dev.angussoftware.app.screens.AngusSoftwareAppScreen
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
internal fun main() {
    ComposeViewport(document.body!!) {
        AngusTheme {
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
    }
}
