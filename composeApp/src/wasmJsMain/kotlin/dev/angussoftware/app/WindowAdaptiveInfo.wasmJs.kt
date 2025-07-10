package dev.angussoftware.app

import androidx.compose.runtime.Composable
import kotlinx.browser.window

/**
 * WasmJs implementation of currentWindowAdaptiveInfo
 * Using JavaScript's window.innerWidth to determine the window size
 */
@Composable
actual fun currentWindowAdaptiveInfo(): WindowAdaptiveInfo {
    // Get window width in pixels
    val widthPx = window.innerWidth
    
    // Convert to dp (assuming a standard density of 96 dpi)
    val widthDp = (widthPx / 1.0).toInt()
    
    val widthSizeClass = WindowWidthSizeClass.fromWidth(widthDp)
    
    return WindowAdaptiveInfo(widthSizeClass)
}