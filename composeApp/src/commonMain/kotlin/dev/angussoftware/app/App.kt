package dev.angussoftware.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

// Define an enum class for different screens
enum class Screen {
    Home,
    Projects,
    Blog
}

// Home screen composable
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Home Page",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

// Projects screen composable
@Composable
fun ProjectsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Projects Page",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("My projects will be listed here")
    }
}

// Blog screen composable
@Composable
fun BlogScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Blog Page",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("My blog posts will be listed here")
    }
}

@Composable
@Preview
fun App() {
    // Create a navigation state to track the current screen
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    // Get window size information
    val windowInfo = currentWindowAdaptiveInfo()

    // Determine if we should use NavigationRail based on window width
    val useNavigationRail = !windowInfo.isCompact

    MaterialTheme {
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
                            .wrapContentHeight(Alignment.CenterVertically,
                                true),
                        containerColor = NavigationBarDefaults.containerColor
                    ) {
                        NavigationRailItem(
                            selected = currentScreen == Screen.Home,
                            onClick = { currentScreen = Screen.Home },
                            label = { Text("Home") },
                            icon = { Text("🏠") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == Screen.Projects,
                            onClick = { currentScreen = Screen.Projects },
                            label = { Text("Projects") },
                            icon = { Text("📋") }
                        )
                        NavigationRailItem(
                            selected = currentScreen == Screen.Blog,
                            onClick = { currentScreen = Screen.Blog },
                            label = { Text("Blog") },
                            icon = { Text("📝") }
                        )
                    }
                }

                // Content area - display the current screen
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Home -> HomeScreen()
                        Screen.Projects -> ProjectsScreen()
                        Screen.Blog -> BlogScreen()
                    }
                }
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
                        selected = currentScreen == Screen.Home,
                        onClick = { currentScreen = Screen.Home },
                        label = { Text("Home") },
                        icon = { Text("🏠") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Projects,
                        onClick = { currentScreen = Screen.Projects },
                        label = { Text("Projects") },
                        icon = { Text("📋") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Blog,
                        onClick = { currentScreen = Screen.Blog },
                        label = { Text("Blog") },
                        icon = { Text("📝") }
                    )
                }

                // Content area - display the current screen
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Home -> HomeScreen()
                        Screen.Projects -> ProjectsScreen()
                        Screen.Blog -> BlogScreen()
                    }
                }
            }
        }
    }
}
