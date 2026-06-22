package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dev.angussoftware.app.ui.utils.LocalOverrideWindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.LocalWindowAdaptiveInfoOverride
import dev.angussoftware.app.ui.utils.WindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for [ProjectsScreen].
 *
 * ProjectsScreen uses a LazyColumn with 7 projects. Only items in the viewport
 * are composed, so tests must scroll to reach lower items. These tests exercise:
 * - Compact and expanded layouts
 * - Projects with/without links (branch on project.link)
 * - Projects with/without subtitles, descriptions
 * - Technology chips rendering (branch on project.technologies.isNotEmpty)
 * - Full list scroll (exercises LazyColumn + all project composables)
 */
class ProjectsScreenInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.COMPACT) {
        val adaptiveInfo = WindowAdaptiveInfo(widthSizeClass)
        composeTestRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides adaptiveInfo,
                LocalOverrideWindowAdaptiveInfo provides adaptiveInfo,
            ) {
                ProjectsScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun scrollDown(times: Int = 1) {
        repeat(times) {
            composeTestRule.onNodeWithTag(PROJECTS_SCREEN_TEST_TAG)
                .performTouchInput { swipeUp() }
            composeTestRule.waitForIdle()
        }
    }

    // === Layout branches ===

    @Test
    fun projectsScreen_compactLayout_isDisplayed() {
        setContent(WindowWidthSizeClass.COMPACT)
        composeTestRule.onNodeWithTag(PROJECTS_SCREEN_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun projectsScreen_expandedLayout_isDisplayed() {
        setContent(WindowWidthSizeClass.EXPANDED)
        composeTestRule.onNodeWithTag(PROJECTS_SCREEN_TEST_TAG).assertIsDisplayed()
    }

    // === First two projects (visible without scrolling) ===
    // Portfolio Website: NO link, has description + technologies
    // Temperlux: has link, subtitle, description, technologies

    @Test
    fun projectsScreen_portfolioProject_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Portfolio Website").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_temperluxProject_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Temperlux").assertIsDisplayed()
    }

    // === Technology chips from visible projects ===

    @Test
    fun projectsScreen_kotlinTechnology_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Kotlin").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_composeTechnology_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Compose").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_multiplatformTechnology_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Multiplatform").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_rustTechnology_isDisplayed() {
        setContent()
        // Temperlux has "Rust" as a technology chip
        composeTestRule.onNodeWithText("Rust").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_gtk4Technology_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("GTK4").assertIsDisplayed()
    }

    // === Scroll to reach remaining projects ===

    @Test
    fun projectsScreen_scrollToGooglePlayProject() {
        setContent()
        scrollDown(1)
        composeTestRule.onNodeWithText("Google Play Developer Account").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_scrollToAngusPaint() {
        setContent()
        scrollDown(2)
        composeTestRule.onNodeWithText("Angus Paint").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_scrollToAngusSolitaire() {
        setContent()
        scrollDown(3)
        composeTestRule.onNodeWithText("Angus Solitaire").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_scrollToBlinkReader() {
        setContent()
        scrollDown(4)
        composeTestRule.onNodeWithText("Blink Reader").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_scrollToTapTargetBooster() {
        setContent()
        scrollDown(5)
        composeTestRule.onNodeWithText("Tap Target Booster").assertIsDisplayed()
    }

    // === XML tech chip (only on Angus Paint and below) ===

    @Test
    fun projectsScreen_xmlTechnology_afterScroll() {
        setContent()
        scrollDown(2)
        composeTestRule.onNodeWithText("XML").assertIsDisplayed()
    }
}
