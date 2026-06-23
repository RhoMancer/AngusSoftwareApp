package dev.angussoftware.app.navigation

import dev.angussoftware.app.blog.BlogPost

internal const val BLOG_POST_LOADING_ID = "loading"
internal const val BLOG_POST_ERROR_ID = "error"
internal const val RSS_FEED_URL = "https://rhomancer.github.io/angus-blog-content/rss.xml"
internal const val NAV_HOST_TEST_TAG = "NavHost"

/**
 * Parses the post ID from a navigation route string.
 * Extracts the ID (everything after the last slash) from the route path.
 *
 * @param route The navigation route string (e.g., "BlogPost/post1")
 * @return The parsed post ID, or empty string if route is null/blank
 */
internal fun parsePostId(route: String?): String =
    route?.substringAfter("BlogPost/").orEmpty()

/**
 * Creates a BlogPost object for loading state display.
 *
 * @param loadingTitle The localized loading title string
 * @return A BlogPost configured for loading state
 */
internal fun createLoadingBlogPost(loadingTitle: String): BlogPost =
    BlogPost(
        id = BLOG_POST_LOADING_ID,
        title = loadingTitle,
        url = "",
        pubDate = null,
        summary = null,
        imageUrl = null,
        content = null,
    )

/**
 * Creates a BlogPost object for error state display.
 *
 * @param errorTitle The localized error title string
 * @return A BlogPost configured for error state
 */
internal fun createErrorBlogPost(errorTitle: String): BlogPost =
    BlogPost(
        id = BLOG_POST_ERROR_ID,
        title = errorTitle,
        url = "",
        pubDate = null,
        summary = null,
        imageUrl = null,
        content = null,
    )
