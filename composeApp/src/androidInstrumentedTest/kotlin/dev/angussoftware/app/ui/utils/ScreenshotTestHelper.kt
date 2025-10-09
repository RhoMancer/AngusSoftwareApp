package dev.angussoftware.app.ui.utils

import android.graphics.Bitmap
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.File
import java.io.FileOutputStream

/**
 * Helper object for capturing DEVICE-LEVEL screenshots during instrumented tests.
 * 
 * Uses UiAutomation.takeScreenshot() to capture the entire screen at the system level,
 * which doesn't require Compose hierarchies.
 * 
 * Screenshots are saved to public Download directory:
 * /sdcard/Download/dev.angussoftware.app/debug/screenshots/
 * 
 * This directory:
 * - Persists after the test app uninstalls (which happens automatically after tests)
 * - Doesn't require WRITE_EXTERNAL_STORAGE permission on any Android version
 * - Allows the fetchScreenshots Gradle task to retrieve screenshots
 * 
 * Screenshots are automatically copied to: composeApp/screenshots/
 */
object ScreenshotTestHelper {
    
    private const val TAG = "ScreenshotTest"
    private const val APP_ID = "dev.angussoftware.app"
    private const val BUILD_TYPE = "debug"
    private const val BASE_DIR = "/sdcard/Download"
    private val SCREENSHOTS_DIR = "$BASE_DIR/$APP_ID/$BUILD_TYPE/screenshots"
    
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
        subdirectory: String = ""
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
    
    private fun saveToDevice(bitmap: Bitmap, fileName: String, subdirectory: String) {
        val targetDir = if (subdirectory.isNotEmpty()) {
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
