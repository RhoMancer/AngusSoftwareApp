package dev.angussoftware.app.screens

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import dev.angussoftware.app.navigation.NAV_HOST_TEST_TAG
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Test

class AngusSoftwareAppScreenContentInstrumentedTest : BaseScreenTest() {

    /**
     * Verifies that navigating via BottomNavigationBar swaps the content area
     * to the correct destination screen in compact mode, and that selection states reflect the route.
     * Asserts presence of HOME_SCREEN_TEST_TAG, PROJECTS_SCREEN_TEST_TAG, and BLOG_SCREEN_TEST_TAG as navigation occurs.
     */
    @Test
    fun compact_navigation_showsCorrectContent() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            AngusSoftwareAppScreen()
        }

        // NavHost always exists
        composeTestRule.onNodeWithTag(NAV_HOST_TEST_TAG).assertExists()

        // Initial: Home
        composeTestRule.onNodeWithTag(NAV_ITEM_HOME_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).assertIsNotSelected()
        composeTestRule.onNodeWithTag(HOME_SCREEN_TEST_TAG).assertExists()

        // Navigate to Projects
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(NAV_ITEM_HOME_TAG).assertIsNotSelected()
        composeTestRule.onNodeWithTag(PROJECTS_SCREEN_TEST_TAG).assertExists()

        // Navigate to Blog
        composeTestRule.onNodeWithTag(NAV_ITEM_BLOG_TAG).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NAV_ITEM_BLOG_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).assertIsNotSelected()
        composeTestRule.onNodeWithTag(BLOG_SCREEN_TEST_TAG).assertExists()
    }

    /**
     * Verifies that NavigationRail interaction (non-compact mode) correctly
     * updates selection and swaps the content area to the expected destination screens.
     * Confirms rail presence and content tags on navigation.
     */
    @Test
    fun nonCompact_navigationRail_selectionAndContent() {
        setAdaptiveContent(WindowWidthSizeClass.EXPANDED) {
            AngusSoftwareAppScreen()
        }

        // Ensure NavigationRail is shown in non-compact mode
        composeTestRule.onNodeWithTag(NAV_RAIL_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(NAV_BAR_TEST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(NAV_HOST_TEST_TAG).assertExists()

        // Initial: Home
        composeTestRule.onNodeWithTag(NAV_ITEM_HOME_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(HOME_SCREEN_TEST_TAG).assertExists()

        // Navigate to Projects
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(PROJECTS_SCREEN_TEST_TAG).assertExists()

        // Navigate to Blog
        composeTestRule.onNodeWithTag(NAV_ITEM_BLOG_TAG).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NAV_ITEM_BLOG_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(BLOG_SCREEN_TEST_TAG).assertExists()
    }
}
