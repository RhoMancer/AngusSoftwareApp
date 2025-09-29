package dev.angussoftware.app.blog

internal const val DEFAULT_UNTITLED_POST = "Untitled"

internal object RssParser {
    fun parse(xml: String, limit: Int = 20): List<BlogPost> {
        val items = ITEM_REGEX.findAll(xml).map { it.value }.take(limit)
        val posts = mutableListOf<BlogPost>()
        for (itemXml in items) {
            val guid = extractTag(itemXml, "guid")?.let { stripCdata(it).trim() }
            val link = extractLink(itemXml).ifBlank {
                guid?.takeIf { it.startsWith("http://") || it.startsWith("https://") } ?: ""
            }
            if (link.isBlank()) continue
            val title = extractTag(itemXml, "title")?.let { decodeXmlEntities(stripCdata(it)).trim() }.orEmpty()
            val pubDate = extractTag(itemXml, "pubDate")?.let { stripCdata(it).trim() }

            // Full content: prefer content:encoded, then <content> if present
            val contentRaw = extractTag(itemXml, "content:encoded") ?: extractTag(itemXml, "content")
            val contentPlain = contentRaw?.let { stripHtml(decodeXmlEntities(stripCdata(it))).trim() }

            // Summary: prefer <description>, else derive from content
            val descriptionRaw = extractTag(itemXml, "description")
            val summary = (descriptionRaw ?: contentRaw)
                ?.let { stripHtml(decodeXmlEntities(stripCdata(it))).trim() }
                ?.take(n = 400)

            val imageUrl = extractEnclosureUrl(itemXml) ?: extractMediaUrl(itemXml)

            posts += BlogPost(
                id = (guid ?: link),
                title = title.ifBlank { DEFAULT_UNTITLED_POST },
                url = link,
                pubDate = pubDate,
                summary = summary,
                imageUrl = imageUrl,
                content = contentPlain
            )
        }
        return posts
    }

    private fun extractTag(xml: String, tag: String): String? {
        val pattern = "<${tag}(?:[^>]*)>([\\s\\S]*?)</${tag}>"
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        return regex.find(xml)?.groupValues?.get(1)
    }

    private fun extractLink(xml: String): String {
        // 1) Standard RSS link content
        return extractTag(xml, "link")?.let { stripCdata(it).trim() } ?:
        // 2) Atom-style link href attribute
        extractAtomLinkHref(xml)?.trim() ?: ""
    }

    private fun extractAtomLinkHref(xml: String): String? {
        val regex = Regex("<link[^>]*href=\"([^\"]+)\"[^>]*/?>", RegexOption.IGNORE_CASE)
        return regex.find(xml)?.groupValues?.get(1)
    }

    private fun extractEnclosureUrl(xml: String): String? {
        val regex = Regex("<enclosure[^>]*url=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        return regex.find(xml)?.groupValues?.get(1)
    }

    private fun extractMediaUrl(xml: String): String? {
        val regex1 = Regex("<media:content[^>]*url=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        val regex2 = Regex("<media:thumbnail[^>]*url=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        return regex1.find(xml)?.groupValues?.get(1) ?: regex2.find(xml)?.groupValues?.get(1)
    }

    private fun stripCdata(text: String): String =
        text.removePrefix("<![CDATA[").removeSuffix("]]>")

    internal fun stripHtml(html: String): String =
        html.replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun decodeXmlEntities(text: String): String = text
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")

    private val ITEM_REGEX = Regex("<item[\\s\\S]*?</item>", RegexOption.IGNORE_CASE)
}