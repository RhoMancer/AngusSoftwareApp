package dev.angussoftware.app.ui.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.angussoftware.app.navigation.LocalNavigationBarHeight

/**
 * Common reusable screen state holder for Home, Projects, Blog screens.
 */
internal data class CommonScreenState(
    val statusBarHeightDp: Dp,
    val bottomInset: Dp,
    val listState: LazyListState,
    val isCollapsed: Boolean,
    val alpha: Float,
    val titleAlpha: Float,
    val bgAlpha: Float,
    val isCompactScreen: Boolean,
    val tilePadding: Dp,
    val appBarHeightDp: Dp,
)

// Test-only override hook for window adaptive info to make isCompactScreen deterministic in tests
// Default is null, meaning production behavior using currentWindowAdaptiveInfo()
internal val LocalOverrideWindowAdaptiveInfo = compositionLocalOf<WindowAdaptiveInfo?> { null }

@Composable
internal fun rememberCommonScreenState(
    collapseThreshold: Dp = 120.dp,
    tilePadding: Dp = 16.dp,
    appBarHeightDp: Dp = 64.dp,
): CommonScreenState {
    // Insets
    val statusBarHeightPx = WindowInsets.statusBars.getTop(LocalDensity.current)
    val navigationBarHeightPx = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    val density = LocalDensity.current
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }
    val systemNavigationBarHeightDp = with(density) { navigationBarHeightPx.toDp() }
    val appNavigationBarHeightDp = LocalNavigationBarHeight.current
    val bottomInset = systemNavigationBarHeightDp + appNavigationBarHeightDp

    // Scroll state and collapse logic
    val listState = rememberLazyListState()
    val collapseThresholdPx = with(density) { collapseThreshold.toPx() }
    val isCollapsed by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            isCollapsedFor(index, offset, collapseThresholdPx.toInt())
        }
    }

    // Animations
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeIn",
    )
    LaunchedEffect(Unit) { isVisible = true }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        label = "topBarTitleAlpha",
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        label = "topBarBgAlpha",
    )

    val adaptiveInfo = LocalOverrideWindowAdaptiveInfo.current ?: currentWindowAdaptiveInfo()
    val isCompactScreen = adaptiveInfo.isCompact

    return CommonScreenState(
        statusBarHeightDp = statusBarHeightDp,
        bottomInset = bottomInset,
        listState = listState,
        isCollapsed = isCollapsed,
        alpha = alpha,
        titleAlpha = titleAlpha,
        bgAlpha = bgAlpha,
        isCompactScreen = isCompactScreen,
        tilePadding = tilePadding,
        appBarHeightDp = appBarHeightDp,
    )
}

/**
 * Pure predicate that determines whether the top app bar should be considered collapsed,
 * based on the current LazyList position and a pixel threshold.
 *
 * Contract:
 * - Collapsed when the first visible item index is greater than 0 (scrolled past first item)
 * - Or when the first visible item scroll offset is strictly greater than thresholdPx
 * - Not collapsed when at the first item and offset <= thresholdPx
 */
internal fun isCollapsedFor(
    listIndex: Int,
    listOffsetPx: Int,
    thresholdPx: Int,
): Boolean = listIndex > 0 || listOffsetPx > thresholdPx
