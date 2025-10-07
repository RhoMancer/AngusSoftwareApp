package dev.angussoftware.app.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.angussoftware.app.navigation.LocalNavigationBarHeight
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

private const val LIST_TAG = "list"
private const val COLLAPSED_TAG = "collapsed"
private const val TITLE_ALPHA_TAG = "titleAlpha"
private const val BG_ALPHA_TAG = "bgAlpha"
private const val FADE_ALPHA_TAG = "fadeAlpha"
private const val SCROLL_BY_TAG = "scrollBy"
private const val SCROLL_TO_TOP_TAG = "scrollToTop"
private const val SCROLL_TO_THRESHOLD_TAG = "scrollToThreshold"
private const val TILE_PADDING_TAG = "tilePadding"
private const val APP_BAR_HEIGHT_TAG = "appBarHeight"
private const val BOTTOM_INSET_TAG = "bottomInset"
private const val IS_COMPACT_TAG = "isCompact"
private const val TOGGLE_NAV_HEIGHT_TAG = "toggleNavHeight"
private const val BOTTOM_INSET_DELTA_TAG = "bottomInsetDelta"

/**
 * UI tests for rememberCommonScreenState collapse behavior and animations.
 */
class CommonScreenStateUiTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun collapse_toggles_false_to_true_when_scrolled_past_first_item() = runComposeUiTest {
        setContent { TestScreen() }
        // Ensure composition has run
        waitForIdle()

        // Initially at top: not collapsed
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // Scroll so that first visible item index > 0
        onNodeWithTag(LIST_TAG).performScrollToIndex(1)
        waitForIdle()

        // Should be collapsed now
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun titleAlpha_reaches_one_after_collapse_when_advancing_clock() = runComposeUiTest {
        // Manually control the test clock
        mainClock.autoAdvance = false

        setContent { TestScreen() }
        // Ensure composition is ready before interacting with nodes when autoAdvance is false
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Trigger collapse
        onNodeWithTag(LIST_TAG).performScrollToIndex(1)
        waitForIdle()

        // Advance the clock enough for animations to reach end state
        // titleAlpha uses default animationSpec (spring), so give ample time
        mainClock.advanceTimeBy(5000L)
        waitForIdle()

        // Expect titleAlpha to have reached 1.00 (formatted with 2 decimals)
        onNodeWithTag(TITLE_ALPHA_TAG).assertTextEquals("1.00")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reverse_transition_and_stable_end_states() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { TestScreen() }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Initially expanded
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")
        onNodeWithTag(TITLE_ALPHA_TAG).assertTextEquals("0.00")
        onNodeWithTag(BG_ALPHA_TAG).assertTextEquals("0.00")

        // Collapse by scrolling to next item
        onNodeWithTag(LIST_TAG).performScrollToIndex(1)
        waitForIdle()
        mainClock.advanceTimeBy(3000)
        waitForIdle()

        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")
        onNodeWithTag(TITLE_ALPHA_TAG).assertTextEquals("1.00")
        onNodeWithTag(BG_ALPHA_TAG).assertTextEquals("1.00")

        // Expand back to top (index 0, offset 0)
        onNodeWithTag(SCROLL_TO_TOP_TAG).performClick()
        waitForIdle()
        mainClock.advanceTimeBy(3000)
        waitForIdle()

        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")
        onNodeWithTag(TITLE_ALPHA_TAG).assertTextEquals("0.00")
        onNodeWithTag(BG_ALPHA_TAG).assertTextEquals("0.00")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cover_bgAlpha_and_top_level_fade_alpha() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { TestScreen() }
        // First frame to ensure composition, alpha animation target is set to 1 but progress is 0
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Fade alpha should start at 0.00
        onNodeWithTag(FADE_ALPHA_TAG).assertTextEquals("0.00")
        // Not collapsed yet, so bgAlpha remains 0.00
        onNodeWithTag(BG_ALPHA_TAG).assertTextEquals("0.00")

        // Advance time to complete the 1000ms fade-in tween
        mainClock.advanceTimeBy(1000)
        waitForIdle()
        onNodeWithTag(FADE_ALPHA_TAG).assertTextEquals("1.00")

        // Now collapse and ensure bgAlpha reaches 1.00
        onNodeWithTag(LIST_TAG).performScrollToIndex(1)
        waitForIdle()
        mainClock.advanceTimeBy(3000)
        waitForIdle()
        onNodeWithTag(BG_ALPHA_TAG).assertTextEquals("1.00")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun collapses_by_offset_within_first_item_when_scrolled_beyond_threshold() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { TestScreen() }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Ensure starting expanded
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // Scroll by threshold+1 pixels while staying on the first item (index 0)
        onNodeWithTag(SCROLL_BY_TAG).performClick()
        waitForIdle()
        mainClock.advanceTimeBy(3000)
        waitForIdle()

        // Collapsed due to offset-only path
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")
        onNodeWithTag(TITLE_ALPHA_TAG).assertTextEquals("1.00")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun threshold_parameterization_and_boundary() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { TestScreen(collapseThreshold = 1.dp) }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        onNodeWithTag(SCROLL_TO_THRESHOLD_TAG).performClick()
        waitForIdle()
        mainClock.advanceTimeBy(200)
        waitForIdle()
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        onNodeWithTag(SCROLL_BY_TAG).performClick()
        waitForIdle()
        mainClock.advanceTimeBy(3000)
        waitForIdle()
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun insets_and_LocalNavigationBarHeight_affect_bottomInset_delta() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { InsetsTestScreen() }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Initial delta should be 0.00
        onNodeWithTag(BOTTOM_INSET_DELTA_TAG).assertTextEquals("0.00")

        // Toggle nav height to 25.dp (from 5.dp) => delta should become 20.00
        onNodeWithTag(TOGGLE_NAV_HEIGHT_TAG).performClick()
        waitForIdle()
        mainClock.advanceTimeBy(200)
        waitForIdle()
        onNodeWithTag(BOTTOM_INSET_DELTA_TAG).assertTextEquals("20.00")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pass_through_parameters_are_reflected() = runComposeUiTest {
        setContent { TestScreen(tilePadding = 7.dp, appBarHeightDp = 123.dp) }
        waitForIdle()
        onNodeWithTag(TILE_PADDING_TAG).assertTextEquals("7")
        onNodeWithTag(APP_BAR_HEIGHT_TAG).assertTextEquals("123")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun isCompactScreen_branching_via_override() = runComposeUiTest {
        // Force COMPACT
        setContent {
            CompositionLocalProvider(LocalOverrideWindowAdaptiveInfo provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT)) {
                TestScreen()
            }
        }
        waitForIdle()
        onNodeWithTag(IS_COMPACT_TAG).assertTextEquals("true")

        // Force EXPANDED
        setContent {
            CompositionLocalProvider(LocalOverrideWindowAdaptiveInfo provides WindowAdaptiveInfo(WindowWidthSizeClass.EXPANDED)) {
                TestScreen()
            }
        }
        waitForIdle()
        onNodeWithTag(IS_COMPACT_TAG).assertTextEquals("false")
    }
}

@androidx.compose.runtime.Composable
private fun TestScreen(
    collapseThreshold: Dp = 120.dp,
    tilePadding: Dp = 16.dp,
    appBarHeightDp: Dp = 64.dp,
) {
    val state = rememberCommonScreenState(
        collapseThreshold = collapseThreshold,
        tilePadding = tilePadding,
        appBarHeightDp = appBarHeightDp,
    )
    val scope = rememberCoroutineScope()
    val thresholdPx = with(LocalDensity.current) { collapseThreshold.toPx() }

    // Expose state via tagged text for assertions
    Text(modifier = Modifier.testTag(COLLAPSED_TAG), text = state.isCollapsed.toString())
    Text(
        modifier = Modifier.testTag(TITLE_ALPHA_TAG),
        text = String.format("%.2f", state.titleAlpha)
    )
    Text(
        modifier = Modifier.testTag(BG_ALPHA_TAG),
        text = String.format("%.2f", state.bgAlpha)
    )
    Text(
        modifier = Modifier.testTag(FADE_ALPHA_TAG),
        text = String.format("%.2f", state.alpha)
    )
    Text(
        modifier = Modifier.testTag(IS_COMPACT_TAG),
        text = state.isCompactScreen.toString()
    )
    Text(
        modifier = Modifier.testTag(TILE_PADDING_TAG),
        text = state.tilePadding.value.toInt().toString()
    )
    Text(
        modifier = Modifier.testTag(APP_BAR_HEIGHT_TAG),
        text = state.appBarHeightDp.value.toInt().toString()
    )
    Text(
        modifier = Modifier.testTag(BOTTOM_INSET_TAG),
        text = String.format("%.2f", state.bottomInset.value)
    )

    // Controls to drive precise scroll behavior in tests
    Button(modifier = Modifier.testTag(SCROLL_TO_TOP_TAG), onClick = {
        scope.launch { state.listState.scrollToItem(0, 0) }
    }) { Text("top") }
    Button(modifier = Modifier.testTag(SCROLL_BY_TAG), onClick = {
        scope.launch { state.listState.scrollToItem(0, (thresholdPx + 1f).toInt()) }
    }) { Text("by") }
    Button(modifier = Modifier.testTag(SCROLL_TO_THRESHOLD_TAG), onClick = {
        scope.launch { state.listState.scrollToItem(0, thresholdPx.toInt()) }
    }) { Text("thr") }

    // Content with a scrollable list using the state's listState
    LazyColumn(state = state.listState, modifier = Modifier.testTag(LIST_TAG)) {
        // Enough items to allow scrolling
        items((0 until 50).toList()) { _ ->
            Box(Modifier.fillMaxWidth().height(200.dp))
        }
    }
}

@androidx.compose.runtime.Composable
private fun InsetsTestScreen() {
    // Start with 5.dp and allow toggling to 25.dp to validate 20.dp delta effect
    val scope = rememberCoroutineScope()
    var navHeight by remember { mutableStateOf(5.dp) }
    CompositionLocalProvider(LocalNavigationBarHeight provides navHeight) {
        val state = rememberCommonScreenState()
        // Capture baseline bottom inset on first composition; remember keeps it stable
        val baseline = remember { state.bottomInset.value }

        Button(modifier = Modifier.testTag(TOGGLE_NAV_HEIGHT_TAG), onClick = {
            scope.launch {
                navHeight = 25.dp
            }
        }) { Text("toggle") }

        // Show current bottom inset and the delta from baseline
        Text(modifier = Modifier.testTag(BOTTOM_INSET_TAG), text = String.format("%.2f", state.bottomInset.value))
        Text(modifier = Modifier.testTag(BOTTOM_INSET_DELTA_TAG), text = String.format("%.2f", state.bottomInset.value - baseline))
    }
}
