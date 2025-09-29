package dev.angussoftware.app.blog

// Expect/actual to fetch URL contents as text across platforms
internal expect suspend fun fetchUrlText(url: String): String
