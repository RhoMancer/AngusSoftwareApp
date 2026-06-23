package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performScrollToNode
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

    private fun scrollToText(text: String, substring: Boolean = false) {
        val matcher = if (substring) hasText(text, substring = true) else hasText(text)
        composeTestRule.onNodeWithTag("ProjectsList").performScrollToNode(matcher)
    }

    // === Projects with images (need scrolling to reach) ===

    @Test
    fun projectsScreen_angusPaintProject_exists() {
        setContent()
        scrollToText("Angus Paint")
        composeTestRule.onNodeWithText("Angus Paint").assertExists()
    }

    @Test
    fun projectsScreen_angusPaintDescription_exists() {
        setContent()
        scrollToText("Paint your own masterpiece", substring = true)
        composeTestRule.onNodeWithText("Paint your own masterpiece", substring = true).assertExists()
    }

    @Test
    fun projectsScreen_angusPaintTechnologies_exist() {
        setContent()
        scrollToText("Angus Paint")
        composeTestRule.onNodeWithText("XML", substring = true).assertExists()
    }

    @Test
    fun projectsScreen_solitaireProject_exists() {
        setContent()
        scrollToText("Angus Solitaire")
        composeTestRule.onNodeWithText("Angus Solitaire").assertExists()
    }

    @Test
    fun projectsScreen_solitaireDescription_exists() {
        setContent()
        scrollToText("score as many points", substring = true)
        composeTestRule.onNodeWithText("score as many points", substring = true).assertExists()
    }

    @Test
    fun projectsScreen_blinkReaderProject_exists() {
        setContent()
        scrollToText("Blink Reader")
        composeTestRule.onNodeWithText("Blink Reader").assertExists()
    }

    @Test
    fun projectsScreen_blinkReaderDescription_exists() {
        setContent()
        scrollToText("enhanced speed", substring = true)
        composeTestRule.onNodeWithText("enhanced speed", substring = true).assertExists()
    }

    @Test
    fun projectsScreen_tapTargetProject_exists() {
        setContent()
        scrollToText("Tap Target Booster")
        composeTestRule.onNodeWithText("Tap Target Booster").assertExists()
    }

    @Test
    fun projectsScreen_tapTargetDescription_exists() {
        setContent()
        scrollToText("touch screen skills", substring = true)
        composeTestRule.onNodeWithText("touch screen skills", substring = true).assertExists()
    }

    // === Google Play developer account entry ===

    @Test
    fun projectsScreen_googlePlayEntry_exists() {
        setContent()
        scrollToText("Google Play Developer Account")
        composeTestRule.onNodeWithText("Google Play Developer Account").assertExists()
    }

    // === Verify all project descriptions render ===

    @Test
    fun projectsScreen_portfolioDescription_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Portfolio Website").assertIsDisplayed()
        composeTestRule.onNodeWithText("dynamic content", substring = true).assertIsDisplayed()
    }

    @Test
    fun projectsScreen_temperluxDescription_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Temperlux").assertIsDisplayed()
        composeTestRule.onNodeWithText("Brightness controller", substring = true).assertIsDisplayed()
    }

    @Test
    fun projectsScreen_temperluxSubtitle_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Temperlux").assertIsDisplayed()
        composeTestRule.onNodeWithText("external and internal displays", substring = true).assertIsDisplayed()
    }
}
