package dev.angussoftware.app.blog

data class BlogPost(
    val id: String,
    val title: String,
    val url: String,
    val pubDate: String?,
    val summary: String?,
    val imageUrl: String?,
    val content: String?
)