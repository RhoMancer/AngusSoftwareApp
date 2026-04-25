package dev.angussoftware.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import angussoftwareapp.composeapp.generated.resources.Res
import angussoftwareapp.composeapp.generated.resources.blog_post_not_found
import angussoftwareapp.composeapp.generated.resources.ui_loading
import dev.angussoftware.app.blog.BlogRepository
import dev.angussoftware.app.screens.*
import org.jetbrains.compose.resources.stringResource

internal const val BLOG_POST_LOADING_ID = "loading"
internal const val BLOG_POST_ERROR_ID = "error"
internal const val RSS_FEED_URL = "https://rhomancer.github.io/angus-blog-content/rss.xml"

/**
 * Parses the post ID from a navigation route string.
 * Extracts the ID (everything after the last slash) from the route path.
 *
 * @param route The navigation route string (e.g., "BlogPost/post1")
 * @return The parsed post ID, or empty string if route is null/blank
 */
internal fun parsePostId(route: String?): String =
    route?.substringAfterLast("/").orEmpty()

/**
 * Creates a BlogPost object for loading state display.
 *
 * @param loadingTitle The localized loading title string
 * @return A BlogPost configured for loading state
 */
internal fun createLoadingBlogPost(loadingTitle: String): dev.angussoftware.app.blog.BlogPost =
    dev.angussoftware.app.blog.BlogPost(
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
internal fun createErrorBlogPost(errorTitle: String): dev.angussoftware.app.blog.BlogPost =
    dev.angussoftware.app.blog.BlogPost(
        id = BLOG_POST_ERROR_ID,
        title = errorTitle,
        url = "",
        pubDate = null,
        summary = null,
        imageUrl = null,
        content = null,
    )

internal const val NAV_HOST_TEST_TAG = "NavHost"

@Composable
internal fun displayCurrentScreen(navController: NavHostController) {
    Scaffold {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.name,
            modifier =
                Modifier
                    .fillMaxSize()
                    .testTag(NAV_HOST_TEST_TAG),
        ) {
            composable(route = Screen.Home.name) {
                HomeScreen()
            }
            composable(route = Screen.Projects.name) {
                ProjectsScreen()
            }
            composable(route = Screen.Blog.name) {
                BlogScreen(navController)
            }
            composable(
                route = "${Screen.BlogPost.name}/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val postId = backStackEntry.savedStateHandle.get<String>("postId").orEmpty()
                val feedUrl = RSS_FEED_URL

                var blogPost by remember { mutableStateOf<dev.angussoftware.app.blog.BlogPost?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(postId) {
                    try {
                        val repository = BlogRepository(feedUrl)
                        val allPosts = repository.fetchPosts(limit = Int.MAX_VALUE)
                        blogPost = allPosts.find { it.id == postId }
                        isLoading = false
                    } catch (e: Exception) {
                        // todo: proper error handling
                        isLoading = false
                    }
                }

                if (isLoading) {
                    BlogPostScreen(
                        blogPost = createLoadingBlogPost(stringResource(Res.string.ui_loading)),
                        onBackClick = { navController.popBackStack() },
                    )
                } else {
                    blogPost?.let { post ->
                        BlogPostScreen(
                            blogPost = post,
                            onBackClick = { navController.popBackStack() },
                        )
                    } ?: run {
                        BlogPostScreen(
                            blogPost = createErrorBlogPost(stringResource(Res.string.blog_post_not_found)),
                            onBackClick = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
