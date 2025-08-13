package dev.angussoftware.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import angussoftwareapp.composeapp.generated.resources.*
import dev.angussoftware.app.ui.components.ScreenContainer
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.components.SkillChip
import org.jetbrains.compose.resources.stringResource

private data class Project(
    val title: String,
    val description: String,
    val technologies: List<String>,
)

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
        ),
        Project(
            title = stringResource(Res.string.project_task_tracker_title),
            description = stringResource(Res.string.project_task_tracker_desc),
            technologies = listOf(
                Res.string.tech_kotlin,
                Res.string.tech_compose,
                Res.string.tech_sqlite,
                Res.string.tech_ktor
            ).map {
                stringResource(it)
            }
        ),
        Project(
            title = stringResource(Res.string.project_api_gateway_title),
            description = stringResource(Res.string.project_api_gateway_desc),
            technologies = listOf(
                Res.string.tech_ktor,
                Res.string.tech_jwt,
                Res.string.tech_docker
            ).map {
                stringResource(it)
            }
        )
    )

    ScreenContainer { alpha, tilePadding ->

        Column(
            verticalArrangement = Arrangement.spacedBy(tilePadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            projects.forEach { project ->
                SectionCard(alpha = alpha) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            project.technologies.forEach { tech ->
                                SkillChip(tech)
                            }
                        }
                    }
                }
            }
        }
    }
}