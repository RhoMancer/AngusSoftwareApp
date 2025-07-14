package dev.angussoftware.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.DESKTOP
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import com.angussoftware.theming.compose.ui.theme.AngusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
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
fun SmallAndroidPreview() {
    App()
}

@Preview(
    name = "Phone",
    showBackground = true,
    device = PHONE
)
@Composable
fun AppAndroidPreview() {
    App()
}

@Preview(
    name = "Pixel Tablet",
    showBackground = true,
    device = TABLET
)
@Composable
fun TabletAndroidPreview() {
    App()
}