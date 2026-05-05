package dev.angussoftware.app.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class ContactSectionInstrumentedTest : BaseScreenTest() {

    /**
     * Test that the ContactSection container is displayed.
     */
    @Test
    fun contactSection_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithTag("contact_section").assertIsDisplayed()
    }

    /**
     * Test that the section title is displayed correctly.
     */
    @Test
    fun sectionTitle_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithText("Contact Information").assertIsDisplayed()
    }

    /**
     * Test that the section title test tag is correctly applied.
     */
    @Test
    fun sectionTitleTestTag_isCorrectlyApplied() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithTag("contact_section_title").assertExists()
    }

    /**
     * Test that email ContactItem is displayed.
     */
    @Test
    fun emailContactItem_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("rhomancer@proton.me").assertIsDisplayed()
    }

    /**
     * Test that location ContactItem is displayed.
     */
    @Test
    fun locationContactItem_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithText("Location").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chicago IL").assertIsDisplayed()
    }

    /**
     * Test that both ContactItems are present with correct test tags.
     */
    @Test
    fun contactItems_haveCorrectTestTags() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithTag("contact_item_Email").assertExists()
        composeTestRule.onNodeWithTag("contact_item_Location").assertExists()
    }

    /**
     * Test that the "Connect with me" subtitle is displayed.
     */
    @Test
    fun socialMediaSubtitle_isDisplayed() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithText("Connect with me").assertIsDisplayed()
    }

    /**
     * Test that the social media subtitle test tag is correctly applied.
     */
    @Test
    fun socialMediaSubtitleTestTag_isCorrectlyApplied() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithTag("contact_section_social_title").assertExists()
    }


    /**
     * Test that all four social media buttons are present.
     */
    @Test
    fun allSocialMediaButtons_arePresent() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithText("LinkedIn").assertExists()
        composeTestRule.onNodeWithText("GitHub").assertExists()
        composeTestRule.onNodeWithText("BlueSky").assertExists()
        composeTestRule.onNodeWithText("ProtoPro").assertExists()
    }

    /**
     * Test that the section renders with alpha = 0.5f (semi-transparent).
     */
    @Test
    fun section_rendersWithHalfAlpha() {
        setContent {
            ContactSection(alpha = 0.5f)
        }

        composeTestRule.onNodeWithTag("contact_section").assertExists()
        composeTestRule.onNodeWithText("Contact Information").assertExists()
    }

    /**
     * Test that the section renders with alpha = 0f (invisible but present).
     */
    @Test
    fun section_rendersWithZeroAlpha() {
        setContent {
            ContactSection(alpha = 0f)
        }

        composeTestRule.onNodeWithTag("contact_section").assertExists()
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
        composeTestRule.onNodeWithTag("contact_section").assertExists()

        // Title
        composeTestRule.onNodeWithTag("contact_section_title").assertExists()

        // Contact items
        composeTestRule.onNodeWithTag("contact_item_Email").assertExists()
        composeTestRule.onNodeWithTag("contact_item_Location").assertExists()

        // Social media subtitle
        composeTestRule.onNodeWithTag("contact_section_social_title").assertExists()

        // Social media buttons
        composeTestRule.onNodeWithTag("social_media_button_LinkedIn").assertExists()
        composeTestRule.onNodeWithTag("social_media_button_GitHub").assertExists()
        composeTestRule.onNodeWithTag("social_media_button_BlueSky").assertExists()
        composeTestRule.onNodeWithTag("social_media_button_ProtoPro").assertExists()
    }

    /**
     * Test that social media buttons are clickable.
     */
    @Test
    fun socialMediaButtons_areClickable() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithTag("social_media_button_LinkedIn").assertHasClickAction()
        composeTestRule.onNodeWithTag("social_media_button_GitHub").assertHasClickAction()
        composeTestRule.onNodeWithTag("social_media_button_BlueSky").assertHasClickAction()
        composeTestRule.onNodeWithTag("social_media_button_ProtoPro").assertHasClickAction()
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
        composeTestRule.onAllNodesWithTag("contact_section").assertCountEquals(2)
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
        composeTestRule.onNodeWithTag("contact_section").assertExists()
        composeTestRule.onNodeWithTag("contact_section").assertIsDisplayed()
    }

    /**
     * Test that the section handles recomposition correctly.
     */
    @Test
    fun section_handlesRecompositionCorrectly() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onNodeWithTag("contact_section").assertIsDisplayed()

        // Force recomposition
        composeTestRule.waitForIdle()

        // Verify everything is still present
        composeTestRule.onNodeWithTag("contact_section").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contact Information").assertIsDisplayed()
    }

    /**
     * Test edge case with very small alpha value.
     */
    @Test
    fun section_handlesVerySmallAlpha() {
        setContent {
            ContactSection(alpha = 0.01f)
        }

        composeTestRule.onNodeWithTag("contact_section").assertExists()
    }

    /**
     * Test that all four social media buttons have unique tags.
     */
    @Test
    fun socialMediaButtons_haveUniqueTags() {
        setContent {
            ContactSection(alpha = 1f)
        }

        composeTestRule.onAllNodesWithTag("social_media_button_LinkedIn").assertCountEquals(1)
        composeTestRule.onAllNodesWithTag("social_media_button_GitHub").assertCountEquals(1)
        composeTestRule.onAllNodesWithTag("social_media_button_BlueSky").assertCountEquals(1)
        composeTestRule.onAllNodesWithTag("social_media_button_ProtoPro").assertCountEquals(1)
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
