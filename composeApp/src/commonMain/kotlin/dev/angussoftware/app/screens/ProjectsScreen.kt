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

private data class Project(
    val title: String,
    val description: String,
    val technologies: List<String>
)

@Composable
fun ProjectsScreen() {
    val projects = listOf(
        Project(
            title = "Portfolio Website",
            description = "A responsive personal portfolio built with Compose Multiplatform, featuring dynamic content and smooth animations.",
            technologies = listOf("Kotlin", "Compose", "Multiplatform")
        ),
        Project(
            title = "Task Tracker App",
            description = "Cross-platform task management app with offline support and sync.",
            technologies = listOf("Kotlin", "Compose", "SQLite", "Ktor")
        ),
        Project(
            title = "API Gateway",
            description = "Lightweight API gateway with authentication and rate limiting.",
            technologies = listOf("Ktor", "JWT", "Docker")
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