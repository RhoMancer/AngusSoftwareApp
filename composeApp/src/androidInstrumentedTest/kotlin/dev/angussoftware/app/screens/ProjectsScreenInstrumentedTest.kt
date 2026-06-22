package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertExists
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
 * ProjectsScreen hardcodes 7 projects with varying optional fields:
 * - Portfolio Website: has description + technologies, NO link, NO images, has icon
 * - Temperlux: has subtitle + description + link + technologies, NO images
 * - Google Play Developer Account: has link + icon only, NO description/images/tech
 * - Angus Paint: has all fields including 7 images
 * - Angus Solitaire: has all fields including 3 images
 * - Blink Reader: has all fields including 3 images
 * - Tap Target Booster: has all fields including 4 images
 *
 * These tests verify that projects with different field combinations render correctly,
 * exercising all conditional branches in the composable. Uses assertExists for items
 * that may be off-screen in the LazyColumn (JaCoCo still counts them as covered when
 * the composable executes, even if not displayed).
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

    private fun scrollDown() {
        composeTestRule.onNodeWithTag(PROJECTS_SCREEN_TEST_TAG)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()
    }

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

    // === Projects visible at top of LazyColumn ===

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

    // === Projects requiring scroll (use assertExists — still exercises composable code) ===

    @Test
    fun projectsScreen_googlePlayProject_exists() {
        setContent()
        composeTestRule.onNodeWithText("Google Play Developer Account").assertExists()
    }

    @Test
    fun projectsScreen_angusPaintProject_exists() {
        setContent()
        composeTestRule.onNodeWithText("Angus Paint").assertExists()
    }

    @Test
    fun projectsScreen_angusSolitaireProject_exists() {
        setContent()
        composeTestRule.onNodeWithText("Angus Solitaire").assertExists()
    }

    @Test
    fun projectsScreen_blinkReaderProject_exists() {
        setContent()
        composeTestRule.onNodeWithText("Blink Reader").assertExists()
    }

    @Test
    fun projectsScreen_tapTargetBoosterProject_exists() {
        setContent()
        composeTestRule.onNodeWithText("Tap Target Booster").assertExists()
    }

    // === Technology chips (branch: project.technologies.isNotEmpty()) ===

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
    fun projectsScreen_rustTechnology_exists() {
        setContent()
        composeTestRule.onNodeWithText("Rust").assertExists()
    }

    @Test
    fun projectsScreen_gtk4Technology_exists() {
        setContent()
        composeTestRule.onNodeWithText("GTK4").assertExists()
    }

    @Test
    fun projectsScreen_xmlTechnology_exists() {
        setContent()
        composeTestRule.onNodeWithText("XML").assertExists()
    }

    // === Scroll exercise (exercises LazyColumn + page indicator dots) ===

    @Test
    fun projectsScreen_scrollRevealsMoreProjects() {
        setContent()

        composeTestRule.onNodeWithText("Portfolio Website").assertIsDisplayed()

        scrollDown()
        scrollDown()

        composeTestRule.onNodeWithText("Tap Target Booster").assertExists()
    }
}
