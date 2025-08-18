package dev.angussoftware.app

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * WasmJs implementation of currentWindowAdaptiveInfo
 * Now observes browser resize events and converts pixels to dp using LocalDensity
 */
@Composable
actual fun currentWindowAdaptiveInfo(): WindowAdaptiveInfo {
    val density = LocalDensity.current

    var widthPx by remember { mutableStateOf(window.innerWidth) }

    // Listen for window resize and update state to trigger recomposition
    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            widthPx = window.innerWidth
        }
        window.addEventListener("resize", listener)
        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    val widthDp = with(density) { widthPx.toFloat().toDp().value.toInt() }
    val widthSizeClass = WindowWidthSizeClass.fromWidth(widthDp)
    return WindowAdaptiveInfo(widthSizeClass)
}
