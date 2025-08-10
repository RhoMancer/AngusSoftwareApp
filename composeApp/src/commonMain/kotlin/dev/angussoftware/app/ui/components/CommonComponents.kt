package dev.angussoftware.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import dev.angussoftware.app.navigation.LocalNavigationBarHeight

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
 * ScreenContainer centralizes the common screen scaffolding used by multiple screens:
 * - Vertical scroll support
 * - Fade-in animation providing alpha for child content
 * - Horizontal padding of 16.dp
 * - Top spacer for status bar + tile padding
 * - Bottom spacer for system + app navigation bar + tile padding
 *
 * The [content] lambda receives the animated [alpha] and the standard [tilePadding].
 */
@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(alpha: Float, tilePadding: Dp) -> Unit
) {
    val scrollState = rememberScrollState()

    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeIn"
    )

    LaunchedEffect(Unit) { isVisible = true }

    // Insets
    val statusBarHeightPx = WindowInsets.statusBars.getTop(LocalDensity.current)
    val navigationBarHeightPx = WindowInsets.navigationBars.getBottom(LocalDensity.current)

    val density = LocalDensity.current
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }
    val systemNavigationBarHeightDp = with(density) { navigationBarHeightPx.toDp() }

    val appNavigationBarHeightDp = LocalNavigationBarHeight.current
    val navigationBarHeightDp = systemNavigationBarHeightDp + appNavigationBarHeightDp

    val tilePadding = 16.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(statusBarHeightDp + tilePadding))

        content(alpha, tilePadding)

        Spacer(modifier = Modifier.height(navigationBarHeightDp + tilePadding))
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
