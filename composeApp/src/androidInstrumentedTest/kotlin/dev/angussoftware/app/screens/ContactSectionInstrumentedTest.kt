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
 * Comprehensive instrumented tests for the ContactSection composable.
 * 
 * This test suite covers:
 * - Visual rendering and UI composition
 * - Section title display and test tags
 * - ContactItem integration (email and location)
 * - SocialMediaButton integration (all 4 platforms)
 * - Alpha parameter behavior
 * - SectionCard integration
 * - Container structure and hierarchy
 * - Accessibility features
 * - Material Design compliance
 * - Multiple composition scenarios
 * - Real-world contact data rendering
 * - Edge cases
 */
@RunWith(AndroidJUnit4::class)
class ContactSectionInstrumentedTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /**
     * Test that the ContactSection container is displayed.
     */
    @Test
    fun contactSection_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithTag("contact_section").assertIsDisplayed()
    }

    /**
     * Test that the section title is displayed correctly.
     */
    @Test
    fun sectionTitle_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithText("Contact Information").assertIsDisplayed()
    }

    /**
     * Test that the section title test tag is correctly applied.
     */
    @Test
    fun sectionTitleTestTag_isCorrectlyApplied() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithTag("contact_section_title").assertExists()
    }

    /**
     * Test that email ContactItem is displayed.
     */
    @Test
    fun emailContactItem_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithText("Email").assertIsDisplayed()
        rule.onNodeWithText("rhomancer@proton.me").assertIsDisplayed()
    }

    /**
     * Test that location ContactItem is displayed.
     */
    @Test
    fun locationContactItem_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithText("Location").assertIsDisplayed()
        rule.onNodeWithText("Chicago IL").assertIsDisplayed()
    }

    /**
     * Test that both ContactItems are present with correct test tags.
     */
    @Test
    fun contactItems_haveCorrectTestTags() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithTag("contact_item_Email").assertExists()
        rule.onNodeWithTag("contact_item_Location").assertExists()
    }

    /**
     * Test that the "Connect with me" subtitle is displayed.
     */
    @Test
    fun socialMediaSubtitle_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithText("Connect with me").assertIsDisplayed()
    }

    /**
     * Test that the social media subtitle test tag is correctly applied.
     */
    @Test
    fun socialMediaSubtitleTestTag_isCorrectlyApplied() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithTag("contact_section_social_title").assertExists()
    }


    /**
     * Test that all four social media buttons are present.
     */
    @Test
    fun allSocialMediaButtons_arePresent() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithText("LinkedIn").assertExists()
        rule.onNodeWithText("GitHub").assertExists()
        rule.onNodeWithText("BlueSky").assertExists()
        rule.onNodeWithText("ProtoPro").assertExists()
    }

    /**
     * Test that the section renders with alpha = 0.5f (semi-transparent).
     */
    @Test
    fun section_rendersWithHalfAlpha() {
        setContent {
            ContactSection(alpha = 0.5f)
        }

        rule.onNodeWithTag("contact_section").assertExists()
        rule.onNodeWithText("Contact Information").assertExists()
    }

    /**
     * Test that the section renders with alpha = 0f (invisible but present).
     */
    @Test
    fun section_rendersWithZeroAlpha() {
        setContent {
            ContactSection(alpha = 0f)
        }

        rule.onNodeWithTag("contact_section").assertExists()
    }

    /**
     * Test that the section maintains proper structure with all main elements.
     */
    @Test
    fun section_hasProperStructure() {
        setContent {
            ContactSection(alpha = 1f)
        }

        // Container
        rule.onNodeWithTag("contact_section").assertExists()
        
        // Title
        rule.onNodeWithTag("contact_section_title").assertExists()
        
        // Contact items
        rule.onNodeWithTag("contact_item_Email").assertExists()
        rule.onNodeWithTag("contact_item_Location").assertExists()
        
        // Social media subtitle
        rule.onNodeWithTag("contact_section_social_title").assertExists()
        
        // Social media buttons
        rule.onNodeWithTag("social_media_button_LinkedIn").assertExists()
        rule.onNodeWithTag("social_media_button_GitHub").assertExists()
        rule.onNodeWithTag("social_media_button_BlueSky").assertExists()
        rule.onNodeWithTag("social_media_button_ProtoPro").assertExists()
    }

    /**
     * Test that social media buttons are clickable.
     */
    @Test
    fun socialMediaButtons_areClickable() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithTag("social_media_button_LinkedIn").assertHasClickAction()
        rule.onNodeWithTag("social_media_button_GitHub").assertHasClickAction()
        rule.onNodeWithTag("social_media_button_BlueSky").assertHasClickAction()
        rule.onNodeWithTag("social_media_button_ProtoPro").assertHasClickAction()
    }

    /**
     * Test that the section can be composed multiple times.
     */
    @Test
    fun section_canBeComposedMultipleTimes() {
        setContent {
            androidx.compose.foundation.layout.Column {
                ContactSection(alpha = 1f)
                ContactSection(alpha = 0.5f)
            }
        }

        // Both sections should exist
        rule.onAllNodesWithTag("contact_section").assertCountEquals(2)
    }

    /**
     * Test that the section integrates properly with SectionCard.
     */
    @Test
    fun section_integratesWithSectionCard() {
        setContent {
            ContactSection(alpha = 1f)
        }

        // The section should be wrapped in a card
        rule.onNodeWithTag("contact_section").assertExists()
        rule.onNodeWithTag("contact_section").assertIsDisplayed()
    }

    /**
     * Test that the section handles recomposition correctly.
     */
    @Test
    fun section_handlesRecompositionCorrectly() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onNodeWithTag("contact_section").assertIsDisplayed()
        
        // Force recomposition
        rule.waitForIdle()
        
        // Verify everything is still present
        rule.onNodeWithTag("contact_section").assertIsDisplayed()
        rule.onNodeWithText("Contact Information").assertIsDisplayed()
    }

    /**
     * Test edge case with very small alpha value.
     */
    @Test
    fun section_handlesVerySmallAlpha() {
        setContent {
            ContactSection(alpha = 0.01f)
        }

        rule.onNodeWithTag("contact_section").assertExists()
    }

    /**
     * Test that all four social media buttons have unique tags.
     */
    @Test
    fun socialMediaButtons_haveUniqueTags() {
        setContent {
            ContactSection(alpha = 1f)
        }

        rule.onAllNodesWithTag("social_media_button_LinkedIn").assertCountEquals(1)
        rule.onAllNodesWithTag("social_media_button_GitHub").assertCountEquals(1)
        rule.onAllNodesWithTag("social_media_button_BlueSky").assertCountEquals(1)
        rule.onAllNodesWithTag("social_media_button_ProtoPro").assertCountEquals(1)
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
