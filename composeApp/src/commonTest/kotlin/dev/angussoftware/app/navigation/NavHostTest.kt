package dev.angussoftware.app.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigation.compose.rememberNavController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import dev.angussoftware.app.blog.BlogPost

internal class NavHostTest {
    
    @Test
    fun constantValuesAreCorrect() {
        assertEquals("loading", BLOG_POST_LOADING_ID)
        assertEquals("error", BLOG_POST_ERROR_ID)
        assertEquals("https://rhomancer.github.io/angus-blog-content/rss.xml", RSS_FEED_URL)
    }
    
    // Post index parsing tests - Critical functionality
    @Test
    fun parsePostIndexWithValidNumericRoute() {
        val route = "BlogPost/5"
        val result = parsePostIndex(route)
        assertEquals(5, result, "Should parse valid numeric index correctly")
    }
    
    @Test
    fun parsePostIndexWithZeroIndex() {
        val route = "BlogPost/0"
        val result = parsePostIndex(route)
        assertEquals(0, result, "Should parse zero index correctly")
    }
    
    @Test
    fun parsePostIndexWithLargeNumber() {
        val route = "BlogPost/999999"
        val result = parsePostIndex(route)
        assertEquals(999999, result, "Should parse large numbers correctly")
    }
    
    @Test
    fun parsePostIndexWithNegativeNumber() {
        val route = "BlogPost/-5"
        val result = parsePostIndex(route)
        assertEquals(-5, result, "Should parse negative numbers correctly")
    }
    
    @Test
    fun parsePostIndexWithInvalidStringRoute() {
        val route = "BlogPost/invalid"
        val result = parsePostIndex(route)
        assertEquals(0, result, "Should return 0 for non-numeric route")
    }
    
    @Test
    fun parsePostIndexWithEmptyRoute() {
        val route = "BlogPost/"
        val result = parsePostIndex(route)
        assertEquals(0, result, "Should return 0 for empty index")
    }
    
    @Test
    fun parsePostIndexWithNullRoute() {
        val result = parsePostIndex(null)
        assertEquals(0, result, "Should return 0 for null route")
    }
    
    @Test
    fun parsePostIndexWithEmptyString() {
        val result = parsePostIndex("")
        assertEquals(0, result, "Should return 0 for empty string")
    }
    
    @Test
    fun parsePostIndexWithNoSlash() {
        val route = "BlogPost"
        val result = parsePostIndex(route)
        assertEquals(0, result, "Should return 0 when no slash present")
    }
    
    @Test
    fun parsePostIndexWithMultipleSlashes() {
        val route = "Blog/Post/Category/42"
        val result = parsePostIndex(route)
        assertEquals(42, result, "Should extract index after last slash")
    }
    
    @Test
    fun parsePostIndexWithDecimalNumber() {
        val route = "BlogPost/3.14"
        val result = parsePostIndex(route)
        assertEquals(0, result, "Should return 0 for decimal numbers")
    }
    
    @Test
    fun parsePostIndexWithSpecialCharacters() {
        val route = "BlogPost/5@#$"
        val result = parsePostIndex(route)
        assertEquals(0, result, "Should return 0 for strings with special characters")
    }
    
    @Test
    fun parsePostIndexWithWhitespace() {
        val route = "BlogPost/ 5 "
        val result = parsePostIndex(route)
        assertEquals(0, result, "Should return 0 for strings with whitespace")
    }
    
    @Test
    fun parsePostIndexWithVeryLargeNumber() {
        val route = "BlogPost/2147483647" // Int.MAX_VALUE
        val result = parsePostIndex(route)
        assertEquals(2147483647, result, "Should handle Int.MAX_VALUE correctly")
    }
    
    @Test
    fun parsePostIndexWithNumberLargerThanIntMax() {
        val route = "BlogPost/2147483648" // Int.MAX_VALUE + 1
        val result = parsePostIndex(route)
        assertEquals(0, result, "Should return 0 for numbers larger than Int.MAX_VALUE")
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
        val title = "This is a very long loading title that might be used in some scenarios where detailed loading information is needed"
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
        val title = "An unexpected error occurred while trying to load the blog post. Please check your connection and try again later."
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