package dev.angussoftware.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * Android implementation of currentWindowAdaptiveInfo using LocalConfiguration
 */
@Composable
internal actual fun currentWindowAdaptiveInfo(): WindowAdaptiveInfo {
    val widthPx = LocalWindowInfo.current.containerSize.width
    val density = LocalDensity.current.density

    val widthDp = widthPx / density

    val widthSizeClass = WindowWidthSizeClass.fromWidth(widthDp.toInt())

    return WindowAdaptiveInfo(widthSizeClass)
}

