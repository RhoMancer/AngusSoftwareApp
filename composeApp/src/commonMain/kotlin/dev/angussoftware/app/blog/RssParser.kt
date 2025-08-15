package dev.angussoftware.app.blog

internal object RssParser {
    fun parse(xml: String, limit: Int = 20): List<BlogPost> {
        val items = ITEM_REGEX.findAll(xml).map { it.value }.take(limit)
        val posts = mutableListOf<BlogPost>()
        for (itemXml in items) {
            val link = extractTag(itemXml, "link")?.trim().orEmpty()
            if (link.isBlank()) continue
            val guid = extractTag(itemXml, "guid")?.trim()
            val title = extractTag(itemXml, "title")?.let { decodeXmlEntities(stripCdata(it)).trim() }.orEmpty()
            val pubDate = extractTag(itemXml, "pubDate")?.let { stripCdata(it).trim() }
            val descriptionRaw = extractTag(itemXml, "description")
            val description = descriptionRaw?.let { stripHtml(decodeXmlEntities(stripCdata(it))).trim() }?.take(400)
            val imageUrl = extractEnclosureUrl(itemXml) ?: extractMediaUrl(itemXml)

            posts += BlogPost(
                id = (guid ?: link),
                title = if (title.isBlank()) "Untitled" else title,
                url = link,
                pubDate = pubDate,
                summary = description,
                imageUrl = imageUrl
            )
        }
        return posts
    }

    private fun extractTag(xml: String, tag: String): String? {
        val regex = Regex("<${'$'}tag(?:[^>]*)>([\\s\\S]*?)</${'$'}tag>", RegexOption.IGNORE_CASE)
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