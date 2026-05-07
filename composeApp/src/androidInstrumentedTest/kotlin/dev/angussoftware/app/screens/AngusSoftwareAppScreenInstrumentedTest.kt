package dev.angussoftware.app.screens

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import dev.angussoftware.app.navigation.NAV_HOST_TEST_TAG
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Test

class AngusSoftwareAppScreenInstrumentedTest : BaseScreenTest() {

    /**
     * Verified compact layout shows BottomNavigationBar, hides NavigationRail,
     * and NavHost is present.
     */
    @Test
    fun compactLayout_showsBottomNavBar() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            AngusSoftwareAppScreen()
        }

        composeTestRule.onNodeWithTag(NAV_BAR_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(NAV_RAIL_TEST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(NAV_HOST_TEST_TAG).assertExists()
    }

    /**
     * Verified non-compact layout shows NavigationRail, hides BottomNavigationBar,
     * and NavHost is present.
     */
    @Test
    fun nonCompactLayout_showsNavigationRail() {
        setAdaptiveContent(WindowWidthSizeClass.EXPANDED) {
            AngusSoftwareAppScreen()
        }

        composeTestRule.onNodeWithTag(NAV_RAIL_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(NAV_BAR_TEST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(NAV_HOST_TEST_TAG).assertExists()
    }

    /**
     * Verified navigation selection toggles as expected:
     * - Initial: Home selected, Projects not selected
     * - After Projects click: Projects selected, Home unselected
     * - After Blog click: Blog selected, Projects unselected
     */
    @Test
    fun navigation_clicksUpdateSelection() {
        setAdaptiveContent(WindowWidthSizeClass.COMPACT) {
            AngusSoftwareAppScreen()
        }

        // Verify initial selection
        composeTestRule.onNodeWithTag(NAV_ITEM_HOME_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).assertIsNotSelected()

        // Click Projects then verify selection toggles
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(NAV_ITEM_HOME_TAG).assertIsNotSelected()

        // Click Blog then verify selection toggles
        composeTestRule.onNodeWithTag(NAV_ITEM_BLOG_TAG).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(NAV_ITEM_BLOG_TAG).assertIsSelected()
        composeTestRule.onNodeWithTag(NAV_ITEM_PROJECTS_TAG).assertIsNotSelected()
    }
}
