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
import dev.angussoftware.app.blog.BlogPost
import dev.angussoftware.app.blog.BlogRepository
import dev.angussoftware.app.screens.*
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

                var blogPost by remember { mutableStateOf<BlogPost?>(null) }
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
