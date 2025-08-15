package dev.angussoftware.app.blog

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response

internal actual suspend fun fetchUrlText(url: String): String {
    val response: Response = window.fetch(url, RequestInit()).await()
    return response.text().await()
}