package dev.angussoftware.app.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import kotlin.test.Test

private const val LIST_TAG = "list"
private const val COLLAPSED_TAG = "collapsed"
private const val TITLE_ALPHA_TAG = "titleAlpha"

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
}

@androidx.compose.runtime.Composable
private fun TestScreen() {
    val state = rememberCommonScreenState()

    // Expose state via tagged text for assertions
    Text(modifier = Modifier.testTag(COLLAPSED_TAG), text = state.isCollapsed.toString())
    Text(
        modifier = Modifier.testTag(TITLE_ALPHA_TAG),
        text = String.format("%.2f", state.titleAlpha)
    )

    // Content with a scrollable list using the state's listState
    LazyColumn(state = state.listState, modifier = Modifier.testTag(LIST_TAG)) {
        // Enough items to allow scrolling
        items((0 until 50).toList()) { _ ->
            Box(Modifier.fillMaxWidth().height(200.dp))
        }
    }
}
