package dev.angussoftware.app.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import angussoftwareapp.composeapp.generated.resources.*
import dev.angussoftware.app.blog.BlogPost
import dev.angussoftware.app.blog.BlogRepository
import dev.angussoftware.app.currentWindowAdaptiveInfo
import dev.angussoftware.app.navigation.LocalNavigationBarHeight
import dev.angussoftware.app.ui.components.SectionCard
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogScreen() {
    val uriHandler = LocalUriHandler.current
    val feedUrl = "https://rhomancer.github.io/angus-blog-content/rss.xml"

    var isLoading by remember { mutableStateOf(true) }
    var allPosts by remember { mutableStateOf<List<BlogPost>>(emptyList()) }
    val pageSize = 20
    var visibleCount by remember { mutableStateOf(pageSize) }
    var selectedPost by remember { mutableStateOf<BlogPost?>(null) }
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
                    text = stringResource(Res.string.blog_title),
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
                        val clickableModifier = Modifier.clickable { selectedPost = post }
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

        if (isCompactScreen) {
            TopAppBar(
                title = { Text("Angus Software", modifier = Modifier.alpha(titleAlpha)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = bgAlpha),
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = bgAlpha)
                )
            )
        }

        // In-app reader overlay
        selectedPost?.let { post ->
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = statusBarHeightDp + tilePadding, bottom = bottomInset + tilePadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Close",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { selectedPost = null }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { uriHandler.openUri(post.url) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = "Open in browser",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Open",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    post.pubDate?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (!post.imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(top = 12.dp)
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
                    if (!post.content.isNullOrBlank()) {
                        Text(
                            text = post.content!!,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    } else {
                        post.summary?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}