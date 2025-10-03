package dev.angussoftware.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.*
import androidx.compose.ui.platform.testTag
import dev.angussoftware.app.screens.Screen
import dev.angussoftware.app.screens.BlogScreen
import dev.angussoftware.app.screens.BlogPostScreen
import dev.angussoftware.app.screens.HomeScreen
import dev.angussoftware.app.screens.ProjectsScreen
import dev.angussoftware.app.blog.BlogRepository
import angussoftwareapp.composeapp.generated.resources.Res
import angussoftwareapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
internal const val BLOG_POST_LOADING_ID = "loading"
internal const val BLOG_POST_ERROR_ID = "error"
internal const val RSS_FEED_URL = "https://rhomancer.github.io/angus-blog-content/rss.xml"

/**
 * Parses the post index from a navigation route string.
 * Extracts the numeric index from the end of the route path.
 * 
 * @param route The navigation route string (e.g., "BlogPost/5")
 * @return The parsed post index as Int, or 0 if parsing fails or route is invalid
 */
internal fun parsePostIndex(route: String?): Int {
    return try {
        val indexStr = route?.substringAfterLast("/") ?: ""
        indexStr.toIntOrNull() ?: 0
    } catch (e: Exception) {
        0
    }
}

/**
 * Creates a BlogPost object for loading state display.
 * 
 * @param loadingTitle The localized loading title string
 * @return A BlogPost configured for loading state
 */
internal fun createLoadingBlogPost(loadingTitle: String): dev.angussoftware.app.blog.BlogPost {
    return dev.angussoftware.app.blog.BlogPost(
        id = BLOG_POST_LOADING_ID,
        title = loadingTitle,
        url = "",
        pubDate = null,
        summary = null,
        imageUrl = null,
        content = null
    )
}

/**
 * Creates a BlogPost object for error state display.
 * 
 * @param errorTitle The localized error title string
 * @return A BlogPost configured for error state
 */
internal fun createErrorBlogPost(errorTitle: String): dev.angussoftware.app.blog.BlogPost {
    return dev.angussoftware.app.blog.BlogPost(
        id = BLOG_POST_ERROR_ID,
        title = errorTitle,
        url = "",
        pubDate = null,
        summary = null,
        imageUrl = null,
        content = null
    )
}

internal const val NAV_HOST_TEST_TAG = "NavHost"

@Composable
internal fun displayCurrentScreen(navController: NavHostController) {
    Scaffold {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.name,
            modifier = Modifier
                .fillMaxSize()
                .testTag(NAV_HOST_TEST_TAG)
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
                route = "${Screen.BlogPost.name}/{postIndex}",
                arguments = listOf(navArgument("postIndex") { type = NavType.StringType })
            ) { backStackEntry ->
                val postIndex = parsePostIndex(backStackEntry.destination.route)
                val feedUrl = RSS_FEED_URL
                
                var blogPost by remember { mutableStateOf<dev.angussoftware.app.blog.BlogPost?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                
                LaunchedEffect(postIndex) {
                    try {
                        val repository = BlogRepository(feedUrl)
                        val allPosts = repository.fetchPosts(limit = Int.MAX_VALUE)
                        blogPost = if (postIndex < allPosts.size) allPosts[postIndex] else null
                        isLoading = false
                    } catch (e: Exception) {
                        // todo: proper error handling
                        isLoading = false
                    }
                }
                
                if (isLoading) {
                    BlogPostScreen(
                        blogPost = createLoadingBlogPost(stringResource(Res.string.ui_loading)),
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    blogPost?.let { post ->
                        BlogPostScreen(
                            blogPost = post,
                            onBackClick = { navController.popBackStack() }
                        )
                    } ?: run {
                        BlogPostScreen(
                            blogPost = createErrorBlogPost(stringResource(Res.string.blog_post_not_found)),
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}