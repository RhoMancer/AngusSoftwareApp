package dev.angussoftware.app.i18n

import kotlinx.browser.window

actual fun setupLocale(languageTag: String) {
    // Store locale for Compose resource resolution.
    // Compose Multiplatform on WasmJs reads window.navigator.languages
    // which the browser provides automatically. This is a no-op fallback
    // to log what locale was detected.
    println("[i18n] Detected locale: $languageTag")
}
