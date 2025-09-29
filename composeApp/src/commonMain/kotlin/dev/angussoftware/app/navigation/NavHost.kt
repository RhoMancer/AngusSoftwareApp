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
import dev.angussoftware.app.Screen
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

@Composable
fun displayCurrentScreen(navController: NavHostController) {
    Scaffold {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.name,
            modifier = Modifier
                .fillMaxSize()
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
                val postIndex = try {
                    val route = backStackEntry.destination.route ?: ""
                    val indexStr = route.substringAfterLast("/")
                    indexStr.toIntOrNull() ?: 0
                } catch (e: Exception) {
                    0
                }
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
                        isLoading = false
                    }
                }
                
                if (isLoading) {
                    BlogPostScreen(
                        blogPost = dev.angussoftware.app.blog.BlogPost(
                            id = BLOG_POST_LOADING_ID,
                            title = stringResource(Res.string.ui_loading),
                            url = "",
                            pubDate = null,
                            summary = null,
                            imageUrl = null,
                            content = null
                        ),
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
                            blogPost = dev.angussoftware.app.blog.BlogPost(
                                id = BLOG_POST_ERROR_ID,
                                title = stringResource(Res.string.blog_post_not_found),
                                url = "",
                                pubDate = null,
                                summary = null,
                                imageUrl = null,
                                content = null
                            ),
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}