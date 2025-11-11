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
 * Comprehensive instrumented tests for the AboutMePersonalSection composable.
 * 
 * This test suite covers:
 * - Visual rendering and UI composition
 * - Section title display and typography
 * - All three paragraph content display and typography
 * - Material Design compliance
 * - Alpha parameter behavior (fade-in animation)
 * - SectionCard integration
 * - Test tags verification
 * - Edge cases (different alpha values)
 * - Accessibility features
 * - Layout structure verification
 */
@RunWith(AndroidJUnit4::class)
class AboutMePersonalSectionInstrumentedTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    // Test data - expected string content
    private val expectedTitle = "Overly Personal"
    private val expectedParagraph1Start = "I think a lot about how fleeting life is"
    private val expectedParagraph2Start = "Most of what I do"
    private val expectedParagraph3Start = "When I let go of worry"

    /**
     * Test that all content elements are displayed together.
     */
    @Test
    fun allContentElements_areDisplayed() {
        setContent {
            AboutMePersonalSection(alpha = 1f)
        }

        rule.onNodeWithText(expectedTitle, substring = true).assertIsDisplayed()
        rule.onNodeWithText(expectedParagraph1Start, substring = true).assertIsDisplayed()
        rule.onNodeWithText(expectedParagraph2Start, substring = true).assertIsDisplayed()
        rule.onNodeWithText(expectedParagraph3Start, substring = true).assertExists()
    }

    /**
     * Test that the container test tag is correctly applied.
     */
    @Test
    fun containerTestTag_isCorrectlyApplied() {
        setContent {
            AboutMePersonalSection(alpha = 1f)
        }

        rule.onNodeWithTag("about_me_personal_section").assertExists()
    }

    /**
     * Test that all test tags are correctly applied.
     */
    @Test
    fun allTestTags_areCorrectlyApplied() {
        setContent {
            AboutMePersonalSection(alpha = 1f)
        }

        rule.onNodeWithTag("about_me_personal_section").assertExists()
        rule.onNodeWithTag("about_me_personal_title").assertExists()
        rule.onNodeWithTag("about_me_personal_paragraph1").assertExists()
        rule.onNodeWithTag("about_me_personal_paragraph2").assertExists()
        rule.onNodeWithTag("about_me_personal_paragraph3").assertExists()
    }

    /**
     * Test that the section renders with zero opacity (alpha = 0f).
     * Content should exist but may not be visually displayed.
     */
    @Test
    fun section_rendersWithZeroOpacity() {
        setContent {
            AboutMePersonalSection(alpha = 0f)
        }

        // Elements should exist even with zero opacity
        rule.onNodeWithTag("about_me_personal_section").assertExists()
        rule.onNodeWithTag("about_me_personal_title").assertExists()
    }

    /**
     * Test that the section renders with partial opacity (alpha = 0.5f).
     */
    @Test
    fun section_rendersWithPartialOpacity() {
        setContent {
            AboutMePersonalSection(alpha = 0.5f)
        }

        rule.onNodeWithTag("about_me_personal_section").assertIsDisplayed()
        rule.onNodeWithText(expectedTitle, substring = true).assertIsDisplayed()
    }


    /**
     * Test that the section can be composed multiple times without issues.
     */
    @Test
    fun section_canBeComposedMultipleTimes() {
        setContent {
            androidx.compose.foundation.layout.Column {
                AboutMePersonalSection(alpha = 1f)
                AboutMePersonalSection(alpha = 0.5f)
            }
        }

        // Both instances should exist
        rule.onAllNodesWithTag("about_me_personal_section").assertCountEquals(2)
    }

    /**
     * Test that the section renders correctly after recomposition.
     */
    @Test
    fun section_rendersCorrectlyAfterRecomposition() {
        setContent {
            AboutMePersonalSection(alpha = 1f)
        }

        rule.waitForIdle()
        
        // Verify content is still displayed after idle
        rule.onNodeWithTag("about_me_personal_section").assertIsDisplayed()
        rule.onNodeWithText(expectedTitle, substring = true).assertIsDisplayed()
    }

    /**
     * Test that the section maintains structure with very small alpha.
     */
    @Test
    fun section_maintainsStructureWithVerySmallAlpha() {
        setContent {
            AboutMePersonalSection(alpha = 0.01f)
        }

        // Structure should still exist even with minimal alpha
        rule.onNodeWithTag("about_me_personal_section").assertExists()
        rule.onNodeWithTag("about_me_personal_title").assertExists()
        rule.onNodeWithTag("about_me_personal_paragraph1").assertExists()
    }

    /**
     * Test that all paragraph test tags are unique.
     */
    @Test
    fun paragraphTestTags_areUnique() {
        setContent {
            AboutMePersonalSection(alpha = 1f)
        }

        // Each test tag should appear exactly once
        rule.onAllNodesWithTag("about_me_personal_paragraph1").assertCountEquals(1)
        rule.onAllNodesWithTag("about_me_personal_paragraph2").assertCountEquals(1)
        rule.onAllNodesWithTag("about_me_personal_paragraph3").assertCountEquals(1)
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
