package dev.angussoftware.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import angussoftwareapp.composeapp.generated.resources.Res
import angussoftwareapp.composeapp.generated.resources.nav_blog
import angussoftwareapp.composeapp.generated.resources.nav_home
import angussoftwareapp.composeapp.generated.resources.nav_projects
import dev.angussoftware.app.ui.utils.rememberedCurrentWindowAdaptiveInfo
import dev.angussoftware.app.navigation.DefaultNavigationBarHeight
import dev.angussoftware.app.navigation.LocalNavigationBarHeight
import dev.angussoftware.app.navigation.displayCurrentScreen
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.testTag


// Define an enum class for different screens
internal enum class Screen {
    Home,
    Projects,
    Blog,
    BlogPost
}

// Test tags for UI testing
internal const val NAV_BAR_TEST_TAG = "BottomNavigationBar"
internal const val NAV_RAIL_TEST_TAG = "NavigationRail"
internal const val NAV_ITEM_HOME_TAG = "NavItem_Home"
internal const val NAV_ITEM_PROJECTS_TAG = "NavItem_Projects"
internal const val NAV_ITEM_BLOG_TAG = "NavItem_Blog"

// Screen content tags for destination verification in tests
internal const val HOME_SCREEN_TEST_TAG = "HomeScreen"
internal const val PROJECTS_SCREEN_TEST_TAG = "ProjectsScreen"
internal const val BLOG_SCREEN_TEST_TAG = "BlogScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
internal fun AngusSoftwareAppScreen(navController: NavHostController = rememberNavController()) {
    // Get window size information
    val windowInfo = rememberedCurrentWindowAdaptiveInfo()

    // Remember the navigation bar height state
    val navigationBarHeightState = remember { mutableStateOf(DefaultNavigationBarHeight) }
    val density = LocalDensity.current
    
    // Observe the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if we should use NavigationRail based on window width
    val isCompactScreen = windowInfo.isCompact

    if (isCompactScreen) {
        // Provide the navigation bar height through CompositionLocal
        CompositionLocalProvider(LocalNavigationBarHeight provides navigationBarHeightState.value) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            // Measure the height of the NavigationBar
                            .onGloballyPositioned { coordinates ->
                                // Convert pixels to dp and update the state
                                val heightInDp = with(density) { coordinates.size.height.toDp() }
                                if (heightInDp > 0.dp) {
                                    navigationBarHeightState.value = heightInDp
                                }
                            }
                            .testTag(NAV_BAR_TEST_TAG)
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == Screen.Home.name,
                            onClick = { navController.navigate(Screen.Home.name) },
                            label = { Text(stringResource(Res.string.nav_home)) },
                            icon = { Icon(
                                Icons.Default.Home,
                                contentDescription = stringResource(Res.string.nav_home)
                            ) },
                            modifier = Modifier.testTag(NAV_ITEM_HOME_TAG)
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Projects.name,
                            onClick = { navController.navigate(Screen.Projects.name) },
                            label = { Text(stringResource(Res.string.nav_projects)) },
                            icon = { Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = stringResource(Res.string.nav_projects)
                            ) },
                            modifier = Modifier.testTag(NAV_ITEM_PROJECTS_TAG)
                        )
                        NavigationBarItem(
                            selected = currentRoute == Screen.Blog.name,
                            onClick = { navController.navigate(Screen.Blog.name) },
                            label = { Text(stringResource(Res.string.nav_blog)) },
                            icon = { Icon(
                                Icons.Default.Create,
                                contentDescription = stringResource(Res.string.nav_blog)
                            ) },
                            modifier = Modifier.testTag(NAV_ITEM_BLOG_TAG)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()) {
                // Layout with NavigationBar for small screens
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    displayCurrentScreen(navController)
                }
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) {
            // Layout with NavigationRail for medium and larger screens
            Row(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    // Navigation rail
                    NavigationRail(
                        Modifier
                            .align(Alignment.CenterVertically)
                            .wrapContentHeight(
                                Alignment.CenterVertically,
                                true
                            )
                            .testTag(NAV_RAIL_TEST_TAG),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        NavigationRailItem(
                            selected = currentRoute == Screen.Home.name,
                            onClick = { navController.navigate(Screen.Home.name) },
                            label = { Text(stringResource(Res.string.nav_home)) },
                            icon = { Icon(
                                Icons.Default.Home,
                                contentDescription = stringResource(Res.string.nav_home)
                            ) },
                            modifier = Modifier.padding(horizontal = 16.dp).testTag(NAV_ITEM_HOME_TAG)
                        )
                        NavigationRailItem(
                            selected = currentRoute == Screen.Projects.name,
                            onClick = { navController.navigate(Screen.Projects.name) },
                            label = { Text(stringResource(Res.string.nav_projects)) },
                            icon = { Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = stringResource(Res.string.nav_projects)
                            ) },
                            modifier = Modifier.padding(horizontal = 16.dp).testTag(NAV_ITEM_PROJECTS_TAG)
                        )
                        NavigationRailItem(
                            selected = currentRoute == Screen.Blog.name,
                            onClick = { navController.navigate(Screen.Blog.name) },
                            label = { Text(stringResource(Res.string.nav_blog)) },
                            icon = { Icon(
                                Icons.Default.Create,
                                contentDescription = stringResource(Res.string.nav_blog)
                            ) },
                            modifier = Modifier.padding(horizontal = 16.dp).testTag(NAV_ITEM_BLOG_TAG)
                        )
                    }
                }
                displayCurrentScreen(navController)
            }
        }
    }
}




