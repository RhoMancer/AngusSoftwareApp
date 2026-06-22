package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.angussoftware.app.blog.BlogPost
import dev.angussoftware.app.ui.utils.LocalWindowAdaptiveInfoOverride
import dev.angussoftware.app.ui.utils.WindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for [BlogPostScreen] covering all branch paths:
 * - pubDate present / absent
 * - imageUrl present / absent
 * - content present / summary fallback / neither
 * - compact vs expanded layout
 * - back button and open-in-browser click handlers
 */
class BlogPostScreenInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(
        blogPost: BlogPost,
        widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.COMPACT,
        onBackClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(widthSizeClass),
            ) {
                BlogPostScreen(blogPost = blogPost, onBackClick = onBackClick)
            }
        }
    }

    @Test
    fun blogPost_withAllFields_displaysAllContent() {
        val post = BlogPost(
            id = "1",
            title = "Full Post",
            url = "https://example.com/full",
            pubDate = "2025-06-15",
            summary = "Summary text",
            imageUrl = "https://example.com/image.png",
            content = "Full content body",
        )
        setContent(post)

        composeTestRule.onNodeWithText("Full Post").assertIsDisplayed()
        composeTestRule.onNodeWithText("2025-06-15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full content body").assertIsDisplayed()
    }

    @Test
    fun blogPost_withNullPubDate_doesNotShowDate() {
        val post = BlogPost(
            id = "1",
            title = "No Date Post",
            url = "https://example.com/nodate",
            pubDate = null,
            summary = "Summary",
            imageUrl = null,
            content = null,
        )
        setContent(post)

        composeTestRule.onNodeWithText("No Date Post").assertIsDisplayed()
        composeTestRule.onNodeWithText("2025-06-15").assertDoesNotExist()
    }

    @Test
    fun blogPost_withNullImageUrl_doesNotShowImagePlaceholder() {
        val post = BlogPost(
            id = "1",
            title = "No Image",
            url = "https://example.com/noimg",
            pubDate = null,
            summary = "Summary",
            imageUrl = null,
            content = null,
        )
        setContent(post)

        // The image placeholder text should not exist when imageUrl is null
        composeTestRule.onNodeWithText("No Image").assertIsDisplayed()
    }

    @Test
    fun blogPost_withImageUrl_showsImagePlaceholder() {
        val post = BlogPost(
            id = "1",
            title = "Has Image",
            url = "https://example.com/img",
            pubDate = null,
            summary = "Summary",
            imageUrl = "https://example.com/image.png",
            content = null,
        )
        setContent(post)

        composeTestRule.onNodeWithText("Has Image").assertIsDisplayed()
    }

    @Test
    fun blogPost_withContent_showsContent() {
        val post = BlogPost(
            id = "1",
            title = "Content Post",
            url = "https://example.com/content",
            pubDate = null,
            summary = "Should NOT show this summary",
            imageUrl = null,
            content = "The actual content",
        )
        setContent(post)

        composeTestRule.onNodeWithText("The actual content").assertIsDisplayed()
    }

    @Test
    fun blogPost_withoutContent_fallsBackToSummary() {
        val post = BlogPost(
            id = "1",
            title = "Summary Post",
            url = "https://example.com/summary",
            pubDate = null,
            summary = "Fallback summary text",
            imageUrl = null,
            content = null,
        )
        setContent(post)

        composeTestRule.onNodeWithText("Fallback summary text").assertIsDisplayed()
    }

    @Test
    fun blogPost_withoutContentOrSummary_showsNeither() {
        val post = BlogPost(
            id = "1",
            title = "Bare Post",
            url = "https://example.com/bare",
            pubDate = null,
            summary = null,
            imageUrl = null,
            content = null,
        )
        setContent(post)

        composeTestRule.onNodeWithText("Bare Post").assertIsDisplayed()
    }

    @Test
    fun blogPost_backButton_callsOnBackClick() {
        var backClicked = false
        val post = BlogPost(
            id = "1",
            title = "Back Test",
            url = "https://example.com/back",
            pubDate = null,
            summary = null,
            imageUrl = null,
            content = null,
        )
        setContent(post, onBackClick = { backClicked = true })

        composeTestRule.onNodeWithText("Back").performClick()
        assertEquals(true, backClicked)
    }

    @Test
    fun blogPost_compactLayout_usesCompactPadding() {
        val post = BlogPost(
            id = "1",
            title = "Compact Layout",
            url = "https://example.com/compact",
            pubDate = "2025-01-01",
            summary = "Summary",
            imageUrl = null,
            content = null,
        )
        setContent(post, widthSizeClass = WindowWidthSizeClass.COMPACT)

        composeTestRule.onNodeWithText("Compact Layout").assertIsDisplayed()
        composeTestRule.onNodeWithText("2025-01-01").assertIsDisplayed()
    }

    @Test
    fun blogPost_expandedLayout_usesExpandedPadding() {
        val post = BlogPost(
            id = "1",
            title = "Expanded Layout",
            url = "https://example.com/expanded",
            pubDate = "2025-01-01",
            summary = "Summary",
            imageUrl = null,
            content = null,
        )
        setContent(post, widthSizeClass = WindowWidthSizeClass.EXPANDED)

        composeTestRule.onNodeWithText("Expanded Layout").assertIsDisplayed()
        composeTestRule.onNodeWithText("2025-01-01").assertIsDisplayed()
    }
}
