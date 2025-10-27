/**
 * ============================================================================
 * SCREENSHOT TESTING SYSTEM FOR INSTRUMENTED TESTS
 * ============================================================================
 *
 * This section implements an automated screenshot capture and retrieval system
 * for Android instrumented tests. The system provides visual verification of
 * UI states to complement functional test assertions.
 *
 * ## System Overview
 *
 * The screenshot system consists of:
 * 1. **Capture**: ScreenshotTestHelper.kt captures device-level screenshots during tests
 * 2. **Storage**: Screenshots saved to public device directory (survives app uninstall)
 * 3. **Retrieval**: Gradle tasks automatically fetch screenshots to local project
 * 4. **Cleanup**: Device storage cleaned after successful retrieval
 *
 * ## Automated Workflow
 *
 * When you run `./gradlew :composeApp:connectedDebugAndroidTest`, this happens:
 *
 * ```
 * 1. createScreenshotDirectory    → Creates /sdcard/Download/.../screenshots/ on device
 * 2. connectedDebugAndroidTest    → Runs all instrumented tests
 *    └─ During tests: captureDeviceScreenshot() saves PNG files to device
 * 3. fetchScreenshots            → Pulls screenshots to composeApp/screenshots/
 * 4. clearScreenshots            → Removes screenshots from device storage
 * ```
 *
 * ## Configuration Variables
 *
 * The screenshot helper now computes the device path dynamically at runtime using the
 * installed app's package (applicationId) and the active build type. To keep Gradle tasks
 * aligned without hard-coding, this script derives the device path from two Gradle properties
 * with sensible defaults:
 * - screenshotAppId: defaults to the app's current applicationId (dev.angussoftware.app)
 * - screenshotBuildType: defaults to "debug"
 *
 * You can override them when running tasks, for example:
 *   gradlew :composeApp:connectedDebugAndroidTest -PscreenshotBuildType=debug
 *   gradlew :composeApp:fetchScreenshots -PscreenshotAppId=com.example.debug -PscreenshotBuildType=debug
 */

val appId = (findProperty("screenshotAppId") as String?) ?: "dev.angussoftware.app" // Application ID for namespacing. Override with -PscreenshotAppId=com.example
val buildType = (findProperty("screenshotBuildType") as String?) ?: "debug" // Build type (debug/release). Override with -PscreenshotBuildType=release
val screenshotsDeviceDir = "/sdcard/Download/$appId/$buildType/screenshots" // Device storage path
val screenshotsLocalDir = file("screenshots") // Local project directory for screenshots

/**
 * Task 1: Create Screenshot Directory
 *
 * Creates the screenshot storage directory on the connected Android device before tests run.
 * This ensures screenshots have a place to be saved during test execution.
 *
 * - **Timing**: Runs BEFORE connectedDebugAndroidTest (via dependsOn)
 * - **Command**: `adb shell mkdir -p /sdcard/Download/dev.angussoftware.app/debug/screenshots`
 * - **Error Handling**: Ignores errors if directory already exists (idempotent)
 * - **Requirements**: Connected Android device/emulator with ADB access
 */
tasks.register<Exec>("createScreenshotDirectory") {
    group = "verification"
    description = "Creates screenshot directory on connected Android device"

    // Capture script variables as task-local values for configuration cache compatibility
    val deviceDir = screenshotsDeviceDir

    commandLine("adb", "shell", "mkdir", "-p", deviceDir)

    doFirst {
        println("Creating screenshot directory on device: $deviceDir")
    }

    doLast {
        println("Screenshot directory created successfully")
    }

    // Ignore errors if directory already exists - makes this task idempotent
    isIgnoreExitValue = true
}

/**
 * Task 2: Fetch Screenshots from Device
 *
 * Transfers all captured screenshots from the Android device to the local project directory.
 * This makes screenshots available for manual review and version control.
 *
 * - **Timing**: Runs AFTER connectedDebugAndroidTest completes (via finalizedBy)
 * - **Command**: `adb pull /sdcard/Download/.../screenshots/ ./composeApp/screenshots/`
 * - **Local Destination**: `composeApp/screenshots/` (relative to project root)
 * - **Directory Creation**: Automatically creates local directory if missing
 * - **Error Handling**: Ignores errors if no screenshots exist (tests may not capture any)
 * - **Persistence**: Screenshots remain in local project after device cleanup
 *
 * Note: This task runs even if tests fail, ensuring screenshots are retrieved for debugging.
 */
tasks.register<Exec>("fetchScreenshots") {
    group = "verification"
    description = "Fetches screenshots from device to local project directory"

    // Capture script variables as task-local values for configuration cache compatibility
    val deviceDir = screenshotsDeviceDir
    val localDir = screenshotsLocalDir

    commandLine("adb", "pull", deviceDir, localDir.absolutePath)

    doFirst {
        println("Fetching screenshots from device...")
        println("Device path: $deviceDir")
        println("Local path: ${localDir.absolutePath}")

        // Create local directory if it doesn't exist
        if (!localDir.exists()) {
            localDir.mkdirs()
            println("Created local screenshots directory")
        }
    }

    doLast {
        println("Screenshots fetched successfully to: ${localDir.absolutePath}")
    }

    // Don't fail if no screenshots exist - some tests might not capture screenshots
    isIgnoreExitValue = true
}

/**
 * Task 3: Clear Screenshots from Device
 *
 * Removes all screenshots from device storage to free up space and prevent accumulation.
 * This cleanup runs after screenshots have been successfully fetched to local storage.
 *
 * - **Timing**: Runs AFTER fetchScreenshots completes (via finalizedBy chain)
 * - **Command**: `adb shell rm -rf /sdcard/Download/dev.angussoftware.app/debug/screenshots`
 * - **Safety**: Only runs after fetchScreenshots, ensuring screenshots are saved locally first
 * - **Error Handling**: Ignores errors if directory doesn't exist (already cleaned)
 * - **Storage Management**: Prevents device storage bloat from accumulated test screenshots
 *
 * This ensures a clean slate for further test runs while preserving screenshots locally.
 */
tasks.register<Exec>("clearScreenshots") {
    group = "verification"
    description = "Removes screenshots from device storage"

    // Capture script variables as task-local values for configuration cache compatibility
    val deviceDir = screenshotsDeviceDir

    commandLine("adb", "shell", "rm", "-rf", deviceDir)

    doFirst {
        println("Clearing screenshots from device: $deviceDir")
    }

    doLast {
        println("Device screenshots cleared")
    }

    // Ignore errors if directory doesn't exist or is already clean
    isIgnoreExitValue = true
}

/**
 * ============================================================================
 * TASK ORCHESTRATION & DEPENDENCY CHAIN
 * ============================================================================
 *
 * This section configures the automatic execution of screenshot tasks around
 * instrumented tests. The orchestration ensures proper timing and error resilience.
 *
 * ## Task Execution Order
 *
 * ```
 * ./gradlew :composeApp:connectedDebugAndroidTest
 * │
 * ├─ dependsOn: createScreenshotDirectory
 * │  └─ Creates /sdcard/Download/.../screenshots/ on device
 * │
 * ├─ connectedDebugAndroidTest (main task)
 * │  ├─ Installs test APK with auto-granted permissions
 * │  ├─ Runs all instrumented tests
 * │  └─ During tests: captureDeviceScreenshot() saves to device
 * │
 * ├─ finalizedBy: fetchScreenshots
 * │  └─ Pulls screenshots to composeApp/screenshots/
 * │
 * └─ finalizedBy: clearScreenshots
 *    └─ Removes screenshots from device storage
 * ```
 *
 * ## Key Orchestration Features
 *
 * - **Dependency**: createScreenshotDirectory runs BEFORE tests (ensures directory exists)
 * - **Finalization**: fetchScreenshots runs AFTER tests (even if tests fail)
 * - **Chain**: clearScreenshots runs AFTER fetchScreenshots (safe cleanup)
 * - **Error Resilience**: Each task uses isIgnoreExitValue = true for graceful handling
 * - **Single Command**: All tasks execute with one command (no manual intervention)
 *
 * ## Manual Task Execution (Optional)
 *
 * You can run individual tasks for debugging:
 *
 * ```bash
 * # Create directory only
 * ./gradlew createScreenshotDirectory
 *
 * # Fetch existing screenshots
 * ./gradlew fetchScreenshots
 *
 * # Clean device storage
 * ./gradlew clearScreenshots
 *
 * # Run tests without screenshot automation
 * ./gradlew connectedDebugAndroidTest --continue
 * ```
 *
 * ## Troubleshooting
 *
 * **No screenshots captured:**
 * 1. Check that tests call captureDeviceScreenshot()
 * 2. Verify device has permissions (installation block includes -g flag)
 * 3. Ensure device/emulator is connected: `adb devices`
 *
 * **Screenshots not fetched:**
 * 1. Check device path exists: `adb shell ls -la /sdcard/Download/${appId}/${buildType}/screenshots/`
 * 2. Manually pull: `adb pull /sdcard/Download/${appId}/${buildType}/screenshots/ ./composeApp/screenshots/`
 * 3. Check ADB connection and permissions
 *
 * **Task failures:**
 * 1. All screenshot tasks use isIgnoreExitValue = true (won't fail builds)
 * 2. Check console output for specific error messages
 * 3. Verify ADB is in PATH and device is authorized
 *
 * **Configuration mismatch:**
 * 1. Ensure the runtime path (computed in ScreenshotTestHelper) and these Gradle properties resolve to the same device directory.
 * 2. If needed, override with -PscreenshotAppId=<finalApplicationId> and/or -PscreenshotBuildType=<variant> when running tasks.
 * 3. Prefer running connectedDebugAndroidTest so the defaults (debug + app's applicationId) are correct.
 */
tasks.matching { it.name == "connectedDebugAndroidTest" }.configureEach {
    // BEFORE tests: create directory (ensures screenshots have storage location)
    dependsOn("createScreenshotDirectory")

    // AFTER tests: fetch screenshots and clean device (runs even if tests fail)
    finalizedBy("fetchScreenshots")
}

tasks.matching { it.name == "fetchScreenshots" }.configureEach {
    // AFTER fetching: clean device storage (maintains device hygiene)
    finalizedBy("clearScreenshots")
}
