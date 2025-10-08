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
private const val STATUS_BAR_HEIGHT_TAG = "statusBarHeight"
private const val RECOMPOSE_TAG = "forceRecompose"
private const val ALPHA_DIFF_TAG = "alphaDiff"

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
    fun isCompactScreen_true_for_compact_size_class() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalOverrideWindowAdaptiveInfo provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT)) {
                TestScreen()
            }
        }
        waitForIdle()
        onNodeWithTag(IS_COMPACT_TAG).assertTextEquals("true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun isCompactScreen_false_for_expanded_size_class() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalOverrideWindowAdaptiveInfo provides WindowAdaptiveInfo(WindowWidthSizeClass.EXPANDED)) {
                TestScreen()
            }
        }
        waitForIdle()
        onNodeWithTag(IS_COMPACT_TAG).assertTextEquals("false")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun medium_size_class_yields_isCompactScreen_false() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalOverrideWindowAdaptiveInfo provides WindowAdaptiveInfo(WindowWidthSizeClass.MEDIUM)) {
                TestScreen()
            }
        }
        waitForIdle()
        onNodeWithTag(IS_COMPACT_TAG).assertTextEquals("false")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun statusBarHeightDp_is_non_negative_and_stable_across_recomposition() = runComposeUiTest {
        setContent { TestScreen() }
        waitForIdle()

        // Read the initial statusBarHeight text
        val initialHeightText = onNodeWithTag(STATUS_BAR_HEIGHT_TAG).fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text
        val initialHeight = initialHeightText.toFloatOrNull() ?: 0f
        assertTrue(initialHeight >= 0f, "statusBarHeightDp should be non-negative, was: $initialHeight")

        // Trigger a recomposition without changing insets or any state that affects statusBarHeightDp
        onNodeWithTag("triggerRecompose").performClick()
        waitForIdle()

        // Read the height again and assert it's unchanged
        val afterRecomposeHeightText = onNodeWithTag(STATUS_BAR_HEIGHT_TAG).fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text
        val afterRecomposeHeight = afterRecomposeHeightText.toFloatOrNull() ?: 0f
        assertTrue(abs(afterRecomposeHeight - initialHeight) < 0.01f, 
            "statusBarHeightDp should remain stable across recomposition, was $initialHeight then $afterRecomposeHeight")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun fade_in_alpha_stable_across_recompositions_after_reaching_one() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { TestScreen() }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Advance time to complete the fade-in animation (1000ms)
        mainClock.advanceTimeBy(1000)
        waitForIdle()
        onNodeWithTag(FADE_ALPHA_TAG).assertTextEquals("1.00")

        // Trigger recomposition
        onNodeWithTag("triggerRecompose").performClick()
        waitForIdle()
        mainClock.advanceTimeBy(100)
        waitForIdle()

        // Fade alpha should remain 1.00 (not restart)
        onNodeWithTag(FADE_ALPHA_TAG).assertTextEquals("1.00")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun collapse_to_uncollapse_transition_when_scrolling_back_to_top() = runComposeUiTest {
        setContent { TestScreen() }
        waitForIdle()

        // Initially at top: not collapsed
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // Scroll down to trigger collapse
        onNodeWithTag(LIST_TAG).performScrollToIndex(5)
        waitForIdle()
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")

        // Scroll back to top
        onNodeWithTag(LIST_TAG).performScrollToIndex(0)
        waitForIdle()

        // Should be uncollapsed now
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun titleAlpha_and_bgAlpha_return_to_zero_when_uncollapsing() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { TestScreen() }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Scroll down to collapse
        onNodeWithTag(LIST_TAG).performScrollToIndex(5)
        mainClock.advanceTimeBy(500)
        waitForIdle()
        
        // Advance time to complete animations
        mainClock.advanceTimeBy(5000L)
        waitForIdle()

        // Verify collapsed state with alpha values at 1.00
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")
        onNodeWithTag(TITLE_ALPHA_TAG).assertTextEquals("1.00")
        onNodeWithTag(BG_ALPHA_TAG).assertTextEquals("1.00")

        // Scroll back to top
        onNodeWithTag(LIST_TAG).performScrollToIndex(0)
        mainClock.advanceTimeBy(500)
        waitForIdle()

        // Advance time to complete uncollapse animations
        mainClock.advanceTimeBy(5000L)
        waitForIdle()

        // Verify uncollapsed state with alpha values back to 0.00
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")
        onNodeWithTag(TITLE_ALPHA_TAG).assertTextEquals("0.00")
        onNodeWithTag(BG_ALPHA_TAG).assertTextEquals("0.00")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiple_rapid_scroll_operations_maintain_state_consistency() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { TestScreen() }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Initial state: not collapsed
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // First scroll down: should collapse
        onNodeWithTag(LIST_TAG).performScrollToIndex(3)
        mainClock.advanceTimeBy(500)
        waitForIdle()
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")

        // Scroll back to top: should uncollapse
        onNodeWithTag(LIST_TAG).performScrollToIndex(0)
        mainClock.advanceTimeBy(500)
        waitForIdle()
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // Scroll down again: should collapse again
        onNodeWithTag(LIST_TAG).performScrollToIndex(2)
        mainClock.advanceTimeBy(500)
        waitForIdle()
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")

        // Final scroll to top: should uncollapse
        onNodeWithTag(LIST_TAG).performScrollToIndex(0)
        mainClock.advanceTimeBy(500)
        waitForIdle()
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initial_bottomInset_value_is_non_negative() = runComposeUiTest {
        setContent { TestScreen() }
        waitForIdle()

        // Read the initial bottomInset text
        val bottomInsetText = onNodeWithTag(BOTTOM_INSET_TAG).fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text
        val bottomInset = bottomInsetText.toFloatOrNull() ?: -1f
        
        assertTrue(bottomInset >= 0f, "bottomInset should be non-negative, was: $bottomInset")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scrolling_exactly_to_threshold_does_not_collapse() = runComposeUiTest {
        setContent { TestScreen() }
        waitForIdle()

        // Initially not collapsed
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // Click button to scroll exactly to threshold (offset = threshold, not > threshold)
        onNodeWithTag(SCROLL_TO_THRESHOLD_TAG).performClick()
        waitForIdle()

        // Should remain not collapsed (contract: only collapse when offset > threshold)
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scrolling_past_first_item_by_one_index_triggers_collapse() = runComposeUiTest {
        setContent { TestScreen() }
        waitForIdle()

        // Initially not collapsed
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // Scroll to item index 1 (past first item)
        onNodeWithTag(LIST_TAG).performScrollToIndex(1)
        waitForIdle()

        // Should now be collapsed (index > 0 triggers collapse)
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun titleAlpha_and_bgAlpha_animate_in_sync() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { TestScreen() }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // Trigger collapse
        onNodeWithTag(LIST_TAG).performScrollToIndex(1)
        waitForIdle()

        // Check alpha difference at various points during animation
        // They should animate together, so difference should remain close to 0
        for (i in 0..10) {
            mainClock.advanceTimeBy(100)
            waitForIdle()
            val alphaDiffText = onNodeWithTag(ALPHA_DIFF_TAG).fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text
            val alphaDiff = alphaDiffText.toFloatOrNull() ?: Float.MAX_VALUE
            assertTrue(abs(alphaDiff) < 0.01f, "titleAlpha and bgAlpha should animate in sync, difference was $alphaDiff at step $i")
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun custom_collapse_threshold_50dp_works_correctly() = runComposeUiTest {
        setContent { TestScreen(collapseThreshold = 50.dp) }
        waitForIdle()

        // Initially not collapsed
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // Scroll to trigger collapse with custom threshold
        onNodeWithTag(LIST_TAG).performScrollToIndex(1)
        waitForIdle()

        // Should be collapsed
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun custom_collapse_threshold_200dp_works_correctly() = runComposeUiTest {
        setContent { TestScreen(collapseThreshold = 200.dp) }
        waitForIdle()

        // Initially not collapsed
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("false")

        // Scroll to trigger collapse with larger threshold
        onNodeWithTag(LIST_TAG).performScrollToIndex(1)
        waitForIdle()

        // Should be collapsed (scrolling to item 1 moves past index 0)
        onNodeWithTag(COLLAPSED_TAG).assertTextEquals("true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun negative_tilePadding_parameter_is_passed_through() = runComposeUiTest {
        setContent { TestScreen(tilePadding = (-5).dp) }
        waitForIdle()

        // Should pass through the negative value as-is
        onNodeWithTag(TILE_PADDING_TAG).assertTextEquals("-5")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun zero_appBarHeight_parameter_is_passed_through() = runComposeUiTest {
        setContent { TestScreen(appBarHeightDp = 0.dp) }
        waitForIdle()

        // Should pass through zero value
        onNodeWithTag(APP_BAR_HEIGHT_TAG).assertTextEquals("0")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun very_large_parameters_are_handled() = runComposeUiTest {
        setContent { TestScreen(tilePadding = 9999.dp, appBarHeightDp = 8888.dp) }
        waitForIdle()

        // Should pass through large values
        onNodeWithTag(TILE_PADDING_TAG).assertTextEquals("9999")
        onNodeWithTag(APP_BAR_HEIGHT_TAG).assertTextEquals("8888")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun commonScreenState_data_class_equality_works() = runComposeUiTest {
        var state1: CommonScreenState? = null
        var state2: CommonScreenState? = null
        
        setContent {
            state1 = rememberCommonScreenState(
                collapseThreshold = 120.dp,
                tilePadding = 16.dp,
                appBarHeightDp = 64.dp
            )
            state2 = rememberCommonScreenState(
                collapseThreshold = 120.dp,
                tilePadding = 16.dp,
                appBarHeightDp = 64.dp
            )
        }
        waitForIdle()

        // Two states created with same parameters should have equal property values
        assertTrue(state1 != null && state2 != null, "Both states should be initialized")
        assertTrue(state1!!.tilePadding == state2!!.tilePadding, "tilePadding should match")
        assertTrue(state1.appBarHeightDp == state2.appBarHeightDp, "appBarHeightDp should match")
        assertTrue(state1.isCollapsed == state2.isCollapsed, "isCollapsed should match initially")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun commonScreenState_data_class_copy_works() = runComposeUiTest {
        var originalState: CommonScreenState? = null
        
        setContent {
            originalState = rememberCommonScreenState(tilePadding = 16.dp, appBarHeightDp = 64.dp)
        }
        waitForIdle()

        // Test copy functionality
        val copiedState = originalState!!.copy(tilePadding = 32.dp)
        
        // Copied state should have new tilePadding but same other immutable properties
        assertTrue(copiedState.tilePadding == 32.dp, "Copied state should have new tilePadding value")
        assertTrue(copiedState.appBarHeightDp == originalState!!.appBarHeightDp, "appBarHeightDp should remain the same")
        assertTrue(copiedState.statusBarHeightDp == originalState!!.statusBarHeightDp, "statusBarHeightDp should remain the same")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun commonScreenState_all_properties_are_accessible() = runComposeUiTest {
        var state: CommonScreenState? = null
        
        setContent {
            state = rememberCommonScreenState(tilePadding = 20.dp, appBarHeightDp = 80.dp)
        }
        waitForIdle()

        // Verify all 10 properties are accessible and have reasonable values
        assertTrue(state != null, "State should be initialized")
        assertTrue(state!!.statusBarHeightDp.value >= 0f, "statusBarHeightDp should be non-negative")
        assertTrue(state!!.bottomInset.value >= 0f, "bottomInset should be non-negative")
        assertTrue(state!!.listState != null, "listState should not be null")
        assertTrue(state!!.isCollapsed == false, "isCollapsed should be false initially")
        assertTrue(state!!.alpha >= 0f && state!!.alpha <= 1f, "alpha should be between 0 and 1")
        assertTrue(state!!.titleAlpha >= 0f && state!!.titleAlpha <= 1f, "titleAlpha should be between 0 and 1")
        assertTrue(state!!.bgAlpha >= 0f && state!!.bgAlpha <= 1f, "bgAlpha should be between 0 and 1")
        assertTrue(state!!.isCompactScreen != null, "isCompactScreen should not be null")
        assertTrue(state!!.tilePadding == 20.dp, "tilePadding should match input")
        assertTrue(state!!.appBarHeightDp == 80.dp, "appBarHeightDp should match input")
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
    var recomposeFlag by remember { mutableStateOf(false) }

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
    Text(
        modifier = Modifier.testTag(STATUS_BAR_HEIGHT_TAG),
        text = String.format("%.2f", state.statusBarHeightDp.value)
    )
    Text(
        modifier = Modifier.testTag(ALPHA_DIFF_TAG),
        text = String.format("%.2f", state.titleAlpha - state.bgAlpha)
    )
    Text(
        modifier = Modifier.testTag(RECOMPOSE_TAG),
        text = recomposeFlag.toString()
    )

    // Controls to drive precise scroll behavior in tests
    Button(modifier = Modifier.testTag("triggerRecompose"), onClick = {
        recomposeFlag = !recomposeFlag
    }) { Text("recomp") }
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
