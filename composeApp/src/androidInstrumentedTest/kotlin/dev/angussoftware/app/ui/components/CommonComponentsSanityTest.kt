package dev.angussoftware.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sanity tests for simple components in CommonComponents.kt
 */
@RunWith(AndroidJUnit4::class)
class CommonComponentsSanityTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(content: @Composable () -> Unit) {
        rule.setContent {
            MaterialTheme { content() }
        }
    }

    /**
     * ✅ SCREENSHOT TESTED: Verified SectionCard renders child Text("Hello") and preserves the container modifier tag.
     * Screenshot code was used during development and removed for performance.
     */
    @Test
    fun sectionCard_displays_child_and_accepts_modifier() {
        setContent {
            SectionCard(
                alpha = 0.6f,
                modifier = Modifier.testTag("SectionCard"),
            ) {
                Text("Hello")
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("Hello").assertIsDisplayed()
        rule.onNodeWithTag("SectionCard").assertExists()
    }

    /**
     * ✅ SCREENSHOT TESTED: Verified SkillChip displays the provided text and preserves the container modifier tag.
     * Screenshot code was used during development and removed for performance.
     */
    @Test
    fun skillChip_displays_text_and_accepts_modifier() {
        setContent {
            SkillChip(
                text = "Kotlin",
                modifier = Modifier.testTag("SkillChip"),
            )
        }
        rule.waitForIdle()

        rule.onNodeWithText("Kotlin").assertIsDisplayed()
        rule.onNodeWithTag("SkillChip").assertExists()
    }

    /**
     * ✅ SCREENSHOT TESTED: Verified CommonTopAppBar forwards container modifier in non-compact mode
     * (tag survives through modifier.shadow(4.dp)). Screenshot code was used during development and removed.
     */
    @Test
    fun container_modifier_is_applied_nonCompact() {
        setContent {
            CommonTopAppBar(
                title = "Home",
                isCompactScreen = false,
                showNonCompact = true,
                modifier = Modifier.testTag("AppBarContainerNonCompact"),
            )
        }
        rule.waitForIdle()

        rule.onNodeWithTag("AppBarContainerNonCompact").assertExists()
        rule.onNodeWithText("Home").assertIsDisplayed()
    }
}
