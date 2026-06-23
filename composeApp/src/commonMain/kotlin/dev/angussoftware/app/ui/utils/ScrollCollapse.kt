package dev.angussoftware.app.ui.utils

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
