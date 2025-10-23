package dev.angussoftware.app.blog

internal interface NetworkClient {
    suspend fun fetchUrlText(url: String): String
}

internal class DefaultNetworkClient(
    private val fetcher: suspend (String) -> String = { url ->
        fetchUrlText(url)
    },
) : NetworkClient {
    override suspend fun fetchUrlText(url: String): String = fetcher(url)
}
