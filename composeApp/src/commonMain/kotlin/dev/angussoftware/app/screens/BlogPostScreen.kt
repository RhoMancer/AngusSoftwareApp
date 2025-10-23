package dev.angussoftware.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import angussoftwareapp.composeapp.generated.resources.*
import dev.angussoftware.app.blog.BlogPost
import dev.angussoftware.app.navigation.LocalNavigationBarHeight
import dev.angussoftware.app.ui.utils.currentWindowAdaptiveInfo
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BlogPostScreen(
    blogPost: BlogPost,
    onBackClick: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val windowInfo = currentWindowAdaptiveInfo()
    val isCompactScreen = windowInfo.isCompact
    val navigationBarHeight = LocalNavigationBarHeight.current

    // Calculate padding for status bar and bottom navigation
    val statusBarHeightDp = if (isCompactScreen) 24.dp else 0.dp
    val bottomInset = if (isCompactScreen) navigationBarHeight else 16.dp
    val tilePadding = 16.dp

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(
                        top = statusBarHeightDp + tilePadding,
                        bottom = bottomInset + tilePadding,
                    ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.ui_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onBackClick() },
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { uriHandler.openUri(blogPost.url) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = stringResource(Res.string.ui_open_in_browser),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(Res.string.ui_open),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = blogPost.title,
                style = MaterialTheme.typography.headlineSmall,
            )
            blogPost.pubDate?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (!blogPost.imageUrl.isNullOrBlank()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_image_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!blogPost.content.isNullOrBlank()) {
                Text(
                    text = blogPost.content!!,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                blogPost.summary?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            // Test-only readout: expose the current LocalNavigationBarHeight for UI tests via a hidden tag
            // This has negligible UX impact and helps verify inset propagation in instrumentation tests.
            Box(modifier = Modifier.padding(0.dp)) {
                Text(
                    text = "${navigationBarHeight.value}",
                    modifier = Modifier.testTag("LOCAL_NAV_BAR_HEIGHT_DP"),
                )
            }
        }
    }
}
