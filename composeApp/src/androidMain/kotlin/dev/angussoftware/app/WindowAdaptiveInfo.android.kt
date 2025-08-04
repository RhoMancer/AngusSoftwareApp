package dev.angussoftware.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp

/**
 * Android implementation of currentWindowAdaptiveInfo using LocalConfiguration
 */
@Composable
actual fun currentWindowAdaptiveInfo(): WindowAdaptiveInfo {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    
    val widthSizeClass = WindowWidthSizeClass.fromWidth(widthDp)
    
    return WindowAdaptiveInfo(widthSizeClass)
}

