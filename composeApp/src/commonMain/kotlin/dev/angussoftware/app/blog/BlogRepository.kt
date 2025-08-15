package dev.angussoftware.app.blog

class BlogRepository(
    private val feedUrl: String
) {
    suspend fun fetchPosts(limit: Int = 20): List<BlogPost> {
        return try {
            val xml = fetchUrlText(feedUrl)
            RssParser.parse(xml, limit)
        } catch (t: Throwable) {
            emptyList()
        }
    }
}