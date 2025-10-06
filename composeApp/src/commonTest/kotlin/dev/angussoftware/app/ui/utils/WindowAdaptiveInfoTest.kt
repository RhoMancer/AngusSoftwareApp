package dev.angussoftware.app.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindowAdaptiveInfoTest {

    @Test
    fun fromWidth_compact_below600() {
        assertEquals(WindowWidthSizeClass.COMPACT, WindowWidthSizeClass.fromWidth(0))
        assertEquals(WindowWidthSizeClass.COMPACT, WindowWidthSizeClass.fromWidth(200))
        assertEquals(
            WindowWidthSizeClass.COMPACT,
            WindowWidthSizeClass.fromWidth(WindowWidthSizeClass.MEDIUM_WIDTH_THRESHOLD_DP - 1)
        )
    }

    @Test
    fun fromWidth_medium_600_to_839() {
        assertEquals(
            WindowWidthSizeClass.MEDIUM,
            WindowWidthSizeClass.fromWidth(WindowWidthSizeClass.MEDIUM_WIDTH_THRESHOLD_DP)
        )
        assertEquals(WindowWidthSizeClass.MEDIUM, WindowWidthSizeClass.fromWidth(700))
        assertEquals(
            WindowWidthSizeClass.MEDIUM,
            WindowWidthSizeClass.fromWidth(WindowWidthSizeClass.EXPANDED_WIDTH_THRESHOLD_DP - 1)
        )
    }

    @Test
    fun fromWidth_expanded_840_and_above() {
        assertEquals(
            WindowWidthSizeClass.EXPANDED,
            WindowWidthSizeClass.fromWidth(WindowWidthSizeClass.EXPANDED_WIDTH_THRESHOLD_DP)
        )
        assertEquals(WindowWidthSizeClass.EXPANDED, WindowWidthSizeClass.fromWidth(1200))
        assertEquals(WindowWidthSizeClass.EXPANDED, WindowWidthSizeClass.fromWidth(Int.MAX_VALUE))
    }

    @Test
    fun fromWidth_negative_treated_as_compact() {
        assertEquals(WindowWidthSizeClass.COMPACT, WindowWidthSizeClass.fromWidth(-1))
    }

    @Test
    fun adaptiveInfo_flags_match_sizeClass() {
        val compact = WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT)
        assertTrue(compact.isCompact)
        assertFalse(compact.isMedium)
        assertFalse(compact.isExpanded)

        val medium = WindowAdaptiveInfo(WindowWidthSizeClass.MEDIUM)
        assertFalse(medium.isCompact)
        assertTrue(medium.isMedium)
        assertFalse(medium.isExpanded)

        val expanded = WindowAdaptiveInfo(WindowWidthSizeClass.EXPANDED)
        assertFalse(expanded.isCompact)
        assertFalse(expanded.isMedium)
        assertTrue(expanded.isExpanded)
    }
}