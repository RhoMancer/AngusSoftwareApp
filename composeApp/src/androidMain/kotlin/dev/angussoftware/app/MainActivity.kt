package dev.angussoftware.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import com.angussoftware.theming.compose.ui.theme.AngusTheme

internal class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        setContent {
            AngusTheme {
                AngusSoftwareAppScreen()
            }
        }
    }
}

@Preview(
    name = "ZFlip7 Front Screen",
    showBackground = true,
    widthDp = 422,
    heightDp = 332
)
@Composable
private fun SmallAndroidPreview() {
    AngusSoftwareAppScreen()
}

@Preview(
    name = "Phone",
    showBackground = true,
    device = PHONE
)
@Composable
private fun AppAndroidPreview() {
    AngusSoftwareAppScreen()
}

@Preview(
    name = "Pixel Tablet",
    showBackground = true,
    device = TABLET
)
@Composable
private fun TabletAndroidPreview() {
    AngusSoftwareAppScreen()
}
