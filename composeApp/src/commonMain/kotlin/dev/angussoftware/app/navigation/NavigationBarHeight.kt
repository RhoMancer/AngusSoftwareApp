package dev.angussoftware.app.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal to provide the NavigationBar height throughout the application.
 * Default value is 0.dp, which should be updated with the actual measured height.
 */
val LocalNavigationBarHeight = compositionLocalOf { 0.dp }

/**
 * Default height for Material3 NavigationBar based on Material Design guidelines.
 * This is used as a fallback if measurement fails.
 */
val DefaultNavigationBarHeight = 80.dp
