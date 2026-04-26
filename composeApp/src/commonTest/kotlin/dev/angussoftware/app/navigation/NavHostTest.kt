package dev.angussoftware.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class NavHostTest {
    @Test
    fun constantValuesAreCorrect() {
        assertEquals("loading", BLOG_POST_LOADING_ID)
        assertEquals("error", BLOG_POST_ERROR_ID)
        assertEquals("https://rhomancer.github.io/angus-blog-content/rss.xml", RSS_FEED_URL)
    }

    // Post ID parsing tests - Critical functionality
    @Test
    fun parsePostIdWithValidRoute() {
        val route = "BlogPost/post1"
        val result = parsePostId(route)
        assertEquals("post1", result, "Should parse valid post ID correctly")
    }

    @Test
    fun parsePostIdWithUrlAsId() {
        val route = "BlogPost/https://example.com/post1"
        val result = parsePostId(route)
        assertEquals("https://example.com/post1", result, "Should parse URL-based post ID")
    }

    @Test
    fun parsePostIdWithEmptySegment() {
        val route = "BlogPost/"
        val result = parsePostId(route)
        assertEquals("", result, "Should return empty string for empty ID segment")
    }

    @Test
    fun parsePostIdWithNullRoute() {
        val result = parsePostId(null)
        assertEquals("", result, "Should return empty string for null route")
    }

    @Test
    fun parsePostIdWithEmptyString() {
        val result = parsePostId("")
        assertEquals("", result, "Should return empty string for empty string")
    }

    @Test
    fun parsePostIdWithNoSlash() {
        val route = "BlogPost"
        val result = parsePostId(route)
        assertEquals("BlogPost", result, "Should return whole string when no slash present")
    }

    @Test
    fun parsePostIdWithMultipleSlashes() {
        val route = "BlogPost/Category/42"
        val result = parsePostId(route)
        assertEquals("Category/42", result, "Should extract everything after BlogPost/")
    }

    @Test
    fun parsePostIdWithSpecialCharacters() {
        val route = "BlogPost/post-123_test"
        val result = parsePostId(route)
        assertEquals("post-123_test", result, "Should preserve special characters in ID")
    }

    @Test
    fun parsePostIdWithEncodedUrl() {
        val route = "BlogPost/https%3A%2F%2Fexample.com%2Fpost1"
        val result = parsePostId(route)
        assertEquals("https%3A%2F%2Fexample.com%2Fpost1", result, "Should handle URL-encoded IDs")
    }

    // BlogPost creation tests - Loading state
    @Test
    fun createLoadingBlogPostWithTitle() {
        val title = "Loading..."
        val result = createLoadingBlogPost(title)

        assertEquals(BLOG_POST_LOADING_ID, result.id)
        assertEquals(title, result.title)
        assertEquals("", result.url)
        assertEquals(null, result.pubDate)
        assertEquals(null, result.summary)
        assertEquals(null, result.imageUrl)
        assertEquals(null, result.content)
    }

    @Test
    fun createLoadingBlogPostWithEmptyTitle() {
        val title = ""
        val result = createLoadingBlogPost(title)

        assertEquals(BLOG_POST_LOADING_ID, result.id)
        assertEquals("", result.title)
        assertEquals("", result.url)
    }

    @Test
    fun createLoadingBlogPostWithLongTitle() {
        val title =
            "This is a very long loading title that might be used in some scenarios where detailed loading information is needed"
        val result = createLoadingBlogPost(title)

        assertEquals(BLOG_POST_LOADING_ID, result.id)
        assertEquals(title, result.title)
    }

    @Test
    fun createLoadingBlogPostWithSpecialCharacters() {
        val title = "Loading... 🔄 Please wait! @#$%^&*()"
        val result = createLoadingBlogPost(title)

        assertEquals(BLOG_POST_LOADING_ID, result.id)
        assertEquals(title, result.title)
    }

    // BlogPost creation tests - Error state
    @Test
    fun createErrorBlogPostWithTitle() {
        val title = "Post not found"
        val result = createErrorBlogPost(title)

        assertEquals(BLOG_POST_ERROR_ID, result.id)
        assertEquals(title, result.title)
        assertEquals("", result.url)
        assertEquals(null, result.pubDate)
        assertEquals(null, result.summary)
        assertEquals(null, result.imageUrl)
        assertEquals(null, result.content)
    }

    @Test
    fun createErrorBlogPostWithEmptyTitle() {
        val title = ""
        val result = createErrorBlogPost(title)

        assertEquals(BLOG_POST_ERROR_ID, result.id)
        assertEquals("", result.title)
        assertEquals("", result.url)
    }

    @Test
    fun createErrorBlogPostWithLongTitle() {
        val title =
            "An unexpected error occurred while trying to load the blog post. Please check your connection and try again later."
        val result = createErrorBlogPost(title)

        assertEquals(BLOG_POST_ERROR_ID, result.id)
        assertEquals(title, result.title)
    }

    @Test
    fun createErrorBlogPostWithSpecialCharacters() {
        val title = "Error 404 - Not Found! ❌ @#$%"
        val result = createErrorBlogPost(title)

        assertEquals(BLOG_POST_ERROR_ID, result.id)
        assertEquals(title, result.title)
    }

    // Edge case tests for BlogPost creation
    @Test
    fun createdBlogPostsAreNotNull() {
        val loadingPost = createLoadingBlogPost("Loading")
        val errorPost = createErrorBlogPost("Error")

        assertNotNull(loadingPost, "Loading BlogPost should not be null")
        assertNotNull(errorPost, "Error BlogPost should not be null")
    }

    @Test
    fun createdBlogPostsHaveCorrectDefaults() {
        val loadingPost = createLoadingBlogPost("Test")
        val errorPost = createErrorBlogPost("Test")

        // Both should have empty URL and null optional fields
        assertEquals("", loadingPost.url)
        assertEquals("", errorPost.url)
        assertEquals(null, loadingPost.pubDate)
        assertEquals(null, errorPost.pubDate)
        assertEquals(null, loadingPost.summary)
        assertEquals(null, errorPost.summary)
        assertEquals(null, loadingPost.imageUrl)
        assertEquals(null, errorPost.imageUrl)
        assertEquals(null, loadingPost.content)
        assertEquals(null, errorPost.content)
    }

    @Test
    fun createdBlogPostsHaveUniqueIds() {
        val loadingPost = createLoadingBlogPost("Loading")
        val errorPost = createErrorBlogPost("Error")

        assertTrue(loadingPost.id != errorPost.id, "Loading and error posts should have different IDs")
        assertEquals(BLOG_POST_LOADING_ID, loadingPost.id)
        assertEquals(BLOG_POST_ERROR_ID, errorPost.id)
    }
}
