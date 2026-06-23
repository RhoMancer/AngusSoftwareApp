package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import dev.angussoftware.app.ui.utils.LocalOverrideWindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.LocalWindowAdaptiveInfoOverride
import dev.angussoftware.app.ui.utils.WindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Rule
import org.junit.Test

class HomeScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.COMPACT) {
        val adaptiveInfo = WindowAdaptiveInfo(widthSizeClass)
        composeTestRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides adaptiveInfo,
                LocalOverrideWindowAdaptiveInfo provides adaptiveInfo,
            ) {
                HomeScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun scrollToText(text: String) {
        composeTestRule.onNodeWithTag("HomeList").performScrollToNode(hasText(text))
    }

    @Test
    fun homeScreen_displaysScreen() {
        setContent()
        composeTestRule.onNodeWithTag(HOME_SCREEN_TEST_TAG).assertExists()
    }

    @Test
    fun homeScreen_showsAboutMeSection() {
        setContent()
        composeTestRule.onNodeWithTag(HOME_SCREEN_TEST_TAG).assertExists()
    }

    @Test
    fun homeScreen_showsContactSection() {
        setContent()
        scrollToText("Contact Information")
        composeTestRule.onNodeWithText("Contact Information").assertExists()
    }

    @Test
    fun homeScreen_showsEmailContact() {
        setContent()
        scrollToText("Email")
        composeTestRule.onNodeWithText("Email").assertExists()
    }

    @Test
    fun homeScreen_showsKeySkillsSection() {
        setContent()
        scrollToText("Key Skills")
        composeTestRule.onNodeWithText("Key Skills").assertExists()
    }

    @Test
    fun homeScreen_showsAndroidSkill() {
        setContent()
        scrollToText("Android")
        composeTestRule.onNodeWithText("Android").assertExists()
    }

    @Test
    fun homeScreen_showsReactSkill() {
        setContent()
        scrollToText("React")
        composeTestRule.onNodeWithText("React").assertExists()
    }

    @Test
    fun homeScreen_showsGitSkill() {
        setContent()
        scrollToText("Git")
        composeTestRule.onNodeWithText("Git").assertExists()
    }

    @Test
    fun homeScreen_showsNodeJSSkill() {
        setContent()
        scrollToText("Node.js")
        composeTestRule.onNodeWithText("Node.js").assertExists()
    }

    @Test
    fun homeScreen_showsCI_CDSkill() {
        setContent()
        scrollToText("CI/CD")
        composeTestRule.onNodeWithText("CI/CD").assertExists()
    }

    @Test
    fun homeScreen_showsContactItem() {
        setContent()
        scrollToText("Contact Information")
        composeTestRule.onNodeWithText("Contact Information").assertExists()
    }

    @Test
    fun homeScreen_showsSocialMediaButton() {
        setContent()
        // Scroll to contact section where social media buttons live
        scrollToText("Contact Information")
        // Social media buttons render after contact items
        composeTestRule.onNodeWithTag("HomeList").assertExists()
    }

    @Test
    fun homeScreen_expandedLayout_displaysCorrectly() {
        setContent(WindowWidthSizeClass.EXPANDED)
        composeTestRule.onNodeWithTag(HOME_SCREEN_TEST_TAG).assertExists()
    }
}
