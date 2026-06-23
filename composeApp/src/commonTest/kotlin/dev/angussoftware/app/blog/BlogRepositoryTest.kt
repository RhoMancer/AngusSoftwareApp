package dev.angussoftware.app.blog

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BlogRepositoryTest {
    // Mock NetworkClient that returns successful RSS XML
    private class MockSuccessNetworkClient : NetworkClient {
        override suspend fun fetchUrlText(url: String): String =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Test Feed</title>
                <link>https://example.com/</link>
                <description>Test</description>
                <item>
                  <title><![CDATA[Test Post 1]]></title>
                  <link>https://example.com/post1</link>
                  <guid>post1</guid>
                  <pubDate>Sat, 16 Aug 2025 18:50:00 GMT</pubDate>
                  <description><![CDATA[Test description 1]]></description>
                </item>
                <item>
                  <title><![CDATA[Test Post 2]]></title>
                  <link>https://example.com/post2</link>
                  <guid>post2</guid>
                  <pubDate>Sun, 17 Aug 2025 19:00:00 GMT</pubDate>
                  <description><![CDATA[Test description 2]]></description>
                </item>
              </channel>
            </rss>
            """.trimIndent()
    }

    // Mock NetworkClient that throws an exception
    private class MockErrorNetworkClient : NetworkClient {
        override suspend fun fetchUrlText(url: String): String = throw RuntimeException("Network error")
    }

    @Test
    internal fun constructorInitializesWithFeedUrl() {
        val feedUrl = "https://example.com/feed.xml"
        BlogRepository(feedUrl)

        // Constructor should not throw any exceptions
        assertTrue(true, "Repository should be created successfully")
    }

    @Test
    internal fun constructorInitializesWithNetworkClient() {
        val feedUrl = "https://example.com/feed.xml"
        val mockClient = MockSuccessNetworkClient()
        BlogRepository(feedUrl, mockClient)

        // Constructor should not throw any exceptions
        assertTrue(true, "Repository should be created successfully with NetworkClient")
    }

    @Test
    internal fun fetchPostsWithDefaultLimitReturnsSuccessfulParsedPosts() =
        runTest {
            val feedUrl = "https://example.com/feed.xml"
            val mockClient = MockSuccessNetworkClient()
            val repository = BlogRepository(feedUrl, mockClient)

            val result = repository.fetchPosts()

            assertEquals(2, result.size, "Should return 2 parsed posts")
            assertEquals("Test Post 1", result[0].title)
            assertEquals("https://example.com/post1", result[0].url)
            assertEquals("post1", result[0].id)
            assertEquals("Test Post 2", result[1].title)
            assertEquals("https://example.com/post2", result[1].url)
            assertEquals("post2", result[1].id)
        }

    @Test
    internal fun fetchPostsWithCustomLimitReturnsLimitedPosts() =
        runTest {
            val feedUrl = "https://example.com/feed.xml"
            val mockClient = MockSuccessNetworkClient()
            val repository = BlogRepository(feedUrl, mockClient)

            val result = repository.fetchPosts(limit = 1)

            assertEquals(1, result.size, "Should return only 1 post due to limit")
            assertEquals("Test Post 1", result[0].title)
        }

    @Test
    internal fun fetchPostsHandlesNetworkError() =
        runTest {
            val feedUrl = "https://example.com/feed.xml"
            val mockClient = MockErrorNetworkClient()
            val repository = BlogRepository(feedUrl, mockClient)

            val result = repository.fetchPosts()

            assertEquals(emptyList(), result, "Should return empty list on network error")
        }

    @Test
    internal fun fetchPostsWithZeroLimit() =
        runTest {
            val feedUrl = "https://example.com/feed.xml"
            val mockClient = MockSuccessNetworkClient()
            val repository = BlogRepository(feedUrl, mockClient)

            val result = repository.fetchPosts(limit = 0)

            assertEquals(emptyList(), result, "Should return empty list with zero limit")
        }

    @Test
    internal fun fetchPostsWithNegativeLimit() =
        runTest {
            val feedUrl = "https://example.com/feed.xml"
            val mockClient = MockSuccessNetworkClient()
            val repository = BlogRepository(feedUrl, mockClient)

            val result = repository.fetchPosts(limit = -1)

            assertEquals(emptyList(), result, "Should return empty list with negative limit")
        }

    @Test
    internal fun fetchPostsWithLargeLimit() =
        runTest {
            val feedUrl = "https://example.com/feed.xml"
            val mockClient = MockSuccessNetworkClient()
            val repository = BlogRepository(feedUrl, mockClient)

            val result = repository.fetchPosts(limit = 1000)

            assertEquals(2, result.size, "Should return all available posts (2) even with large limit")
        }

    @Test
    internal fun fetchPostsUsesDefaultNetworkClientWhenNotProvided() =
        runTest {
            val feedUrl = "https://invalid-url-that-will-fail.com/feed.xml"
            val repository = BlogRepository(feedUrl) // Using default NetworkClient

            val result = repository.fetchPosts()

            // This will use the actual DefaultNetworkClient which should handle the error
            assertEquals(emptyList(), result, "Should return empty list when using default client with invalid URL")
        }
}

    // Mock that returns invalid XML to trigger parser exception
    private class MockCorruptNetworkClient : NetworkClient {
        override suspend fun fetchUrlText(url: String): String = "not valid xml {{{"
    }

    @Test
    internal fun fetchPostsHandlesParseException() =
        runTest {
            val feedUrl = "https://example.com/feed.xml"
            val mockClient = MockCorruptNetworkClient()
            val repository = BlogRepository(feedUrl, mockClient)

            val result = repository.fetchPosts()

            assertEquals(emptyList(), result, "Should return empty list on parse error")
        }
