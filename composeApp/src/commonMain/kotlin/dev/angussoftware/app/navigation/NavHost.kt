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
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.angussoftware.app.blog.BlogUiState
import dev.angussoftware.app.blog.BlogViewModel
import dev.angussoftware.app.screens.*
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
internal fun parsePostIndex(route: String?): Int =
    try {
        val indexStr = route?.substringAfterLast("/") ?: ""
        indexStr.toIntOrNull() ?: 0
    } catch (e: Exception) {
        0
    }

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
                route = "${Screen.BlogPost.name}/{postIndex}",
                arguments = listOf(navArgument("postIndex") { type = NavType.StringType }),
            ) { backStackEntry ->
                val postIndex = backStackEntry.savedStateHandle.get<String>("postIndex")?.toIntOrNull() ?: 0
                val blogViewModel: BlogViewModel = viewModel()
                val uiState by blogViewModel.uiState.collectAsState()

                when (val state = uiState) {
                    is BlogUiState.Loading -> {
                        BlogPostScreen(
                            blogPost = createLoadingBlogPost(stringResource(Res.string.ui_loading)),
                            onBackClick = { navController.popBackStack() },
                        )
                    }
                    is BlogUiState.Success -> {
                        val post = state.posts.getOrNull(postIndex)
                        if (post != null) {
                            BlogPostScreen(
                                blogPost = post,
                                onBackClick = { navController.popBackStack() },
                            )
                        } else {
                            BlogPostScreen(
                                blogPost = createErrorBlogPost(stringResource(Res.string.blog_post_not_found)),
                                onBackClick = { navController.popBackStack() },
                            )
                        }
                    }
                    is BlogUiState.Error -> {
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
