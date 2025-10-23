package dev.angussoftware.app.blog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

const val CONNECT_TIMEOUT = 10000
const val READ_TIMEOUT = 10000

internal actual suspend fun fetchUrlText(url: String): String =
    withContext(Dispatchers.IO) {
        val connection =
            (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
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
