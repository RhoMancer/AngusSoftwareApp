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
import dev.angussoftware.app.ui.components.ScreenContainer
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.components.SkillChip
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
)

@OptIn(ExperimentalFoundationApi::class)
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
            },
            images = listOf(
                Res.drawable.project_test1,
                Res.drawable.project_test2,
                Res.drawable.project_test3
            ),
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
            link = "https://play.google.com/store/apps/details?id=angus.gaming.taptargetbooster"
        )
    )

    val uriHandler = LocalUriHandler.current

    ScreenContainer { alpha, tilePadding ->

        Column(
            verticalArrangement = Arrangement.spacedBy(tilePadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            projects.forEach { project ->
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
                                        .height(180.dp)
                                        .padding(top = 8.dp)
                                ) { page ->
                                    val res = project.images[page]
                                    Image(
                                        painter = painterResource(res),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
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
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
    }
}