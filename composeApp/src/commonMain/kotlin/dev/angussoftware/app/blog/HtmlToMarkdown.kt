package dev.angussoftware.app.blog

/**
 * Converts simple HTML to Markdown so the multiplatform-markdown-renderer can render it.
 * Handles headings, bold, italic, code, links, images, lists, paragraphs,
 * blockquotes, and preformatted blocks.
 */
internal object HtmlToMarkdown {

    internal fun convert(html: String): String {
        var text = html

        // Preformatted blocks: <pre>...</pre> — protect from further processing
        val preBlocks = mutableListOf<String>()
        text = PRE_REGEX.replace(text) { match ->
            val code = match.groupValues[1].trim()
            val index = preBlocks.size
            preBlocks += "```\n$code\n```"
            "\u0000PRE$index\u0000"
        }

        // Decode entities
        text = text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")

        // Self-closing / void elements → newlines
        text = text.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        text = text.replace(Regex("<hr\\s*/?>", RegexOption.IGNORE_CASE), "\n---\n")

        // Headings
        for (level in 6 downTo 1) {
            text = Regex("<h$level[^>]*>([\\s\\S]*?)</h$level>", RegexOption.IGNORE_CASE)
                .replace(text) { match ->
                    val prefix = "#".repeat(level)
                    "\n$prefix ${match.groupValues[1].trim()}\n"
                }
        }

        // Blockquotes
        text = Regex("<blockquote[^>]*>([\\s\\S]*?)</blockquote>", RegexOption.IGNORE_CASE)
            .replace(text) { match ->
                val inner = match.groupValues[1].trim()
                inner.lineSequence().joinToString("\n") { line -> "> $line" }
            }

        // Bold / strong
        text = text.replace(Regex("<(?:strong|b)>([\\s\\S]*?)</(?:strong|b)>", RegexOption.IGNORE_CASE)) {
            "**${it.groupValues[1].trim()}**"
        }

        // Italic / em
        text = text.replace(Regex("<(?:em|i)>([\\s\\S]*?)</(?:em|i)>", RegexOption.IGNORE_CASE)) {
            "*${it.groupValues[1].trim()}*"
        }

        // Inline code (before links so <code> inside <a> works)
        text = text.replace(Regex("<code>([\\s\\S]*?)</code>", RegexOption.IGNORE_CASE)) {
            "`${it.groupValues[1]}`"
        }

        // Links
        text = text.replace(Regex("<a[^>]*href=\"([^\"]+)\"[^>]*>([\\s\\S]*?)</a>", RegexOption.IGNORE_CASE)) {
            "[${it.groupValues[2]}](${it.groupValues[1]})"
        }

        // Images
        text = text.replace(Regex("<img[^>]*src=\"([^\"]+)\"[^>]*alt=\"([^\"]*)\"[^>]*/?>", RegexOption.IGNORE_CASE)) {
            "![${it.groupValues[2]}](${it.groupValues[1]})"
        }
        text = text.replace(Regex("<img[^>]*src=\"([^\"]+)\"[^>]*/?>", RegexOption.IGNORE_CASE)) {
            "![](${it.groupValues[1]})"
        }

        // Unordered lists
        text = Regex("<ul[^>]*>([\\s\\S]*?)</ul>", RegexOption.IGNORE_CASE).replace(text) { match ->
            val items = Regex("<li[^>]*>([\\s\\S]*?)</li>", RegexOption.IGNORE_CASE)
                .findAll(match.groupValues[1])
                .joinToString("\n") { "- ${it.groupValues[1].trim()}" }
            "\n$items\n"
        }

        // Ordered lists
        text = Regex("<ol[^>]*>([\\s\\S]*?)</ol>", RegexOption.IGNORE_CASE).replace(text) { match ->
            val items = Regex("<li[^>]*>([\\s\\S]*?)</li>", RegexOption.IGNORE_CASE)
                .findAll(match.groupValues[1])
                .mapIndexed { idx, match -> "${idx + 1}. ${match.groupValues[1].trim()}" }
                .joinToString("\n")
            "\n$items\n"
        }

        // Remove remaining tags
        text = text.replace(Regex("<[^>]+>"), "")

        // Restore pre blocks
        preBlocks.forEachIndexed { index, block ->
            text = text.replace("\u0000PRE$index\u0000", block)
        }

        // Collapse whitespace
        text = text.replace(Regex("\n{3,}"), "\n\n")

        return text.trim()
    }

    private val PRE_REGEX = Regex("<pre[^>]*>([\\s\\S]*?)</pre>", RegexOption.IGNORE_CASE)
}
