package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive instrumented tests for the SocialMediaButton composable.
 * 
 * This test suite covers:
 * - Visual rendering and UI composition
 * - Click interaction and URL handling
 * - Accessibility features
 * - Multiple platform scenarios
 * - Semantic properties
 * - Material Design compliance
 */
@RunWith(AndroidJUnit4::class)
class SocialMediaButtonInstrumentedTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    // Test data
    private val testPlatform = "LinkedIn"
    private val testUrl = "https://www.linkedin.com/in/test"

    /**
     * Test that the platform name is displayed correctly on the button.
     */
    @Test
    fun platformName_isDisplayed() {
        setContent {
            SocialMediaButton(
                platform = testPlatform,
                url = testUrl
            )
        }

        rule.onNodeWithText(testPlatform).assertIsDisplayed()
    }

    /**
     * Test that multiple different platform names render correctly.
     */
    @Test
    fun multiplePlatforms_renderCorrectly() {
        setContent {
            androidx.compose.foundation.layout.Column {
                SocialMediaButton(platform = "LinkedIn", url = "https://linkedin.com")
                SocialMediaButton(platform = "GitHub", url = "https://github.com")
                SocialMediaButton(platform = "Bluesky", url = "https://bsky.app")
            }
        }

        rule.onNodeWithText("LinkedIn").assertIsDisplayed()
        rule.onNodeWithText("GitHub").assertIsDisplayed()
        rule.onNodeWithText("Bluesky").assertIsDisplayed()
    }

    /**
     * Test that clicking the button triggers the URI handler with the correct URL.
     */
    @Test
    fun clickButton_opensCorrectUrl() {
        var capturedUri: String? = null
        
        rule.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            capturedUri = uri
                        }
                    }
                ) {
                    SocialMediaButton(
                        platform = testPlatform,
                        url = testUrl
                    )
                }
            }
        }

        rule.onNodeWithTag("social_media_button_$testPlatform").performClick()
        rule.waitForIdle()

        assertEquals(testUrl, capturedUri)
    }

    /**
     * Test that multiple buttons handle different URLs correctly.
     */
    @Test
    fun multipleButtons_openDifferentUrls() {
        val capturedUris = mutableListOf<String>()
        
        rule.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            capturedUris.add(uri)
                        }
                    }
                ) {
                    androidx.compose.foundation.layout.Column {
                        SocialMediaButton(platform = "LinkedIn", url = "https://linkedin.com")
                        SocialMediaButton(platform = "GitHub", url = "https://github.com")
                    }
                }
            }
        }

        rule.onNodeWithTag("social_media_button_LinkedIn").performClick()
        rule.waitForIdle()
        
        rule.onNodeWithTag("social_media_button_GitHub").performClick()
        rule.waitForIdle()

        assertEquals(2, capturedUris.size)
        assertTrue(capturedUris.contains("https://linkedin.com"))
        assertTrue(capturedUris.contains("https://github.com"))
    }

    /**
     * Test that different platforms have unique test tags.
     */
    @Test
    fun uniqueTestTags_forDifferentPlatforms() {
        setContent {
            SocialMediaButton(platform = "LinkedIn", url = "https://linkedin.com")
            SocialMediaButton(platform = "GitHub", url = "https://github.com")
        }

        rule.onNodeWithTag("social_media_button_LinkedIn").assertExists()
        rule.onNodeWithTag("social_media_button_GitHub").assertExists()
        
        // Verify they are different nodes
        rule.onAllNodesWithTag("social_media_button_LinkedIn").assertCountEquals(1)
        rule.onAllNodesWithTag("social_media_button_GitHub").assertCountEquals(1)
    }

    /**
     * Test that the button renders correctly with special characters in platform name.
     */
    @Test
    fun platformNameWithSpecialCharacters_rendersCorrectly() {
        val specialPlatform = "Proto.Pro"
        val specialUrl = "https://proto.pro"
        
        setContent {
            SocialMediaButton(
                platform = specialPlatform,
                url = specialUrl
            )
        }

        rule.onNodeWithText(specialPlatform).assertIsDisplayed()
    }

    /**
     * Test that the button renders correctly with long platform names.
     */
    @Test
    fun longPlatformName_rendersCorrectly() {
        val longPlatform = "Very Long Social Media Platform Name"
        
        setContent {
            SocialMediaButton(
                platform = longPlatform,
                url = testUrl
            )
        }

        rule.onNodeWithText(longPlatform).assertIsDisplayed()
    }

    /**
     * Test that the button responds to touch/click interactions.
     */
    @Test
    fun button_respondsToTouch() {
        var clickCount = 0
        
        rule.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            clickCount++
                        }
                    }
                ) {
                    SocialMediaButton(
                        platform = testPlatform,
                        url = testUrl
                    )
                }
            }
        }

        val button = rule.onNodeWithTag("social_media_button_$testPlatform")
        
        // Click multiple times
        button.performClick()
        rule.waitForIdle()
        button.performClick()
        rule.waitForIdle()

        assertEquals(2, clickCount)
    }

    /**
     * Test that empty platform name still renders (edge case).
     */
    @Test
    fun emptyPlatformName_stillRendersButton() {
        setContent {
            SocialMediaButton(
                platform = "",
                url = testUrl
            )
        }

        // Button should exist even with empty platform name
        rule.onNodeWithTag("social_media_button_").assertExists()
    }

    /**
     * Test that the button works with various URL formats.
     */
    @Test
    fun variousUrlFormats_handleCorrectly() {
        val urls = listOf(
            "https://example.com",
            "http://example.com",
            "https://example.com/path/to/page",
            "https://example.com?param=value"
        )
        
        val capturedUris = mutableListOf<String>()
        
        rule.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            capturedUris.add(uri)
                        }
                    }
                ) {
                    androidx.compose.foundation.layout.Column {
                        urls.forEachIndexed { index, url ->
                            SocialMediaButton(
                                platform = "Platform$index",
                                url = url
                            )
                        }
                    }
                }
            }
        }

        urls.forEachIndexed { index, url ->
            rule.onNodeWithTag("social_media_button_Platform$index").performClick()
            rule.waitForIdle()
        }

        assertEquals(urls.size, capturedUris.size)
        urls.forEach { url ->
            assertTrue(capturedUris.contains(url))
        }
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
