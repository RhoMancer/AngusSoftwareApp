package dev.angussoftware.app.blog

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual suspend fun fetchUrlText(url: String): String = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 10000
        readTimeout = 10000
        requestMethod = "GET"
        setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, text/plain; charset=utf-8")
        setRequestProperty("User-Agent", "AngusSoftwareApp/1.0")
    }
    try {
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line).append('\n')
        }
        reader.close()
        sb.toString()
    } finally {
        connection.disconnect()
    }
}