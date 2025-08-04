package dev.angussoftware.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.angussoftware.theming.compose.ui.theme.AngusTheme
import dev.angussoftware.app.navigation.displayCurrentScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Create


// Define an enum class for different screens
enum class Screen {
    Home,
    Projects,
    Blog
}

@Composable
@Preview
fun App(navController: NavHostController = rememberNavController()) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Create a navigation state to track the current screen
    var currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.Home.name
    )

    // Get window size information
    val windowInfo = currentWindowAdaptiveInfo()

    // Determine if we should use NavigationRail based on window width
    val useNavigationRail = !windowInfo.isCompact

    AngusTheme {
        if (useNavigationRail) {
            // Layout with NavigationRail for medium and larger screens
            Row(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxHeight(),
                    color = NavigationBarDefaults.containerColor
                ) {
                    // Navigation rail
                    NavigationRail(
                        Modifier
                            .align(Alignment.CenterVertically)
                            .wrapContentHeight(
                                Alignment.CenterVertically,
                                true
                            ),
                        containerColor = NavigationBarDefaults.containerColor
                    ) {
                        NavigationRailItem(
                            selected = navController.currentDestination?.route == Screen.Home.name,
                            onClick = { navController.navigate(Screen.Home.name) },
                            label = { Text("Home") },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
                        )
                        NavigationRailItem(
                            selected = navController.currentDestination?.route == Screen.Projects.name,
                            onClick = { navController.navigate(Screen.Projects.name) },
                            label = { Text("Projects") },
                            icon = { Icon(Icons.Default.List, contentDescription = "Projects") }
                        )
                        NavigationRailItem(
                            selected = navController.currentDestination?.route == Screen.Blog.name,
                            onClick = { navController.navigate(Screen.Blog.name) },
                            label = { Text("Blog") },
                            icon = { Icon(Icons.Default.Create, contentDescription = "Blog") }
                        )
                    }
                }
                displayCurrentScreen(navController)
            }
        } else {
            // Layout with NavigationBar for small screens
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Navigation bar
                NavigationBar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBarItem(
                        selected = navController.currentDestination?.route == Screen.Home.name,
                        onClick = { navController.navigate(Screen.Home.name) },
                        label = { Text("Home") },
                        icon = { Text("🏠") }
                    )
                    NavigationBarItem(
                        selected = navController.currentDestination?.route == Screen.Projects.name,
                        onClick = { navController.navigate(Screen.Projects.name) },
                        label = { Text("Projects") },
                        icon = { Text("📋") }
                    )
                    NavigationBarItem(
                        selected = navController.currentDestination?.route == Screen.Blog.name,
                        onClick = { navController.navigate(Screen.Blog.name) },
                        label = { Text("Blog") },
                        icon = { Text("📝") }
                    )
                }
                displayCurrentScreen(navController)
            }
        }
    }

    fun unicodeToString(unicode: Int): String{
        return unicode.toChar().toString()
    }
}


