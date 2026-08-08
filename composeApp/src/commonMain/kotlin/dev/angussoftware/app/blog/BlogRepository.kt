package dev.angussoftware.app.blog

internal open class BlogRepository(
    private val feedUrl: String,
    private val networkClient: NetworkClient = DefaultNetworkClient(),
) {
    internal open suspend fun fetchPosts(limit: Int = 20): List<BlogPost> {
        val xml = networkClient.fetchUrlText(feedUrl)
        return RssParser.parse(xml, limit)
    }
}
