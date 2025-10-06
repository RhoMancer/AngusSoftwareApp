package dev.angussoftware.app.ui.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonScreenStateCollapseTest {

    @Test
    fun collapse_when_index_gt_zero() {
        assertTrue(isCollapsedFor(listIndex = 1, listOffsetPx = 0, thresholdPx = 100))
        assertTrue(isCollapsedFor(listIndex = 10, listOffsetPx = 0, thresholdPx = 100))
    }

    @Test
    fun collapse_when_offset_gt_threshold() {
        assertTrue(isCollapsedFor(listIndex = 0, listOffsetPx = 101, thresholdPx = 100))
        assertTrue(isCollapsedFor(listIndex = 0, listOffsetPx = Int.MAX_VALUE, thresholdPx = 100))
    }

    @Test
    fun not_collapsed_when_below_threshold_at_top() {
        assertFalse(isCollapsedFor(listIndex = 0, listOffsetPx = 0, thresholdPx = 100))
        assertFalse(isCollapsedFor(listIndex = 0, listOffsetPx = 99, thresholdPx = 100))
    }

    @Test
    fun not_collapsed_when_offset_equals_threshold() {
        assertFalse(isCollapsedFor(listIndex = 0, listOffsetPx = 100, thresholdPx = 100))
    }
}
