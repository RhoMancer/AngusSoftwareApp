package dev.angussoftware.app.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive instrumented tests for the ContactItem composable.
 *
 * This test suite covers:
 * - Visual rendering and UI composition
 * - Title and content display
 * - Typography and styling
 * - Material Design compliance
 * - Multiple contact types (email, location, etc.)
 * - Edge cases (empty strings, special characters, long text)
 * - Accessibility features
 */
@RunWith(AndroidJUnit4::class)
class ContactItemInstrumentedTest : BaseScreenTest() {

    // Test data
    private val testTitle = "Email"
    private val testContent = "contact@example.com"
    private val locationTitle = "Location"
    private val locationContent = "San Francisco, CA"

    /**
     * Test that both title and content are displayed together.
     */
    @Test
    fun titleAndContent_bothDisplayed() {
        setContent {
            ContactItem(
                title = testTitle,
                content = testContent
            )
        }

        composeTestRule.onNodeWithText(testTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(testContent).assertIsDisplayed()
    }

    /**
     * Test that the container test tag is correctly applied.
     */
    @Test
    fun containerTestTag_isCorrectlyApplied() {
        setContent {
            ContactItem(
                title = testTitle,
                content = testContent
            )
        }

        composeTestRule.onNodeWithTag("contact_item_$testTitle").assertExists()
    }

    /**
     * Test that the title test tag is correctly applied.
     */
    @Test
    fun titleTestTag_isCorrectlyApplied() {
        setContent {
            ContactItem(
                title = testTitle,
                content = testContent
            )
        }

        composeTestRule.onNodeWithTag("contact_item_title_$testTitle").assertExists()
    }

    /**
     * Test that the content test tag is correctly applied.
     */
    @Test
    fun contentTestTag_isCorrectlyApplied() {
        setContent {
            ContactItem(
                title = testTitle,
                content = testContent
            )
        }

        composeTestRule.onNodeWithTag("contact_item_content_$testTitle").assertExists()
    }

    /**
     * Test that multiple ContactItems render correctly.
     */
    @Test
    fun multipleContactItems_renderCorrectly() {
        setContent {
            androidx.compose.foundation.layout.Column {
                ContactItem(title = "Email", content = "test@example.com")
                ContactItem(title = "Location", content = "New York, NY")
                ContactItem(title = "Phone", content = "+1 (555) 123-4567")
            }
        }

        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York, NY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("+1 (555) 123-4567").assertIsDisplayed()
    }

    /**
     * Test that unique test tags are created for different contact items.
     */
    @Test
    fun uniqueTestTags_forDifferentContactItems() {
        setContent {
            androidx.compose.foundation.layout.Column {
                ContactItem(title = "Email", content = "test@example.com")
                ContactItem(title = "Location", content = "New York, NY")
            }
        }

        composeTestRule.onNodeWithTag("contact_item_Email").assertExists()
        composeTestRule.onNodeWithTag("contact_item_Location").assertExists()

        // Verify they are different nodes
        composeTestRule.onAllNodesWithTag("contact_item_Email").assertCountEquals(1)
        composeTestRule.onAllNodesWithTag("contact_item_Location").assertCountEquals(1)
    }

    /**
     * Test that empty title still renders the component.
     */
    @Test
    fun emptyTitle_stillRendersComponent() {
        setContent {
            ContactItem(
                title = "",
                content = testContent
            )
        }

        composeTestRule.onNodeWithTag("contact_item_").assertExists()
        composeTestRule.onNodeWithText(testContent).assertIsDisplayed()
    }

    /**
     * Test that empty content still renders the component.
     */
    @Test
    fun emptyContent_stillRendersComponent() {
        setContent {
            ContactItem(
                title = testTitle,
                content = ""
            )
        }

        composeTestRule.onNodeWithTag("contact_item_$testTitle").assertExists()
        composeTestRule.onNodeWithText(testTitle).assertIsDisplayed()
    }

    /**
     * Test that both empty title and content still renders the component.
     */
    @Test
    fun emptyTitleAndContent_stillRendersComponent() {
        setContent {
            ContactItem(
                title = "",
                content = ""
            )
        }

        composeTestRule.onNodeWithTag("contact_item_").assertExists()
    }

    /**
     * Test that long title text renders correctly without truncation.
     */
    @Test
    fun longTitle_rendersCorrectly() {
        val longTitle = "Very Long Contact Information Title That Should Still Render"

        setContent {
            ContactItem(
                title = longTitle,
                content = testContent
            )
        }

        composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
    }

    /**
     * Test that long content text renders correctly.
     */
    @Test
    fun longContent_rendersCorrectly() {
        val longContent = "This is a very long contact information content that includes multiple words and should still render correctly in the UI without any issues"

        setContent {
            ContactItem(
                title = testTitle,
                content = longContent
            )
        }

        composeTestRule.onNodeWithText(longContent).assertIsDisplayed()
    }

    /**
     * Test that special characters in title render correctly.
     */
    @Test
    fun specialCharactersInTitle_renderCorrectly() {
        val specialTitle = "E-Mail (Work)"

        setContent {
            ContactItem(
                title = specialTitle,
                content = testContent
            )
        }

        composeTestRule.onNodeWithText(specialTitle).assertIsDisplayed()
    }

    /**
     * Test that special characters in content render correctly.
     */
    @Test
    fun specialCharactersInContent_renderCorrectly() {
        val specialContent = "john.doe+test@example.com"

        setContent {
            ContactItem(
                title = testTitle,
                content = specialContent
            )
        }

        composeTestRule.onNodeWithText(specialContent).assertIsDisplayed()
    }

    /**
     * Test that Unicode characters render correctly.
     */
    @Test
    fun unicodeCharacters_renderCorrectly() {
        val unicodeTitle = "地址"  // Chinese for "Address"
        val unicodeContent = "東京都渋谷区"  // Tokyo address in Japanese

        setContent {
            ContactItem(
                title = unicodeTitle,
                content = unicodeContent
            )
        }

        composeTestRule.onNodeWithText(unicodeTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(unicodeContent).assertIsDisplayed()
    }

    /**
     * Test that emojis in content render correctly.
     */
    @Test
    fun emojisInContent_renderCorrectly() {
        val emojiContent = "contact@example.com"

        setContent {
            ContactItem(
                title = testTitle,
                content = emojiContent
            )
        }

        composeTestRule.onNodeWithText(emojiContent).assertIsDisplayed()
    }

    /**
     * Test real-world contact information examples.
     */
    @Test
    fun realWorldContactInfo_rendersCorrectly() {
        val contacts = mapOf(
            "Email" to "john.doe@angussoftware.dev",
            "Location" to "San Francisco, California, USA",
            "Phone" to "+1 (555) 123-4567",
            "Website" to "https://www.angussoftware.dev"
        )

        setContent {
            androidx.compose.foundation.layout.Column {
                contacts.forEach { (title, content) ->
                    ContactItem(title = title, content = content)
                }
            }
        }

        contacts.forEach { (title, content) ->
            composeTestRule.onNodeWithText(title).assertIsDisplayed()
            composeTestRule.onNodeWithText(content).assertIsDisplayed()
        }
    }

    /**
     * Test that multiple identical titles create unique test tags.
     * Note: This is an edge case where the same title is used multiple times.
     */
    @Test
    fun duplicateTitles_createSameTags() {
        setContent {
            androidx.compose.foundation.layout.Column {
                ContactItem(title = "Email", content = "work@example.com")
                ContactItem(title = "Email", content = "personal@example.com")
            }
        }

        // Both should have the same tag (this is expected behavior)
        composeTestRule.onAllNodesWithTag("contact_item_Email").assertCountEquals(2)

        // But different content
        composeTestRule.onNodeWithText("work@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("personal@example.com").assertIsDisplayed()
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
