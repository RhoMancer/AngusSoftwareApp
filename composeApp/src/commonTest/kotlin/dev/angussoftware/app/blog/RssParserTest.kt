package dev.angussoftware.app.blog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RssParserTest {
    @Test
    fun parsesSingleItemFromRss() {
        val xml = """
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
    fun gracefullyHandlesMissingOptionalFields() {
        val xml = """
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
}

