package dev.angussoftware.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import dev.angussoftware.app.ui.utils.LocalOverrideWindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.LocalWindowAdaptiveInfoOverride
import dev.angussoftware.app.ui.utils.WindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Rule

/**
 * Base class for instrumented Compose UI tests.
 *
 * Provides:
 * - [composeTestRule] for Compose testing
 * - [setAdaptiveContent] to test layouts at different window sizes
 */
abstract class BaseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Sets the Compose content with a specific window width size class override.
     * This allows deterministic testing of adaptive layouts.
     *
     * Overrides both [LocalWindowAdaptiveInfoOverride] (used by AngusSoftwareAppScreen)
     * and [LocalOverrideWindowAdaptiveInfo] (used by CommonScreenState).
     */
    internal fun setAdaptiveContent(
        widthSizeClass: WindowWidthSizeClass,
        content: @Composable () -> Unit,
    ) {
        val adaptiveInfo = WindowAdaptiveInfo(widthSizeClass)
        composeTestRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides adaptiveInfo,
                LocalOverrideWindowAdaptiveInfo provides adaptiveInfo,
            ) {
                content()
            }
        }
    }
}
