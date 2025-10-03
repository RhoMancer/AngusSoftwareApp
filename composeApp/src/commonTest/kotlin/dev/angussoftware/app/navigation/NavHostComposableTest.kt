package dev.angussoftware.app.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigation.compose.rememberNavController
import kotlin.test.Test

class NavHostComposableTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testDisplayCurrentScreenRendersNavHost() = runComposeUiTest {
        setContent {
            val navController = rememberNavController()
            displayCurrentScreen(navController)
        }
        onNode(hasTestTag("NavHost"), useUnmergedTree = true).assertExists(
            "NavHost should be displayed"
        )
    }
}