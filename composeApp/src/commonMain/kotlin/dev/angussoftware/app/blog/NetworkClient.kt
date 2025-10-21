package dev.angussoftware.app.blog

internal interface NetworkClient {
    suspend fun fetchUrlText(url: String): String
}

internal class DefaultNetworkClient(
    private val fetcher: suspend (String) -> String = { url -> dev.angussoftware.app.blog.fetchUrlText(url) }
) : NetworkClient {
    override suspend fun fetchUrlText(url: String): String {
        return fetcher(url)
    }
}