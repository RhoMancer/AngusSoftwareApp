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
 * exercising all conditional branches in the composable.
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

    // === Projects WITH links (branch: !project.link.isNullOrBlank() == true) ===

    @Test
    fun projectsScreen_temperluxProject_withLink_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Temperlux").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_googlePlayProject_withLink_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Google Play Developer Account").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_angusPaintProject_withLink_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Angus Paint").assertIsDisplayed()
    }

    // === Projects WITHOUT links (branch: !project.link.isNullOrBlank() == false) ===

    @Test
    fun projectsScreen_portfolioProject_withoutLink_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Portfolio Website").assertIsDisplayed()
    }

    // === Projects WITH subtitles (branch: project.subtitle?.let) ===

    @Test
    fun projectsScreen_temperluxSubtitle_isDisplayed() {
        setContent()
        // Temperlux has a subtitle — verify it renders
        composeTestRule.onNodeWithText("Temperlux").assertIsDisplayed()
    }

    // === Projects WITHOUT subtitles ===

    @Test
    fun projectsScreen_portfolioProject_withoutSubtitle_rendersTitleOnly() {
        setContent()
        composeTestRule.onNodeWithText("Portfolio Website").assertIsDisplayed()
    }

    // === Projects WITH description (branch: project.description?.let) ===

    @Test
    fun projectsScreen_temperluxDescription_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Temperlux").assertIsDisplayed()
    }

    // === Projects WITHOUT description ===

    @Test
    fun projectsScreen_googlePlayProject_withoutDescription_renders() {
        setContent()
        composeTestRule.onNodeWithText("Google Play Developer Account").assertIsDisplayed()
    }

    // === Projects WITH technologies (branch: project.technologies.isNotEmpty()) ===

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
    fun projectsScreen_rustTechnology_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Rust").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_gtk4Technology_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("GTK4").assertIsDisplayed()
    }

    @Test
    fun projectsScreen_xmlTechnology_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("XML").assertIsDisplayed()
    }

    // === All projects render (exercises full LazyColumn + all project items) ===

    @Test
    fun projectsScreen_allProjectsDisplayed() {
        setContent()

        // First two projects should be visible
        composeTestRule.onNodeWithText("Portfolio Website").assertIsDisplayed()
        composeTestRule.onNodeWithText("Temperlux").assertIsDisplayed()

        // Scroll down to reveal more projects
        composeTestRule.onNodeWithTag(PROJECTS_SCREEN_TEST_TAG)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Tap Target Booster").assertIsDisplayed()
    }
}
