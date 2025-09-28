package dev.angussoftware.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/**
 * SectionCard is a reusable Card matching the design used throughout the app's sections.
 * - Full width card
 * - Default elevation 4.dp
 * - Inner Column with 24.dp padding
 * - Supports fade-in alpha passed by parent
 */
@Composable
fun SectionCard(
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            content()
        }
    }
}

/**
 * Shared SkillChip used across screens for displaying tags/skills.
 */
@Composable
fun SkillChip(text: String) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Reusable TopAppBar component used across screens to eliminate duplication.
 * Handles both compact and non-compact screen layouts with appropriate styling.
 *
 * @param title The title text to display in the app bar
 * @param icon Optional icon to display at the start of the title (null = no icon shown)
 * @param isCompactScreen Whether the screen is in compact mode
 * @param titleAlpha Alpha value for the title (used in compact mode)
 * @param bgAlpha Alpha value for the background (used in compact mode)
 * @param showNonCompact Whether to show the non-compact version when not in compact mode (default: true)
 */
@Composable
private fun TitleWithIcon(
    title: String,
    icon: Painter?,
    modifier: Modifier = Modifier
) {
    if (icon != null) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title)
        }
    } else {
        Text(text = title, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String = "Angus Software",
    icon: Painter? = null,
    isCompactScreen: Boolean,
    titleAlpha: Float = 1f,
    bgAlpha: Float = 1f,
    showNonCompact: Boolean = true
) {
    if (isCompactScreen) {
        TopAppBar(
            title = { 
                TitleWithIcon(
                    title = title,
                    icon = icon,
                    modifier = Modifier.alpha(titleAlpha)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = bgAlpha),
                scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = bgAlpha)
            )
        )
    } else if (showNonCompact) {
        TopAppBar(
            modifier = Modifier.shadow(4.dp),
            title = { 
                TitleWithIcon(
                    title = title,
                    icon = icon
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
