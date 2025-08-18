package dev.angussoftware.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

/**
 * Android implementation of currentWindowAdaptiveInfo using LocalConfiguration
 */
@Composable
actual fun currentWindowAdaptiveInfo(): WindowAdaptiveInfo {
    val widthDp = LocalWindowInfo.current.containerSize
    
    val widthSizeClass = WindowWidthSizeClass.fromWidth(widthDp.width)
    
    return WindowAdaptiveInfo(widthSizeClass)
}

