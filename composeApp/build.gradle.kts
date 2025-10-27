import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kover)
}

kover {
    merge {
        subprojects()
    }
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        dependencies {
            androidTestImplementation(libs.androidx.ui.test.junit4.android)
            debugImplementation(libs.androidx.ui.test.manifest)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(rootDirPath)
                                add(projectDirPath)
                            }
                    }
            }
        }
        binaries.executable()
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.angusSoftware.theming.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.angusSoftware.theming.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.angusSoftware.theming.compose)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
    }
}

android {
    namespace = "dev.angussoftware.app"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "dev.angussoftware.app"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Auto-grant permissions during test installation
    installation {
        installOptions += listOf("-g", "-r")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            // Enable code coverage for Android instrumented tests (connectedDebugAndroidTest)
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    androidTestImplementation(libs.androidx.uiautomator)
    kover(project(":composeApp"))
}

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
 * These variables must match the configuration in ScreenshotTestHelper.kt:
 *
 * Screenshot system configuration - MUST match ScreenshotTestHelper.kt constants
 *
 */

val appId = "dev.angussoftware.app" // Application ID for namespacing
val buildType = "debug" // Build type (debug/release)
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
 * 1. Check device path exists: `adb shell ls -la /sdcard/Download/dev.angussoftware.app/debug/screenshots/`
 * 2. Manually pull: `adb pull /sdcard/Download/dev.angussoftware.app/debug/screenshots/ ./composeApp/screenshots/`
 * 3. Check ADB connection and permissions
 *
 * **Task failures:**
 * 1. All screenshot tasks use isIgnoreExitValue = true (won't fail builds)
 * 2. Check console output for specific error messages
 * 3. Verify ADB is in PATH and device is authorized
 *
 * **Configuration mismatch:**
 * 1. Ensure appId and buildType match ScreenshotTestHelper.kt constants
 * 2. Verify SCREENSHOTS_DIR path construction is identical
 * 3. Check that APP_ID = "dev.angussoftware.app" matches applicationId
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

// ==============================
// CODE COVERAGE TASKS
// ==============================

// Generates JaCoCo report for Android instrumented tests (connectedDebugAndroidTest)
tasks.register<JacocoReport>("androidConnectedTestCoverageReport") {
    group = "verification"
    description = "Generates JaCoCo HTML/XML coverage report for connectedDebugAndroidTest (instrumented UI tests)."
    // Ensure device tests run first
    dependsOn("connectedDebugAndroidTest")
    // Also ensure debug compilation tasks that produce class outputs are complete
    dependsOn("compileDebugKotlinAndroid")
    dependsOn("compileDebugJavaWithJavac")
    // Provide an ordering constraint relative to tasks whose outputs we do NOT use
    mustRunAfter("compileDebugUnitTestKotlinAndroid", "compileReleaseKotlinAndroid")

    // Execution data produced by on-device JaCoCo agent
    val executionDataFiles =
        fileTree(buildDir) {
            include(
                "outputs/**/connected/**/*.ec",
                "outputs/**/coverage.ec",
                "outputs/code_coverage/**/**/*.ec",
                "outputs/connected_android_test_code_coverage/**/**/*.ec",
            )
        }
    executionData(executionDataFiles)

    // Class files to analyze (Kotlin/Compose + Java) — restrict to DEBUG variants to avoid
    // implicitly consuming unitTest or release outputs.
    val tmpKotlinDebug = fileTree("$buildDir/tmp/kotlin-classes/debug") { include("**/*.class") }
    val tmpKotlinAndroidDebug = fileTree("$buildDir/tmp/kotlin-classes/androidDebug") { include("**/*.class") }
    val javacDebug = fileTree("$buildDir/intermediates/javac/debug/classes") { include("**/*.class") }

    val allClassDirs =
        files(tmpKotlinDebug, tmpKotlinAndroidDebug, javacDebug)
            .asFileTree
            .matching {
                exclude(
                    "**/R.class",
                    "**/R$*.class",
                    "**/*R*.class",
                    "**/BuildConfig.*",
                    "**/Manifest*.*",
                    "**/*Test*.*",
                    // Exclude generated Compose resources (some are already instrumented by AGP)
                    "**/composeapp/generated/**",
                    "**/generated/**",
                    "**/generated/resources/**",
                    "**/generated/resources/*_commonMainKt*",
                    // Exclude Compose singletons that may be pre-instrumented by AGP
                    "**/*ComposableSingletons*",
                    "**/activity/ComposableSingletons*",
                    // Exclude Compose previews (not part of runtime code)
                    "**/*Preview*"
                )
            }

    classDirectories.setFrom(allClassDirs)
    sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/androidMain/kotlin"))

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/androidConnectedTest/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/androidConnectedTest/report.xml"))
    }

    doLast {
        // Print resolved report locations after successful execution
        val htmlDir = reports.html.outputLocation.get().asFile
        val htmlIndex = htmlDir.resolve("index.html")
        val xmlFile = reports.xml.outputLocation.get().asFile

        if (htmlIndex.exists()) {
            println("Android instrumented coverage HTML: ${htmlIndex.absolutePath}")
        } else {
            println("Android instrumented coverage HTML expected at: ${htmlIndex.absolutePath} (file not found)")
        }

        if (xmlFile.exists()) {
            println("Android instrumented coverage XML: ${xmlFile.absolutePath}")
        } else {
            println("Android instrumented coverage XML expected at: ${xmlFile.absolutePath} (file not found)")
        }
    }
}

// Lifecycle task to run Android instrumented tests and produce the JaCoCo report
tasks.register("androidInstrumentedCoverage") {
    group = "verification"
    description = "Runs instrumented tests and generates the Android JaCoCo coverage report."
    dependsOn("androidConnectedTestCoverageReport")
}

// Combined lifecycle task for both unit (Kover) and instrumented coverage
// Usage: gradlew :composeApp:fullCoverageReport
tasks.register("fullCoverageReport") {
    group = "verification"
    description = "Generates unit (Kover) and Android instrumented coverage reports for this module."
    dependsOn("koverHtmlReport")
    dependsOn("androidInstrumentedCoverage")
}
