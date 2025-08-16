package dev.angussoftware.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.navigation.LocalNavigationBarHeight
import dev.angussoftware.app.currentWindowAdaptiveInfo
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogScreen() {
    val uriHandler = LocalUriHandler.current
    val feedUrl = "https://rhomancer.github.io/angus-blog-content/rss.xml"

    var isLoading by remember { mutableStateOf(true) }
    var allPosts by remember { mutableStateOf<List<BlogPost>>(emptyList()) }
    val pageSize = 20
    var visibleCount by remember { mutableStateOf(pageSize) }

    LaunchedEffect(feedUrl) {
        val repository = BlogRepository(feedUrl)
        // Fetch all posts once, then paginate locally
        allPosts = repository.fetchPosts(limit = Int.MAX_VALUE)
        // Ensure initial visible count doesn't exceed size
        visibleCount = minOf(pageSize, allPosts.size)
        isLoading = false
    }

    // Insets similar to HomeScreen
    val statusBarHeightPx = WindowInsets.statusBars.getTop(LocalDensity.current)
    val navigationBarHeightPx = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    val density = LocalDensity.current
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }
    val systemNavigationBarHeightDp = with(density) { navigationBarHeightPx.toDp() }
    val appNavigationBarHeightDp = LocalNavigationBarHeight.current
    val bottomInset = systemNavigationBarHeightDp + appNavigationBarHeightDp

    // Scroll/collapse behavior
    val listState = rememberLazyListState()
    val collapseThresholdPx = with(density) { 120.dp.toPx() }
    val isCollapsed by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            index > 0 || offset > collapseThresholdPx.toInt()
        }
    }

    // Animations
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeIn"
    )
    LaunchedEffect(Unit) { isVisible = true }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        label = "topBarTitleAlpha"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        label = "topBarBgAlpha"
    )

    val isCompactScreen = currentWindowAdaptiveInfo().isCompact

    val tilePadding = 16.dp

    Box(
    ) {
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
                    text = stringResource(Res.string.blog_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .alpha(alpha)
                )
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

        if (isCompactScreen) {
            TopAppBar(
                title = { Text("Angus Software", modifier = Modifier.alpha(titleAlpha)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = bgAlpha),
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = bgAlpha)
                )
            )
        }
    }
}