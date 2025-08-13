package dev.angussoftware.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.angussoftware.app.ui.components.ScreenContainer
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.components.SkillChip
import org.jetbrains.compose.resources.stringResource
import angussoftwareapp.composeapp.generated.resources.Res
import angussoftwareapp.composeapp.generated.resources.project_api_gateway_desc
import angussoftwareapp.composeapp.generated.resources.project_api_gateway_title
import angussoftwareapp.composeapp.generated.resources.project_portfolio_desc
import angussoftwareapp.composeapp.generated.resources.project_portfolio_title
import angussoftwareapp.composeapp.generated.resources.project_task_tracker_desc
import angussoftwareapp.composeapp.generated.resources.project_task_tracker_title
import angussoftwareapp.composeapp.generated.resources.tech_compose
import angussoftwareapp.composeapp.generated.resources.tech_docker
import angussoftwareapp.composeapp.generated.resources.tech_jwt
import angussoftwareapp.composeapp.generated.resources.tech_kotlin
import angussoftwareapp.composeapp.generated.resources.tech_ktor
import angussoftwareapp.composeapp.generated.resources.tech_multiplatform
import angussoftwareapp.composeapp.generated.resources.tech_sqlite

private data class Project(
    val title: String,
    val description: String,
    val technologies: List<String>
)

@Composable
fun ProjectsScreen() {
    val projects = listOf(
        Project(
            title = stringResource(Res.string.project_portfolio_title),
            description = stringResource(Res.string.project_portfolio_desc),
            technologies = listOf(
                stringResource(Res.string.tech_kotlin),
                stringResource(Res.string.tech_compose),
                stringResource(Res.string.tech_multiplatform)
            )
        ),
        Project(
            title = stringResource(Res.string.project_task_tracker_title),
            description = stringResource(Res.string.project_task_tracker_desc),
            technologies = listOf(
                stringResource(Res.string.tech_kotlin),
                stringResource(Res.string.tech_compose),
                stringResource(Res.string.tech_sqlite),
                stringResource(Res.string.tech_ktor)
            )
        ),
        Project(
            title = stringResource(Res.string.project_api_gateway_title),
            description = stringResource(Res.string.project_api_gateway_desc),
            technologies = listOf(
                stringResource(Res.string.tech_ktor),
                stringResource(Res.string.tech_jwt),
                stringResource(Res.string.tech_docker)
            )
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