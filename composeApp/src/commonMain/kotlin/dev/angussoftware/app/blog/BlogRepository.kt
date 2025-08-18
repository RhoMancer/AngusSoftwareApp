package dev.angussoftware.app.blog

class BlogRepository(
    private val feedUrl: String
) {
    suspend fun fetchPosts(limit: Int = 20): List<BlogPost> {
        return try {
            println("Fetching initial posts 2")

            val xml = fetchUrlText(feedUrl)
            RssParser.parse(xml, limit)
        } catch (t: Throwable) {
            println(t.toString())

            emptyList()
        }
    }
}