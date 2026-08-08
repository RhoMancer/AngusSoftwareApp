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
import angussoftwareapp.composeapp.generated.resources.blog_post_load_error
import angussoftwareapp.composeapp.generated.resources.blog_post_not_found
import angussoftwareapp.composeapp.generated.resources.ui_loading
import dev.angussoftware.app.blog.BlogRepository
import dev.angussoftware.app.screens.*
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource

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
            composable(route = Screen.Settings.name) {
                SettingsScreen()
            }
            composable(
                route = "${Screen.BlogPost.name}/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val postId = backStackEntry.savedStateHandle.get<String>("postId").orEmpty()
                val feedUrl = RSS_FEED_URL

                var loadResult by remember { mutableStateOf<BlogPostLoadResult>(BlogPostLoadResult.Loading) }

                LaunchedEffect(postId) {
                    try {
                        val repository = BlogRepository(feedUrl)
                        val allPosts = repository.fetchPosts(limit = Int.MAX_VALUE)
                        loadResult = blogPostLoadResult(allPosts, postId)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        loadResult = BlogPostLoadResult.Error
                    }
                }

                when (val result = loadResult) {
                    BlogPostLoadResult.Loading ->
                        BlogPostScreen(
                            blogPost = createLoadingBlogPost(stringResource(Res.string.ui_loading)),
                            onBackClick = { navController.popBackStack() },
                        )

                    is BlogPostLoadResult.Post ->
                        BlogPostScreen(
                            blogPost = result.value,
                            onBackClick = { navController.popBackStack() },
                        )

                    BlogPostLoadResult.NotFound ->
                        BlogPostScreen(
                            blogPost = createErrorBlogPost(stringResource(Res.string.blog_post_not_found)),
                            onBackClick = { navController.popBackStack() },
                        )

                    BlogPostLoadResult.Error ->
                        BlogPostScreen(
                            blogPost = createErrorBlogPost(stringResource(Res.string.blog_post_load_error)),
                            onBackClick = { navController.popBackStack() },
                        )
                }
            }
        }
    }
}
