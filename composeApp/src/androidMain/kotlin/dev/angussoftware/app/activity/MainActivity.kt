package dev.angussoftware.app.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import com.angussoftware.theming.compose.ui.theme.AngusTheme
import com.angussoftware.theming.compose.ui.theme.initializeThemeMode
import dev.angussoftware.app.preferences.initThemePreferences
import dev.angussoftware.app.preferences.loadThemePreferences
import dev.angussoftware.app.screens.AngusSoftwareAppScreen
import dev.angussoftware.app.theme.rememberAppThemeState

internal class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Initialize theme preference storage
        initThemePreferences(this)

        // Load saved preferences and initialize theme mode
        val prefs = loadThemePreferences()
        initializeThemeMode(prefs.themeMode)

        setContent {
            val themeState = rememberAppThemeState()
            AngusTheme(
                colorTheme = themeState.activeColorTheme,
            ) {
                AngusSoftwareAppScreen()
            }
        }
    }
}

@Preview(
    name = "ZFlip7 Front Screen",
    showBackground = true,
    widthDp = 422,
    heightDp = 332,
)
@Composable
private fun SmallAndroidPreview() {
    AngusTheme {
        AngusSoftwareAppScreen()
    }
}

@Preview(
    name = "Phone",
    showBackground = true,
    device = PHONE,
)
@Composable
private fun AppAndroidPreview() {
    AngusTheme {
        AngusSoftwareAppScreen()
    }
}

@Preview(
    name = "Pixel Tablet",
    showBackground = true,
    device = TABLET,
)
@Composable
private fun TabletAndroidPreview() {
    AngusTheme {
        AngusSoftwareAppScreen()
    }
}
