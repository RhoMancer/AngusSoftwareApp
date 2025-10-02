package dev.angussoftware.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.angussoftware.theming.compose.ui.theme.AngusTheme
import dev.angussoftware.app.screens.AngusSoftwareAppScreen
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
internal fun main() {
    ComposeViewport(document.body!!) {
        AngusTheme {
            AngusSoftwareAppScreen()
        }
    }
}
