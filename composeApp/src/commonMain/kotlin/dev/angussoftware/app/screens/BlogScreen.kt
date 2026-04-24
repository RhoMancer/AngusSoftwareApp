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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import dev.angussoftware.app.navigation.RSS_FEED_URL
import androidx.navigation.NavHostController
import angussoftwareapp.composeapp.generated.resources.*
import dev.angussoftware.app.blog.BlogPost
import dev.angussoftware.app.blog.BlogRepository
import dev.angussoftware.app.ui.components.CommonTopAppBar
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.utils.rememberCommonScreenState
import org.jetbrains.compose.resources.stringResource

private const val PAGE_SIZE = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BlogScreen(
    navController: NavHostController? = null,
    initialPosts: List<BlogPost>? = null,
    initialIsLoading: Boolean? = null,
) {
    val feedUrl = RSS_FEED_URL

    var isLoading by remember { mutableStateOf(initialIsLoading ?: true) }
    var allPosts by remember { mutableStateOf(initialPosts ?: emptyList()) }
    val pageSize = PAGE_SIZE
    var visibleCount by remember { mutableStateOf(if (initialPosts != null) minOf(pageSize, initialPosts.size) else pageSize) }

    // Skip network fetch if test data is provided
    if (initialPosts == null) {
        LaunchedEffect(feedUrl) {
            val repository = BlogRepository(feedUrl)
            // Fetch all posts once, then paginate locally

            allPosts = repository.fetchPosts(limit = Int.MAX_VALUE)
            // Ensure initial visible count doesn't exceed size
            visibleCount = minOf(pageSize, allPosts.size)
            isLoading = false
        }
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

    Box(
        modifier = Modifier.testTag(BLOG_SCREEN_TEST_TAG),
    ) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            contentPadding =
                PaddingValues(
                    top = statusBarHeightDp + tilePadding,
                    bottom = bottomInset + tilePadding,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Title
            item {
                Text(
                    text = stringResource(Res.string.blog_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier =
                        Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                            .alpha(alpha),
                )
            }

            // RSS copy button
            item {
                val clipboard = LocalClipboardManager.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
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
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                allPosts.isEmpty() -> {
                    item {
                        Text(
                            text = stringResource(Res.string.blog_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                else -> {
                    items(allPosts.take(visibleCount).size) { idx ->
                        val visiblePosts = allPosts.take(visibleCount)
                        val post = visiblePosts[idx]
                        val itemModifier =
                            Modifier
                                .testTag("${BLOG_POST_ITEM_TEST_TAG}_$idx")
                                .clickable {
                                    navController?.navigate("${Screen.BlogPost.name}/$idx")
                                }
                        SectionCard(alpha = alpha, modifier = itemModifier) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                val textMeasurer = rememberTextMeasurer()
                                val titleStyle = MaterialTheme.typography.titleLarge
                                val title = post.title

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val maxWidthPx = constraints.maxWidth
                                    val iconSize = 24.dp
                                    val padding = 8.dp
                                    val density = LocalDensity.current
                                    val iconWidthPx = with(density) { (iconSize + padding).toPx() }

                                    val firstLineMaxWidth = (maxWidthPx - iconWidthPx).toInt().coerceAtLeast(0)

                                    val textLayoutResult = remember(title, maxWidthPx) {
                                        textMeasurer.measure(
                                            text = title,
                                            style = titleStyle,
                                            constraints = Constraints(maxWidth = firstLineMaxWidth)
                                        )
                                    }

                                    val breakIndex = textLayoutResult.getLineEnd(0, visibleEnd = true)

                                    val part1 = title.substring(0, minOf(title.length, breakIndex))
                                    val part2 = if (breakIndex < title.length) title.substring(breakIndex).trimStart() else ""

                                    Column {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = part1,
                                                style = titleStyle,
                                                modifier = Modifier
                                                    .widthIn(max = with(density) { firstLineMaxWidth.toDp() })
                                            )

                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                                contentDescription = "Open",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(iconSize)
                                            )
                                        }

                                        if (part2.isNotEmpty()) {
                                            Text(
                                                text = part2,
                                                style = titleStyle,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                                post.pubDate?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                post.summary?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                                if (!post.imageUrl.isNullOrBlank()) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .padding(top = 8.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "Image placeholder",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (visibleCount < allPosts.size) {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Button(
                                    onClick = { visibleCount = minOf(visibleCount + pageSize, allPosts.size) },
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
            showNonCompact = false,
        )
    }
}
