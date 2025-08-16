package dev.angussoftware.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import angussoftwareapp.composeapp.generated.resources.*
import dev.angussoftware.app.navigation.LocalNavigationBarHeight
import dev.angussoftware.app.currentWindowAdaptiveInfo
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.components.SkillChip
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha

private data class Project(
    val title: String,
    val subtitle: String? = null,
    val technologies: List<String> = listOf(),
    val description: String? = null,
    val link: String? = null,
    val images: List<DrawableResource> = emptyList(),
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen() {
    val projects = listOf(
        Project(
            title = stringResource(Res.string.project_portfolio_title),
            description = stringResource(Res.string.project_portfolio_desc),
            technologies = listOf(
                Res.string.tech_kotlin,
                Res.string.tech_compose,
                Res.string.tech_multiplatform
            ).map {
                stringResource(it)
            }
            // link = "https://your-portfolio-url.example" // Optional: provide when available
        ),
        Project(
            title = stringResource(Res.string.project_google_play_developer_account),
            link = "https://play.google.com/store/apps/dev?id=7308269362866323199"
        ),
        Project(
            title = stringResource(Res.string.project_angus_paint_title),
            subtitle = stringResource(Res.string.project_angus_paint_subtitle),
            description = stringResource(Res.string.project_angus_paint_desc),
            technologies = listOf(
                Res.string.tech_kotlin,
                Res.string.tech_xml
            ).map { stringResource(it) },
            images = listOf(
                Res.drawable.angus_paint_1,
                Res.drawable.angus_paint_2,
                Res.drawable.angus_paint_3,
                Res.drawable.angus_paint_4,
                Res.drawable.angus_paint_5,
                Res.drawable.angus_paint_6,
                Res.drawable.angus_paint_7
            ),
            link = "https://play.google.com/store/apps/details?id=gms.angusgaming.anguspaint"
        ),
        Project(
            title = stringResource(Res.string.project_angus_solitaire_title),
            subtitle = stringResource(Res.string.project_angus_solitaire_subtitle),
            description = stringResource(Res.string.project_angus_solitaire_desc),
            technologies = listOf(
                Res.string.tech_kotlin,
                Res.string.tech_xml
            ).map { stringResource(it) },
            images = listOf(
                Res.drawable.angus_solitaire_1,
                Res.drawable.angus_solitaire_2,
                Res.drawable.angus_solitaire_3
            ),
            link = "https://play.google.com/store/apps/details?id=gms.angus.angussoli"
        ),
        Project(
            title = stringResource(Res.string.project_blink_reader_title),
            subtitle = stringResource(Res.string.project_blink_reader_subtitle),
            description = stringResource(Res.string.project_blink_reader_desc),
            technologies = listOf(
                Res.string.tech_kotlin,
                Res.string.tech_xml
            ).map { stringResource(it) },
            images = listOf(
                Res.drawable.blink_reader_1,
                Res.drawable.blink_reader_2,
                Res.drawable.blink_reader_3
            ),
            link = "https://play.google.com/store/apps/details?id=com.woods.blinkreader"
        ),
        Project(
            title = stringResource(Res.string.project_tap_target_booster_title),
            subtitle = stringResource(Res.string.project_tap_target_booster_subtitle),
            description = stringResource(Res.string.project_tap_target_booster_desc),
            technologies = listOf(
                Res.string.tech_kotlin,
                Res.string.tech_compose
            ).map { stringResource(it) },
            images = listOf(
                Res.drawable.tap_target_1,
                Res.drawable.tap_target_2,
                Res.drawable.tap_target_3,
                Res.drawable.tap_target_4
            ),
            link = "https://play.google.com/store/apps/details?id=angus.gaming.taptargetbooster"
        )
    )

    val uriHandler = LocalUriHandler.current

    // Insets similar to HomeScreen
    val statusBarHeightPx = WindowInsets.statusBars.getTop(LocalDensity.current)
    val navigationBarHeightPx = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    val density = LocalDensity.current
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }
    val systemNavigationBarHeightDp = with(density) { navigationBarHeightPx.toDp() }

    val appNavigationBarHeightDp = LocalNavigationBarHeight.current
    val bottomInset = systemNavigationBarHeightDp + appNavigationBarHeightDp

    // Scroll and collapse logic
    val listState = rememberLazyListState()
    val collapseThresholdPx = with(density) { 120.dp.toPx() }
    val isCollapsed by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            index > 0 || offset > collapseThresholdPx.toInt()
        }
    }

    // Fade-in for content and app bar visuals
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
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = statusBarHeightDp + tilePadding,
                bottom = bottomInset + tilePadding
            ),
            verticalArrangement = Arrangement.spacedBy(tilePadding)
        ) {
            items(projects.size) { idx ->
                val project = projects[idx]
                val clickableModifier = if (!project.link.isNullOrBlank()) {
                    Modifier.clickable { uriHandler.openUri(project.link!!) }
                } else {
                    Modifier
                }
                SectionCard(alpha = alpha, modifier = clickableModifier) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            project.subtitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            if (project.images.isNotEmpty()) {
                                val pagerState = rememberPagerState(initialPage = 0, pageCount = { project.images.size })
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f/9f)
                                        .padding(top = 8.dp)
                                ) { page ->
                                    val res = project.images[page]
                                    Image(
                                        painter = painterResource(res),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                if (project.images.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        repeat(project.images.size) { index ->
                                            val selected = pagerState.currentPage == index
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 3.dp)
                                                    .size(if (selected) 8.dp else 6.dp)
                                                    .background(
                                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                            project.description?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            if (project.technologies.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    project.technologies.forEach { tech ->
                                        SkillChip(tech)
                                    }
                                }
                            }
                        }
                        if (!project.link.isNullOrBlank()) {
                            Text(
                                text = "\u2197",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }
        if (isCompactScreen) {
            TopAppBar(
                title = { Text("Angus Software", modifier = Modifier.alpha(titleAlpha)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = bgAlpha),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = bgAlpha)
                )
            )
        }
    }
}