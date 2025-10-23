package dev.angussoftware.app.ui.utils

import android.graphics.Bitmap
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream

/**
 * Helper object for capturing DEVICE-LEVEL screenshots during instrumented tests.
 *
 * ## Why Device-Level Screenshots?
 *
 * This implementation uses Android's UiAutomation.takeScreenshot() API instead of Compose's
 * captureToImage() for several critical reasons:
 *
 * 1. **No Compose Dependencies**: Works at the Android system level, independent of Compose
 *    hierarchies, semantics trees, or UI framework state
 * 2. **Complete Screen Capture**: Captures everything visible including status bar, navigation
 *    bar, system overlays, and app content - exactly what users see
 * 3. **Reliability**: No timing issues with Compose initialization or "No compose hierarchies
 *    found" errors that plague Compose-level screenshot approaches
 * 4. **Cross-Framework Compatibility**: Works with any Android UI framework, not just Compose
 *
 * ## Storage Strategy
 *
 * Screenshots are saved to: `/sdcard/Download/dev.angussoftware.app/debug/screenshots/`
 *
 * This location is strategically chosen because:
 * - **Public Directory**: No WRITE_EXTERNAL_STORAGE permission required on Android 10+
 * - **Persistence**: Survives app uninstallation (critical since tests auto-uninstall apps)
 * - **ADB Access**: Easily accessible via `adb pull` commands from Gradle tasks
 * - **Namespaced**: App ID prevents conflicts with other projects
 * - **Build-Type Organized**: Separates debug/release screenshots
 *
 * ## Integration with Gradle Tasks
 *
 * This helper works in conjunction with three Gradle tasks defined in build.gradle.kts:
 * 1. `createScreenshotDirectory` - Creates device directory before tests
 * 2. `fetchScreenshots` - Pulls screenshots to local project after tests
 * 3. `clearScreenshots` - Cleans up device storage after fetching
 *
 * The complete automated workflow:
 * ```
 * connectedDebugAndroidTest
 * ├── dependsOn: createScreenshotDirectory
 * ├── during tests: captureDeviceScreenshot() calls save to device
 * ├── finalizedBy: fetchScreenshots (copies to composeApp/screenshots/)
 * └── finalizedBy: clearScreenshots (cleans device storage)
 * ```
 *
 * ## Usage in Tests
 *
 * ```kotlin
 * import dev.angussoftware.app.ui.utils.ScreenshotTestHelper.captureDeviceScreenshot
 *
 * @Test
 * fun myUiTest() = runComposeUiTest {
 *     setContent { MyScreen() }
 *     waitForIdle()
 *
 *     // Capture initial state
 *     captureDeviceScreenshot("01_initial_state")
 *
 *     // Perform UI action
 *     onNodeWithTag("button").performClick()
 *     waitForIdle()
 *
 *     // Capture result state
 *     captureDeviceScreenshot("02_after_click")
 *
 *     // Organize related screenshots
 *     captureDeviceScreenshot("error_case", subdirectory = "error_scenarios")
 * }
 * ```
 *
 * ## Developer Guidelines
 *
 * - **Naming**: Use descriptive sequential names (01_initial, 02_action, 03_result)
 * - **Organization**: Group related screenshots using subdirectory parameter
 * - **Timing**: Call after waitForIdle() to ensure UI has finished rendering
 * - **Error Handling**: Screenshots failures won't fail tests - they're supplementary
 * - **Review**: Screenshots appear in composeApp/screenshots/ after test completion
 *
 * ## For AI Agents
 *
 * Key integration points to understand:
 * - Screenshots captured during test execution are automatically retrieved
 * - No manual intervention required - fully automated workflow
 * - Storage paths must match between this helper and Gradle task configuration
 * - Device-level approach is more reliable than Compose-level for integration testing
 * - Error handling is graceful - screenshot failures don't break test execution
 */
object ScreenshotTestHelper {
    private const val TAG = "ScreenshotTest"
    private const val APP_ID = "dev.angussoftware.app"
    private const val BUILD_TYPE = "debug"
    private const val BASE_DIR = "/sdcard/Download"
    private const val SCREENSHOTS_DIR = "$BASE_DIR/$APP_ID/$BUILD_TYPE/screenshots"

    /**
     * Captures a device-level screenshot (entire screen).
     *
     * This captures everything visible on the device screen, not just Compose content.
     * Works independently of Compose hierarchies.
     *
     * Usage in tests:
     * ```kotlin
     * import dev.angussoftware.app.ui.utils.ScreenshotTestHelper.captureDeviceScreenshot
     *
     * @Test
     * fun myTest() = runComposeUiTest {
     *     setContent { MyScreen() }
     *     waitForIdle()
     *
     *     // Capture device screenshot
     *     captureDeviceScreenshot("01_initial_state")
     *
     *     // Perform action
     *     onNodeWithTag("button").performClick()
     *     waitForIdle()
     *
     *     // Capture another screenshot
     *     captureDeviceScreenshot("02_after_click")
     * }
     * ```
     *
     * @param fileName Name for the screenshot file (without .png extension)
     * @param subdirectory Optional subdirectory within screenshots/ for organization
     */
    fun captureDeviceScreenshot(
        fileName: String,
        subdirectory: String = "",
    ) {
        Log.d(TAG, "Capturing device screenshot: $fileName")

        try {
            // Get instrumentation for system-level operations
            val instrumentation = InstrumentationRegistry.getInstrumentation()

            // Capture device screenshot using UiAutomation (system level, no Compose dependency)
            val bitmap = instrumentation.uiAutomation.takeScreenshot()

            if (bitmap == null) {
                Log.e(TAG, "Failed to capture screenshot: bitmap is null")
                return
            }

            // Save to device
            saveToDevice(bitmap, fileName, subdirectory)

            Log.d(TAG, "Screenshot saved successfully: $fileName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture screenshot: $fileName", e)
            // Don't fail the test - just log the error
        }
    }

    private fun saveToDevice(
        bitmap: Bitmap,
        fileName: String,
        subdirectory: String,
    ) {
        val targetDir =
            if (subdirectory.isNotEmpty()) {
                File(SCREENSHOTS_DIR, subdirectory)
            } else {
                File(SCREENSHOTS_DIR)
            }

        if (!targetDir.exists()) {
            val created = targetDir.mkdirs()
            if (!created && !targetDir.exists()) {
                throw IllegalStateException("Failed to create directory: ${targetDir.absolutePath}")
            }
            Log.d(TAG, "Created directory: ${targetDir.absolutePath}")
        }

        val file = File(targetDir, "$fileName.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }

        Log.d(TAG, "Screenshot written to: ${file.absolutePath}")
    }
}
