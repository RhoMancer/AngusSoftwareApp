package dev.angussoftware.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import angussoftwareapp.composeapp.generated.resources.Res
import angussoftwareapp.composeapp.generated.resources.home_name
import org.jetbrains.compose.resources.stringResource

// Test/Debug semantics keys and tags for UI testing
internal val TitleAlphaKey: SemanticsPropertyKey<Float> = SemanticsPropertyKey("CommonTopAppBarTitleAlpha")
internal val BgAlphaKey: SemanticsPropertyKey<Float> = SemanticsPropertyKey("CommonTopAppBarBgAlpha")
internal const val COMMON_TOP_APP_BAR_TITLE_TAG: String = "CommonTopAppBarTitle"
internal const val COMMON_TOP_APP_BAR_ICON_TAG: String = "CommonTopAppBarIcon"

/**
 * SectionCard is a reusable Card matching the design used throughout the app's sections.
 * - Full width card
 * - Default elevation 4.dp
 * - Inner Column with 24.dp padding
 * - Supports fade-in alpha passed by parent
 */
@Composable
internal fun SectionCard(
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(alpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
        ) {
            content()
        }
    }
}

/**
 * Shared SkillChip used across screens for displaying tags/skills.
 */
@Composable
internal fun SkillChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(vertical = 4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun TitleWithIcon(
    title: String,
    icon: Painter?,
    modifier: Modifier = Modifier,
    debugSemantics: Boolean = false,
) {
    if (icon != null) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .let { base -> if (debugSemantics) base.testTag(COMMON_TOP_APP_BAR_ICON_TAG) else base },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title)
        }
    } else {
        Text(text = title, modifier = modifier)
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
 * @param modifier Modifier for the TopAppBar container
 * @param debugSemantics When true, attaches testTag and a semantics property to the title exposing titleAlpha for tests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommonTopAppBar(
    title: String = stringResource(Res.string.home_name),
    icon: Painter? = null,
    isCompactScreen: Boolean,
    titleAlpha: Float = 1f,
    bgAlpha: Float = 1f,
    showNonCompact: Boolean = true,
    modifier: Modifier = Modifier,
    debugSemantics: Boolean = false,
) {
    if (isCompactScreen) {
        val titleModifier =
            if (debugSemantics) {
                Modifier
                    .alpha(titleAlpha)
                    .testTag(COMMON_TOP_APP_BAR_TITLE_TAG)
                    .semantics { this.set(TitleAlphaKey, titleAlpha) }
            } else {
                Modifier.alpha(titleAlpha)
            }
        TopAppBar(
            modifier = if (debugSemantics) modifier.semantics { this.set(BgAlphaKey, bgAlpha) } else modifier,
            title = {
                TitleWithIcon(
                    title = title,
                    icon = icon,
                    modifier = titleModifier,
                    debugSemantics = debugSemantics,
                )
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = bgAlpha),
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = bgAlpha),
                ),
        )
    } else if (showNonCompact) {
        val titleModifier =
            if (debugSemantics) {
                Modifier.testTag(COMMON_TOP_APP_BAR_TITLE_TAG)
            } else {
                Modifier
            }
        TopAppBar(
            modifier = modifier.shadow(4.dp),
            title = {
                TitleWithIcon(
                    title = title,
                    icon = icon,
                    modifier = titleModifier,
                    debugSemantics = debugSemantics,
                )
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
        )
    }
}
