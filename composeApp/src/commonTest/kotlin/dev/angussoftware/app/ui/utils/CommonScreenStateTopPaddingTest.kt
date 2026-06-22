package dev.angussoftware.app.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests CommonScreenState.topContentPadding computed property.
 * Exercises both branches: compact (isCompactScreen=true) and non-compact.
 */
internal class CommonScreenStateTopPaddingTest {
    private fun makeState(isCompact: Boolean): CommonScreenState {
        return CommonScreenState(
            statusBarHeightDp = 24.dp,
            bottomInset = 48.dp,
            listState = LazyListState(),
            isCollapsed = false,
            alpha = 1f,
            titleAlpha = 0f,
            bgAlpha = 0f,
            isCompactScreen = isCompact,
            tilePadding = 16.dp,
            appBarHeightDp = 64.dp,
        )
    }

    @Test
    fun topContentPadding_compact_excludesAppBar() {
        val state = makeState(isCompact = true)
        // compact: statusBarHeightDp + tilePadding = 24 + 16 = 40
        assertEquals(40.dp, state.topContentPadding)
    }

    @Test
    fun topContentPadding_nonCompact_includesAppBar() {
        val state = makeState(isCompact = false)
        // non-compact: statusBarHeightDp + appBarHeightDp + tilePadding = 24 + 64 + 16 = 104
        assertEquals(104.dp, state.topContentPadding)
    }
}
