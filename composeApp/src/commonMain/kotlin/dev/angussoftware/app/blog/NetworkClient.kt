package dev.angussoftware.app.blog

internal interface NetworkClient {
    suspend fun fetchUrlText(url: String): String
}

internal class DefaultNetworkClient : NetworkClient {
    override suspend fun fetchUrlText(url: String): String {
        return dev.angussoftware.app.blog.fetchUrlText(url)
    }
}