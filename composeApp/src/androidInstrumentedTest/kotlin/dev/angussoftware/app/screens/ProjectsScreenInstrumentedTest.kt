package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.angussoftware.app.ui.utils.LocalOverrideWindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.LocalWindowAdaptiveInfoOverride
import dev.angussoftware.app.ui.utils.WindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for [ProjectsScreen].
 *
 * ProjectsScreen uses a LazyColumn with 7 hardcoded projects. These tests
 * exercise the branch paths for the initially visible projects:
 * - Both layout branches (compact / expanded)
 * - Projects with and without links (Portfolio has no link, Temperlux has one)
 * - Technology chips rendering (Kotlin, Compose, Multiplatform, Rust, GTK4)
 *
 * Lower projects (Angus Paint, etc.) are exercised indirectly by
 * AngusSoftwareAppScreenAdditionalInstrumentedTest which navigates to
 * the Projects tab in the full app.
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

    // === Technology chips from visible projects ===
    // Kotlin, Compose, Multiplatform on Portfolio; Rust, GTK4, libadwaita on Temperlux

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
}
