package dev.angussoftware.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import angussoftwareapp.composeapp.generated.resources.*
import dev.angussoftware.app.Screen
import dev.angussoftware.app.blog.BlogPost
import dev.angussoftware.app.blog.BlogRepository
import dev.angussoftware.app.ui.components.CommonTopAppBar
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.utils.rememberCommonScreenState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogScreen(navController: NavHostController? = null) {
    val feedUrl = "https://rhomancer.github.io/angus-blog-content/rss.xml"

    var isLoading by remember { mutableStateOf(true) }
    var allPosts by remember { mutableStateOf<List<BlogPost>>(emptyList()) }
    val pageSize = 20
    var visibleCount by remember { mutableStateOf(pageSize) }

    println("Fetching initial posts 0")


    LaunchedEffect(feedUrl) {
        val repository = BlogRepository(feedUrl)
        // Fetch all posts once, then paginate locally
        println("Fetching initial posts 1")

        allPosts = repository.fetchPosts(limit = Int.MAX_VALUE)
        // Ensure initial visible count doesn't exceed size
        visibleCount = minOf(pageSize, allPosts.size)
        isLoading = false
    }

    val common = rememberCommonScreenState()

    val statusBarHeightDp = common.statusBarHeightDp
    val bottomInset = common.bottomInset
    val listState = common.listState
    val alpha = common.alpha
    val titleAlpha = common.titleAlpha
    val bgAlpha = common.bgAlpha
    val isCompactScreen = common.isCompactScreen
    val tilePadding = common.tilePadding

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = statusBarHeightDp + tilePadding,
                bottom = bottomInset + tilePadding
            )
        ) {
            // Title
            item {
                Text(
                    text = stringResource(Res.string.nav_blog),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .alpha(alpha)
                )
            }

            // RSS copy button
            item {
                val clipboard = LocalClipboardManager.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { clipboard.setText(AnnotatedString(feedUrl)) }) {
                        Text("Copy RSS link")
                    }
                }
            }

            // Content states
            when {
                isLoading -> {
                    item {
                        Text(
                            text = stringResource(Res.string.blog_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                allPosts.isEmpty() -> {
                    item {
                        Text(
                            text = stringResource(Res.string.blog_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                else -> {
                    items(allPosts.take(visibleCount).size) { idx ->
                        val visiblePosts = allPosts.take(visibleCount)
                        val post = visiblePosts[idx]
                        val clickableModifier = Modifier.clickable {
                            navController?.navigate("${Screen.BlogPost.name}/$idx")
                        }
                        SectionCard(alpha = alpha, modifier = clickableModifier) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = post.title,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    post.pubDate?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    post.summary?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                    if (!post.imageUrl.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .padding(top = 8.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Image placeholder",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Open",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                    if (visibleCount < allPosts.size) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { visibleCount = minOf(visibleCount + pageSize, allPosts.size) }
                                ) {
                                    Text(text = stringResource(Res.string.blog_load_more))
                                }
                            }
                        }
                    }
                }
            }
        }

        CommonTopAppBar(
            isCompactScreen = isCompactScreen,
            titleAlpha = titleAlpha,
            bgAlpha = bgAlpha,
            showNonCompact = false
        )

    }
}