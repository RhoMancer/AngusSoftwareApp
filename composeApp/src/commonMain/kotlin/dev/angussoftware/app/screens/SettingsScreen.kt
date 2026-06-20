package dev.angussoftware.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import angussoftwareapp.composeapp.generated.resources.Res
import angussoftwareapp.composeapp.generated.resources.*
import com.angussoftware.theming.compose.ui.theme.ColorTheme
import com.angussoftware.theming.compose.ui.theme.ThemeMode
import dev.angussoftware.app.theme.rememberAppThemeState
import dev.angussoftware.app.ui.components.CommonTopAppBar
import dev.angussoftware.app.ui.components.SectionCard
import dev.angussoftware.app.ui.utils.rememberCommonScreenState
import org.jetbrains.compose.resources.stringResource

internal const val SETTINGS_SCREEN_TEST_TAG = "SettingsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen() {
    val themeState = rememberAppThemeState()
    val prefs = themeState.prefs
    val common = rememberCommonScreenState()
    val allThemes = ColorTheme.allWithNames

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SETTINGS_SCREEN_TEST_TAG),
    ) {
        LazyColumn(
            state = common.listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = common.topContentPadding,
                bottom = common.bottomInset + common.tilePadding,
            ),
            verticalArrangement = Arrangement.spacedBy(common.tilePadding),
        ) {
            // Appearance section
            item {
                SectionCard(alpha = common.alpha) {
                    Text(
                        text = stringResource(Res.string.settings_appearance),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // Theme Mode selector
                    Text(
                        text = stringResource(Res.string.settings_theme_mode),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    ThemeModeSelector(
                        selected = prefs.themeMode,
                        onSelect = { mode -> themeState.updateThemeMode(mode) },
                    )

                    Spacer(Modifier.height(24.dp))

                    // Light theme dropdown
                    Text(
                        text = stringResource(Res.string.settings_light_theme),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    ThemeDropdown(
                        allThemes = allThemes,
                        selected = prefs.lightTheme,
                        onSelect = { theme -> themeState.updateLightTheme(theme) },
                    )
                    if (!prefs.lightTheme.isLightTheme && prefs.lightTheme != ColorTheme.Angus) {
                        WarningText(stringResource(Res.string.settings_theme_warning_dark_in_light))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Dark theme dropdown
                    Text(
                        text = stringResource(Res.string.settings_dark_theme),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    ThemeDropdown(
                        allThemes = allThemes,
                        selected = prefs.darkTheme,
                        onSelect = { theme -> themeState.updateDarkTheme(theme) },
                    )
                    if (!prefs.darkTheme.isDarkTheme && prefs.darkTheme != ColorTheme.Angus) {
                        WarningText(stringResource(Res.string.settings_theme_warning_light_in_dark))
                    }
                }
            }
        }
        CommonTopAppBar(
            title = stringResource(Res.string.settings_title),
            isCompactScreen = common.isCompactScreen,
            titleAlpha = common.titleAlpha,
            bgAlpha = common.bgAlpha,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeMode.SYSTEM to stringResource(Res.string.settings_theme_mode_system),
        ThemeMode.LIGHT to stringResource(Res.string.settings_theme_mode_light),
        ThemeMode.DARK to stringResource(Res.string.settings_theme_mode_dark),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (mode, label) ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                label = { Text(label) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeDropdown(
    allThemes: List<Pair<ColorTheme, String>>,
    selected: ColorTheme,
    onSelect: (ColorTheme) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = allThemes.find { it.first == selected }?.second ?: "Angus"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(selectedName, modifier = Modifier.weight(1f))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            allThemes.forEach { (theme, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(theme)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun WarningText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
    )
}
