package dev.angussoftware.app.blog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RssParserTest {
    @Test
    internal fun parsesSingleItemFromRss() {
        val xml =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Test Feed</title>
                <link>https://example.com/</link>
                <description>Test</description>
                <item>
                  <title><![CDATA[Hello World]]></title>
                  <link>https://example.com/post1</link>
                  <guid>post1</guid>
                  <pubDate>Sat, 16 Aug 2025 18:50:00 GMT</pubDate>
                  <description><![CDATA[<p>Summary <b>bold</b>.</p>]]></description>
                  <enclosure url="https://example.com/image.jpg" type="image/jpeg" />
                </item>
              </channel>
            </rss>
            """.trimIndent()

        val posts = RssParser.parse(xml, limit = 20)
        assertEquals(1, posts.size, "Expected one post parsed")

        val post = posts.first()
        assertEquals("post1", post.id)
        assertEquals("Hello World", post.title)
        assertEquals("https://example.com/post1", post.url)
        assertEquals("Sat, 16 Aug 2025 18:50:00 GMT", post.pubDate)
        assertEquals("Summary bold .", post.summary)
        assertEquals("https://example.com/image.jpg", post.imageUrl)
    }

    @Test
    internal fun gracefullyHandlesMissingOptionalFields() {
        val xml =
            """
            <rss version="2.0">
              <channel>
                <item>
                  <title>Untitled if blank</title>
                  <link>https://example.com/post2</link>
                </item>
              </channel>
            </rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals(1, posts.size)
        val post = posts.first()
        assertEquals("https://example.com/post2", post.url)
        assertTrue(post.title.isNotBlank())
    }

    // === content:encoded parsing ===

    @Test
    internal fun parsesContentEncoded() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>Full Post</title>
                <link>https://example.com/full</link>
                <content:encoded><![CDATA[<p>Full body text with <a href="#">link</a></p>]]></content:encoded>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals(1, posts.size)
        val post = posts.first()
        assertEquals("Full body text with link", post.content)
    }

    @Test
    internal fun fallsBackToContentTagWhenNoContentEncoded() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>Fallback</title>
                <link>https://example.com/fb</link>
                <content>Plain content here</content>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("Plain content here", posts.first().content)
    }

    @Test
    internal fun derivesSummaryFromContentWhenNoDescription() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>No Desc</title>
                <link>https://example.com/nodesc</link>
                <content:encoded><![CDATA[<p>Content used as summary</p>]]></content:encoded>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("Content used as summary", posts.first().summary)
    }

    // === Atom-style link href fallback ===

    @Test
    internal fun fallsBackToHttpGuidAsUrlWhenNoLink() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>HTTP Guid</title>
                <guid>http://example.com/insecure-guid</guid>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals(1, posts.size)
        assertEquals("http://example.com/insecure-guid", posts.first().url)
    }

    @Test
    internal fun fallsBackToAtomLinkHref() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>Atom Link</title>
                <link href="https://example.com/atom-post" rel="alternate" />
                <guid>https://example.com/atom-post</guid>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals(1, posts.size)
        assertEquals("https://example.com/atom-post", posts.first().url)
    }

    // === guid as URL fallback ===

    @Test
    internal fun fallsBackToGuidAsUrlWhenNoLink() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>Guid Fallback</title>
                <guid>https://example.com/guid-only</guid>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals(1, posts.size)
        assertEquals("https://example.com/guid-only", posts.first().url)
        assertEquals("https://example.com/guid-only", posts.first().id)
    }

    @Test
    internal fun skipsItemsWithoutLinkOrGuidUrl() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>No Link</title>
                <guid>non-url-guid</guid>
              </item>
              <item>
                <title>Has Link</title>
                <link>https://example.com/has-link</link>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals(1, posts.size)
        assertEquals("Has Link", posts.first().title)
    }

    // === media URL extraction ===

    @Test
    internal fun extractsMediaContentUrl() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>Media Content</title>
                <link>https://example.com/mc</link>
                <media:content url="https://example.com/media.jpg" medium="image" />
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("https://example.com/media.jpg", posts.first().imageUrl)
    }

    @Test
    internal fun extractsMediaThumbnailUrl() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>Media Thumb</title>
                <link>https://example.com/mt</link>
                <media:thumbnail url="https://example.com/thumb.jpg" />
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("https://example.com/thumb.jpg", posts.first().imageUrl)
    }

    @Test
    internal fun prefersEnclosureOverMediaUrl() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>Both Images</title>
                <link>https://example.com/both</link>
                <enclosure url="https://example.com/enclosure.jpg" type="image/jpeg" />
                <media:content url="https://example.com/media.jpg" />
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("https://example.com/enclosure.jpg", posts.first().imageUrl)
    }

    // === XML entity decoding ===

    @Test
    internal fun decodesXmlEntitiesInTitle() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>Tom &amp; Jerry &lt;show&gt; &quot;fun&quot;</title>
                <link>https://example.com/entities</link>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("Tom & Jerry <show> \"fun\"", posts.first().title)
    }

    @Test
    internal fun decodesAposEntity() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>It&amp;apos;s a test</title>
                <link>https://example.com/apos</link>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("It's a test", posts.first().title)
    }

    // === HTML stripping ===

    @Test
    internal fun stripsHtmlAndNbspFromDescription() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>HTML Test</title>
                <link>https://example.com/html</link>
                <description><![CDATA[<p>Hello&nbsp;world <span>with</span> <b>tags</b></p>]]></description>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("Hello world with tags", posts.first().summary)
    }

    // === Summary truncation ===

    @Test
    internal fun truncatesSummaryAt400Chars() {
        val longText = "A".repeat(500)
        val xml =
            """
            <rss><channel>
              <item>
                <title>Long Summary</title>
                <link>https://example.com/long</link>
                <description><![CDATA[$longText]]></description>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals(400, posts.first().summary?.length)
    }

    // === Blank title → Untitled ===

    @Test
    internal fun defaultsBlankTitleToUntitled() {
        val xml =
            """
            <rss><channel>
              <item>
                <title></title>
                <link>https://example.com/blank-title</link>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals("Untitled", posts.first().title)
    }

    // === Multiple items + limit ===

    @Test
    internal fun respectsLimitParameter() {
        val items = (1..5).joinToString("") { i ->
            """
            <item>
              <title>Post $i</title>
              <link>https://example.com/post-$i</link>
            </item>
            """.trimIndent()
        }
        val xml = "<rss><channel>$items</channel></rss>"

        val posts = RssParser.parse(xml, limit = 3)
        assertEquals(3, posts.size)
        assertEquals("Post 1", posts[0].title)
        assertEquals("Post 3", posts[2].title)
    }

    @Test
    internal fun parsesMultipleItemsWithDifferentFields() {
        val xml =
            """
            <rss><channel>
              <item>
                <title>With Image</title>
                <link>https://example.com/with-img</link>
                <enclosure url="https://example.com/img1.jpg" type="image/jpeg" />
              </item>
              <item>
                <title>Without Image</title>
                <link>https://example.com/no-img</link>
              </item>
            </channel></rss>
            """.trimIndent()

        val posts = RssParser.parse(xml)
        assertEquals(2, posts.size)
        assertEquals("https://example.com/img1.jpg", posts[0].imageUrl)
        assertNull(posts[1].imageUrl)
    }

    // === Empty / edge cases ===

    @Test
    internal fun returnsEmptyListForEmptyXml() {
        val posts = RssParser.parse("")
        assertTrue(posts.isEmpty())
    }

    @Test
    internal fun returnsEmptyListForNoItems() {
        val xml = "<rss><channel><title>Empty Feed</title></channel></rss>"
        val posts = RssParser.parse(xml)
        assertTrue(posts.isEmpty())
    }
}
