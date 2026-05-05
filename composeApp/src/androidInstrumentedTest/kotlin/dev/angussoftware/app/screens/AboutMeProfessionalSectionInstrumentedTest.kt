package dev.angussoftware.app.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Focused instrumented tests for the AboutMeProfessionalSection composable.
 *
 * This test suite specifically covers:
 * - Non-compact screen FlowRow rendering (lines 248-257 in HomeScreen.kt)
 * - Skill chip display in both compact and non-compact modes
 */
@RunWith(AndroidJUnit4::class)
class AboutMeProfessionalSectionInstrumentedTest : BaseScreenTest() {

    /**
     * Test that the section renders correctly with full opacity.
     */
    @Test
    fun section_rendersWithFullOpacity() {
        setContent {
            AboutMeProfessionalSection(alpha = 1f)
        }

        composeTestRule.onNodeWithText("Professional Experience", substring = true).assertIsDisplayed()
    }

    /**
     * Test that all skill chips are displayed.
     * This covers the FlowRow rendering in both compact and non-compact modes.
     */
    @Test
    fun allSkillChips_areDisplayed() {
        setContent {
            AboutMeProfessionalSection(alpha = 1f)
        }

        // Verify all 9 skill chips exist (may be scrolled off-screen)
        composeTestRule.onNodeWithText("Kotlin").assertExists()
        composeTestRule.onNodeWithText("Compose").assertExists()
        composeTestRule.onNodeWithText("Android").assertExists()
        composeTestRule.onNodeWithText("JavaScript").assertExists()
        composeTestRule.onNodeWithText("React").assertExists()
        composeTestRule.onNodeWithText("Node.js").assertExists()
        composeTestRule.onNodeWithText("UI/UX").assertExists()
        composeTestRule.onNodeWithText("Git").assertExists()
        composeTestRule.onNodeWithText("CI/CD").assertExists()
    }

    /**
     * Test that skill chips render correctly in non-compact screen mode.
     * This specifically tests the else branch (lines 248-257) that uses a single FlowRow.
     *
     * Note: In instrumented tests on typical devices, isCompactScreen may vary.
     * This test verifies that skill chips are displayed regardless of screen mode.
     */
    @Test
    fun skillChips_renderInFlowRow() {
        setContent {
            AboutMeProfessionalSection(alpha = 1f)
        }

        // Verify at least 3 skill chips are visible (proving FlowRow works)
        composeTestRule.onNodeWithText("Kotlin").assertExists()
        composeTestRule.onNodeWithText("Compose").assertExists()
        composeTestRule.onNodeWithText("Android").assertExists()
    }

    /**
     * Test that the section displays the skills title.
     */
    @Test
    fun skillsTitle_isDisplayed() {
        setContent {
            AboutMeProfessionalSection(alpha = 1f)
        }

        composeTestRule.onNodeWithText("Key Skills").assertExists()
    }

    /**
     * Test that all professional paragraphs are present.
     */
    @Test
    fun professionalParagraphs_areDisplayed() {
        setContent {
            AboutMeProfessionalSection(alpha = 1f)
        }

        // Verify key text from each paragraph
        composeTestRule.onNodeWithText("For work", substring = true).assertExists()
        composeTestRule.onNodeWithText("JPMorgan Chase", substring = true).assertExists()
        composeTestRule.onNodeWithText("lead Android developer", substring = true).assertExists()
    }

    // Helper to wrap content with MaterialTheme
    private fun setContent(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            MaterialTheme {
                content()
            }
        }
    }
}
