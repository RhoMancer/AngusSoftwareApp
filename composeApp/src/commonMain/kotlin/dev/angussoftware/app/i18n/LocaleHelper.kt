package dev.angussoftware.app.i18n

/**
 * Platform-specific locale setup.
 * On WasmJs, reads navigator.language and makes it available to Compose resources.
 */
expect fun setupLocale(languageTag: String)
