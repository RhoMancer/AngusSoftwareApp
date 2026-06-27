package dev.angussoftware.app.installprompt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

internal const val PLAY_STORE_BANNER_TAG: String = "play_store_banner"
internal const val PWA_BANNER_TAG: String = "pwa_banner"
internal const val PLAY_STORE_INSTALL_BUTTON_TAG: String = "play_store_install_button"
internal const val PWA_INSTALL_BUTTON_TAG: String = "pwa_install_button"
internal const val BANNER_DISMISS_BUTTON_TAG: String = "banner_dismiss_button"

/**
 * Host composable that observes the InstallPromptController state and shows
 * the appropriate banner (Play Store or PWA) based on platform and state.
 */
@Composable
internal fun InstallPromptHost(controller: InstallPromptController) {
    var state by remember { mutableStateOf(controller.state) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(controller) {
        controller.setStateChangeListener { newState ->
            state = newState
        }
        onDispose {
            controller.setStateChangeListener { }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.isPlayStoreBannerVisible -> {
                PlayStoreInstallBanner(
                    onInstallClick = { controller.onPlayStoreInstallClick() },
                    onDismiss = { controller.onPlayStoreDismiss() },
                )
            }
            state.isPwaBannerVisible -> {
                PwaInstallBanner(
                    onInstallClick = {
                        coroutineScope.launch {
                            controller.onPwaInstallClick()
                        }
                    },
                    onDismiss = { controller.onPwaDismiss() },
                )
            }
        }
    }
}

/**
 * Banner prompting users on Android browsers to install the app from Play Store.
 */
@Composable
internal fun PlayStoreInstallBanner(
    onInstallClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InstallBannerCard(
        title = "Get our Android app",
        description = "Install from Google Play for the best experience",
        primaryButtonText = "Get it on Google Play",
        primaryButtonIcon = Icons.Default.Shop,
        onPrimaryClick = onInstallClick,
        onDismiss = onDismiss,
        testTag = PLAY_STORE_BANNER_TAG,
        primaryButtonTestTag = PLAY_STORE_INSTALL_BUTTON_TAG,
        modifier = modifier,
    )
}

/**
 * Banner prompting users on non-Android platforms to install the PWA.
 */
@Composable
internal fun PwaInstallBanner(
    onInstallClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InstallBannerCard(
        title = "Install this app",
        description = "Add to your home screen for quick access",
        primaryButtonText = "Install app",
        primaryButtonIcon = Icons.Default.Download,
        onPrimaryClick = onInstallClick,
        onDismiss = onDismiss,
        testTag = PWA_BANNER_TAG,
        primaryButtonTestTag = PWA_INSTALL_BUTTON_TAG,
        modifier = modifier,
    )
}

/**
 * Shared banner card component for both Play Store and PWA install prompts.
 */
@Composable
private fun InstallBannerCard(
    title: String,
    description: String,
    primaryButtonText: String,
    primaryButtonIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onPrimaryClick: () -> Unit,
    onDismiss: () -> Unit,
    testTag: String,
    primaryButtonTestTag: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag(testTag),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag(BANNER_DISMISS_BUTTON_TAG),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Not now")
                }
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier.testTag(primaryButtonTestTag),
                ) {
                    Icon(
                        imageVector = primaryButtonIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(primaryButtonText)
                }
            }
        }
    }
}
