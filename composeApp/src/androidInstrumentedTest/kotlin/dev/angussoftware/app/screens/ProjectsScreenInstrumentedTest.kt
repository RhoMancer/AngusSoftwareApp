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
 * ProjectsScreen uses a LazyColumn with 7 hardcoded projects. Only items in
 * the viewport are composed. These tests exercise:
 * - Both layout branches (compact / expanded)
 * - Projects with and without links (Portfolio has no link, Temperlux has one)
 * - Technology chips from visible projects
 * - Progressive scroll through the full list
 *
 * Note: BlogPostScreenInstrumentedTest covers the other major gap.
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
    // Portfolio Website: NO link, has description + technologies + icon
    // Temperlux: has link, subtitle, description, technologies, NO images

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

    // === Technology chips from visible projects (Kotlin, Compose, Multiplatform on Portfolio; Rust, GTK4 on Temperlux) ===

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
        composeTestRule.onNodeWithText("Rust").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_gtk4Technology_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("GTK4").assertIsDisplayed()
    }

    // === Scroll exercise — progressively scroll and verify projects appear ===
    // Each swipeUp scrolls roughly one project card height.

    @Test
    fun projectsScreen_scrollThroughAllProjects() {
        setContent()

        // First two visible without scroll
        composeTestRule.onNodeWithText("Portfolio Website").assertIsDisplayed()
        composeTestRule.onNodeWithText("Temperlux").assertIsDisplayed()

        // Scroll and verify remaining projects appear one at a time
        val remainingProjects = listOf(
            "Google Play Developer Account",
            "Angus Paint",
            "Angus Solitaire",
            "Blink Reader",
            "Tap Target Booster",
        )

        for (projectTitle in remainingProjects) {
            var found = false
            for (attempt in 1..5) {
                composeTestRule.onNodeWithTag(PROJECTS_SCREEN_TEST_TAG)
                    .performTouchInput { swipeUp() }
                composeTestRule.waitForIdle()
                try {
                    composeTestRule.onNodeWithText(projectTitle).assertIsDisplayed()
                    found = true
                    break
                } catch (_: AssertionError) {
                    // Not yet visible, keep scrolling
                }
            }
            assert(found) { "Failed to scroll to project: $projectTitle" }
        }
    }
}
