package dev.angussoftware.app.blog

import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * WASM-specific network implementation using external JavaScript function.
 * Uses external JS to avoid WASM type casting issues with Response objects.
 */

// External JavaScript function implemented in network.js
@JsName("fetchUrlTextExternal")
private external fun fetchUrlTextExternal(url: String): Promise<JsAny>

internal actual suspend fun fetchUrlText(url: String): String {
    // Call external JavaScript function to avoid type casting issues
    val jsResult: JsAny = fetchUrlTextExternal(url).await()
    return jsResult.toString()
}
