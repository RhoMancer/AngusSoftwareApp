package dev.angussoftware.app.blog

internal class BlogRepository(
    private val feedUrl: String,
    private val networkClient: NetworkClient = DefaultNetworkClient(),
) {
    internal suspend fun fetchPosts(limit: Int = 20): List<BlogPost> =
        try {
            val xml = networkClient.fetchUrlText(feedUrl)
            RssParser.parse(xml, limit)
        } catch (t: Throwable) {
            emptyList()
        }
}
