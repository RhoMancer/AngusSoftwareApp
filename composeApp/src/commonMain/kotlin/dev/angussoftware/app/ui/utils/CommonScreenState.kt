package dev.angussoftware.app.ui.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.angussoftware.app.currentWindowAdaptiveInfo
import dev.angussoftware.app.navigation.LocalNavigationBarHeight

/**
 * Common reusable screen state holder for Home, Projects, Blog screens.
 */
data class CommonScreenState(
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

@Composable
fun rememberCommonScreenState(
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
            index > 0 || offset > collapseThresholdPx.toInt()
        }
    }

    // Animations
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeIn"
    )
    LaunchedEffect(Unit) { isVisible = true }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        label = "topBarTitleAlpha"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        label = "topBarBgAlpha"
    )

    val isCompactScreen = currentWindowAdaptiveInfo().isCompact

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