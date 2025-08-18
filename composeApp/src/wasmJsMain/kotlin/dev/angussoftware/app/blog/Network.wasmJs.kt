package dev.angussoftware.app.blog

import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.JsAny

// External JavaScript function declaration
@JsName("fetchUrlTextExternal")
external fun fetchUrlTextExternal(url: String): Promise<JsAny>

internal actual suspend fun fetchUrlText(url: String): String {
    // Call external JavaScript function to avoid type casting issues
    val jsResult: JsAny = fetchUrlTextExternal(url).await()
    return jsResult.toString()
}