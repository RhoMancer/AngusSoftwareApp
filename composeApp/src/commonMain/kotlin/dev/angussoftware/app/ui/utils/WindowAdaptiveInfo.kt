package dev.angussoftware.app.ui.utils

import androidx.compose.runtime.Composable

/**
 * Enum class representing window width size classes
 */
internal enum class WindowWidthSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED;
    
    companion object {
        // Width breakpoints
        const val MEDIUM_WIDTH_THRESHOLD_DP = 600
        const val EXPANDED_WIDTH_THRESHOLD_DP = 840
        
        fun fromWidth(widthDp: Int): WindowWidthSizeClass = when {
            widthDp < MEDIUM_WIDTH_THRESHOLD_DP -> COMPACT
            widthDp < EXPANDED_WIDTH_THRESHOLD_DP -> MEDIUM
            else -> EXPANDED
        }
    }
}

/**
 * Class containing window size information
 */
internal data class WindowAdaptiveInfo(
    val widthSizeClass: WindowWidthSizeClass
) {
    val isCompact: Boolean get() = widthSizeClass == WindowWidthSizeClass.COMPACT
    val isMedium: Boolean get() = widthSizeClass == WindowWidthSizeClass.MEDIUM
    val isExpanded: Boolean get() = widthSizeClass == WindowWidthSizeClass.EXPANDED
}

/**
 * Get current window adaptive information
 */
@Composable
internal expect fun currentWindowAdaptiveInfo(): WindowAdaptiveInfo

