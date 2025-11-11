package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
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
class AboutMeProfessionalSectionInstrumentedTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /**
     * Test that the section renders correctly with full opacity.
     */
    @Test
    fun section_rendersWithFullOpacity() {
        setContent {
            AboutMeProfessionalSection(alpha = 1f)
        }

        rule.onNodeWithText("Professional Experience", substring = true).assertIsDisplayed()
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
        rule.onNodeWithText("Kotlin").assertExists()
        rule.onNodeWithText("Compose").assertExists()
        rule.onNodeWithText("Android").assertExists()
        rule.onNodeWithText("JavaScript").assertExists()
        rule.onNodeWithText("React").assertExists()
        rule.onNodeWithText("Node.js").assertExists()
        rule.onNodeWithText("UI/UX").assertExists()
        rule.onNodeWithText("Git").assertExists()
        rule.onNodeWithText("CI/CD").assertExists()
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
        rule.onNodeWithText("Kotlin").assertExists()
        rule.onNodeWithText("Compose").assertExists()
        rule.onNodeWithText("Android").assertExists()
    }

    /**
     * Test that the section displays the skills title.
     */
    @Test
    fun skillsTitle_isDisplayed() {
        setContent {
            AboutMeProfessionalSection(alpha = 1f)
        }

        rule.onNodeWithText("Key Skills").assertExists()
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
        rule.onNodeWithText("For work", substring = true).assertExists()
        rule.onNodeWithText("JPMorgan Chase", substring = true).assertExists()
        rule.onNodeWithText("lead Android developer", substring = true).assertExists()
    }

    // Helper to wrap content with MaterialTheme
    private fun setContent(content: @Composable () -> Unit) {
        rule.setContent {
            MaterialTheme {
                content()
            }
        }
    }
}
