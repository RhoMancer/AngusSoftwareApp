package dev.angussoftware.app.blog

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class NetworkClientTest {
    @Test
    fun defaultNetworkClientReturnsFetcherValue() =
        runTest {
            val expected = "OK: content"
            val client = DefaultNetworkClient(fetcher = { _ -> expected })

            val result = client.fetchUrlText("https://example.com/test")

            assertEquals(expected, result, "DefaultNetworkClient should return the value produced by the fetcher")
        }

    @Test
    fun defaultNetworkClientPassesUrlToFetcher() =
        runTest {
            val passedUrls = mutableListOf<String>()
            val client =
                DefaultNetworkClient(fetcher = { url ->
                    passedUrls += url
                    "ignored"
                })

            val url = "https://example.com/some-feed.xml"
            client.fetchUrlText(url)

            assertEquals(listOf(url), passedUrls, "Fetcher should receive the same URL that was passed to NetworkClient")
        }

    @Test
    fun defaultNetworkClientPropagatesExceptions() =
        runTest {
            val error = IllegalStateException("boom")
            val client = DefaultNetworkClient(fetcher = { _ -> throw error })

            val thrown =
                assertFailsWith<IllegalStateException> {
                    client.fetchUrlText("https://example.com")
                }
            assertEquals("boom", thrown.message)
        }

    @Test
    fun customNetworkClientImplementationWorks() =
        runTest {
            val custom =
                object : NetworkClient {
                    override suspend fun fetchUrlText(url: String): String = "X:" + url.substringAfterLast('/')
                }
            val result = custom.fetchUrlText("https://example.com/abc")
            assertEquals("X:abc", result)
        }
}
