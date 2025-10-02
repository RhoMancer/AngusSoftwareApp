package dev.angussoftware.app.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import angussoftwareapp.composeapp.generated.resources.*
import com.angussoftware.theming.compose.resources.getAngusPaintPalette
import com.angussoftware.theming.compose.resources.getAngusSimpleLogoSystem
import com.angussoftware.theming.compose.resources.getAngusSolitareIcon
import com.angussoftware.theming.compose.resources.getBlinkReaderIcon
import com.angussoftware.theming.compose.resources.getGooglePlayStoreIcon
import com.angussoftware.theming.compose.resources.getTapTargetBoosterIcon
import dev.angussoftware.app.ui.utils.isWasm
import dev.angussoftware.app.ui.components.CommonTopAppBar
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.components.SkillChip
import dev.angussoftware.app.ui.utils.rememberCommonScreenState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private data class Project(
    val title: String,
    val subtitle: String? = null,
    val technologies: List<String> = listOf(),
    val description: String? = null,
    val link: String? = null,
    val images: List<DrawableResource> = emptyList(),
    val icon: DrawableResource? = null,
)

private const val IMAGE_ASPECT_RATIO = 16f / 9f

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectsScreen() {
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
            },
            icon = getAngusSimpleLogoSystem()
        ),
        Project(
            title = stringResource(Res.string.project_google_play_developer_account),
            link = "https://play.google.com/store/apps/dev?id=7308269362866323199",
            icon = getGooglePlayStoreIcon()
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
            link = "https://play.google.com/store/apps/details?id=gms.angusgaming.anguspaint",
            icon = getAngusPaintPalette()
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
            link = "https://play.google.com/store/apps/details?id=gms.angus.angussoli",
            icon = getAngusSolitareIcon()
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
            link = "https://play.google.com/store/apps/details?id=com.woods.blinkreader",
            icon = getBlinkReaderIcon()
        ),
        Project(
            title = stringResource(Res.string.project_tap_target_booster_title),
            subtitle = stringResource(Res.string.project_tap_target_booster_subtitle),
            description = stringResource(Res.string.project_tap_target_booster_desc),
            technologies = listOf(
                Res.string.tech_kotlin,
                Res.string.tech_xml
            ).map { stringResource(it) },
            images = listOf(
                Res.drawable.tap_target_1,
                Res.drawable.tap_target_2,
                Res.drawable.tap_target_3,
                Res.drawable.tap_target_4
            ),
            link = "https://play.google.com/store/apps/details?id=angus.gaming.taptargetbooster",
            icon = getTapTargetBoosterIcon()
        )
    )

    val uriHandler = LocalUriHandler.current

    val common = rememberCommonScreenState()

    val statusBarHeightDp = common.statusBarHeightDp
    val bottomInset = common.bottomInset
    val listState = common.listState
    val alpha = common.alpha
    val titleAlpha = common.titleAlpha
    val bgAlpha = common.bgAlpha
    val isCompactScreen = common.isCompactScreen
    val tilePadding = common.tilePadding
    val appBarHeightDp = common.appBarHeightDp
    val topContentPadding =
        if (!isCompactScreen) statusBarHeightDp + appBarHeightDp + tilePadding else statusBarHeightDp + tilePadding

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
                top = topContentPadding,
                bottom = bottomInset + tilePadding
            ),
            verticalArrangement = Arrangement.spacedBy(tilePadding)
        ) {
            items(projects.size) { idx ->
                val project = projects[idx]
                val clickableModifier = if (!project.link.isNullOrBlank()) {
                    Modifier.clickable { uriHandler.openUri(project.link) }
                } else {
                    Modifier
                }
                SectionCard(alpha = alpha, modifier = clickableModifier) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                project.icon?.let { iconResource ->
                                    Image(
                                        painter = painterResource(iconResource),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(end = 8.dp)
                                    )
                                }
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                            project.subtitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            if (project.images.isNotEmpty()) {
                                val pagerState =
                                    rememberPagerState(initialPage = 0, pageCount = { project.images.size })
                                val scope = rememberCoroutineScope()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(IMAGE_ASPECT_RATIO)
                                        .padding(top = 8.dp)
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        val res = project.images[page]
                                        Image(
                                            painter = painterResource(res),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    if (isWasm() && project.images.size > 1) {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    val prev = pagerState.currentPage - 1
                                                    if (prev >= 0) pagerState.animateScrollToPage(prev)
                                                }
                                            },
                                            enabled = pagerState.currentPage > 0,
                                            modifier = Modifier
                                                .align(androidx.compose.ui.Alignment.CenterStart)
                                                .size(72.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ChevronLeft,
                                                contentDescription = stringResource(Res.string.ui_previous),
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    val next = pagerState.currentPage + 1
                                                    if (next < project.images.size) pagerState.animateScrollToPage(next)
                                                }
                                            },
                                            enabled = pagerState.currentPage < project.images.size - 1,
                                            modifier = Modifier
                                                .align(androidx.compose.ui.Alignment.CenterEnd)
                                                .size(72.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ChevronRight,
                                                contentDescription = stringResource(Res.string.ui_next),
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
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
                                                        color = if (selected)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.3f
                                                            ),
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = stringResource(Res.string.ui_open),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }
        CommonTopAppBar(
            isCompactScreen = isCompactScreen,
            titleAlpha = titleAlpha,
            bgAlpha = bgAlpha,
            icon = painterResource(getAngusSimpleLogoSystem())
        )
    }
}