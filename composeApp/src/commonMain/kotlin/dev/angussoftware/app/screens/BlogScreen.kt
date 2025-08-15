package dev.angussoftware.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import angussoftwareapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import dev.angussoftware.app.blog.BlogPost
import dev.angussoftware.app.blog.BlogRepository
import dev.angussoftware.app.ui.components.ScreenContainer
import dev.angussoftware.app.ui.components.SectionCard

@Composable
fun BlogScreen() {
    val uriHandler = LocalUriHandler.current
    val feedUrl = "https://rhomancer.github.io/angus-blog-content/rss.xml"

    var isLoading by remember { mutableStateOf(true) }
    var posts by remember { mutableStateOf<List<BlogPost>>(emptyList()) }

    LaunchedEffect(feedUrl) {
        val repository = BlogRepository(feedUrl)
        posts = repository.fetchPosts(limit = 20)
        isLoading = false
    }

    ScreenContainer { alpha, tilePadding ->
        Text(
            text = stringResource(Res.string.blog_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        )

        when {
            isLoading -> {
                Text(
                    text = stringResource(Res.string.blog_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            posts.isEmpty() -> {
                Text(
                    text = stringResource(Res.string.blog_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(tilePadding),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    posts.forEach { post ->
                        SectionCard(alpha = alpha) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { uriHandler.openUri(post.url) }
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
                                    // Placeholder for image (no loader in v1)
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
                            Text(
                                text = "\u2197",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // TODO note for paging at bottom of scroll
        Text(
            text = stringResource(Res.string.blog_paging_todo),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )
    }
}