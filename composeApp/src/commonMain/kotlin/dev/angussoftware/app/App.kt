package dev.angussoftware.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    MaterialTheme {
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { currentScreen = Screen.Home },
                    // Highlight the button if it's the current screen
                    modifier = if (currentScreen == Screen.Home) Modifier.padding(bottom = 2.dp) else Modifier
                ) {
                    Text("Home")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { currentScreen = Screen.Projects },
                    modifier = if (currentScreen == Screen.Projects) Modifier.padding(bottom = 2.dp) else Modifier
                ) {
                    Text("Projects")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { currentScreen = Screen.Blog },
                    modifier = if (currentScreen == Screen.Blog) Modifier.padding(bottom = 2.dp) else Modifier
                ) {
                    Text("Blog")
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
    }
}
