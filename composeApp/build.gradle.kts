import com.angussoftware.gradletools.coverage.ai
import com.angussoftware.gradletools.coverage.gaps
import com.angussoftware.gradletools.coverage.selection
import com.angussoftware.gradletools.coverage.thresholds
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.angusGradleTools.coverage)
}

// Centralized App ID to avoid duplicated strings across android block
val appId = "dev.angussoftware.app"

// Load keystore properties for release signing
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

angusCoverage {
    xmlReport.set(layout.buildDirectory.file("reports/jacoco/androidConnectedTest/report.xml"))
    sourceRoots.set(
        listOf(
            projectDir.resolve("src/commonMain/kotlin").absolutePath,
            projectDir.resolve("src/androidMain/kotlin").absolutePath,
        ),
    )

    gaps {
        contextLines.set(5) // -1 = whole file
        // topNFiles.set(20)

        thresholds {
            //            maxTotalMissedBranches.set(0)
            //            maxMissedBranchesPerFile.set(0)
        }

        ai {
            enabled.set(true)
            allowOnCi.set(false)
            model.set("qwen3-coder:30b")
            timeoutSec.set(120)
            maxPrompt.set(6000)
            redact.set(true)

            selection {
                minCoveredBranches.set(1)
                maxAnalyses.set(20)
            }
        }
    }

    this.unifiedReport.enabled.set(true)
    this.unifiedReport.koverExclusions.set(listOf<String>())
    this.unifiedReport.jacocoExclusions.set(
        listOf(
            "**/R.class",
            "**/R\\$*.class",
            "**/*R*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "**/generated/**",
            "**/*ComposableSingletons*",
            "**/androidx/**",
            "**/android/**",
            "**/kotlin/**",
            "**/kotlinx/**",
            "**/org/koin/**",
            "**/com/arkivanov/**",
            "**/app/cash/turbine/**",
        ),
    )
}

// Kover coverage verification thresholds
// Current: 9.5% LINE / 18.8% BRANCH — set floor just above current to prevent regressions
kover {
    reports {
        verify {
            rule("Coverage verification") {
                minBound(15, CoverageUnit.BRANCH)
                minBound(8, CoverageUnit.LINE)
            }
        }
    }
}

// Chain coverage verification to check task
tasks.named("check").configure {
    dependsOn("koverVerify")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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
            implementation(libs.androidx.lifecycle.viewmodelCompose)
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
    namespace = appId
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = appId
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        // Versioning is sourced from gradle.properties
        versionName = project.version.toString()
        versionCode =
            providers
                .gradleProperty("android.versionCode")
                .orElse("1")
                .map(String::toInt)
                .get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        register("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
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
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// --- Web/Wasm: write human-readable version into resources for display ---
// This task generates `src/wasmJsMain/resources/version.txt` at BUILD TIME (not when bump tasks run).
// The file is regenerated automatically whenever the Wasm build runs, including during CI (GitHub Actions).
// When you run `releasePatch/Minor/Major`, only `gradle.properties` is updated immediately;
// `version.txt` will reflect the new version once the build executes (locally or in CI).
val writeWebVersion by tasks.registering {
    val outFile =
        project.layout.projectDirectory
            .file("src/wasmJsMain/resources/version.txt")
            .asFile
    inputs.property("version", project.version.toString())
    outputs.file(outFile)
    doLast {
        val content =
            buildString {
                appendLine("# AUTO-GENERATED at build time from gradle.properties")
                appendLine("# Do not edit manually — changes will be overwritten by CI/local builds.")
                appendLine("# To bump the version, run: ./gradlew releasePatch (or releaseMinor/releaseMajor)")
                append(project.version.toString())
            }
        outFile.writeText(content)
    }
}

// Some Gradle/KMP versions don't expose `processWasmJsMainResources`; hook into the
// target-level resources task instead when it is realized.
tasks.configureEach {
    if (name == "wasmJsProcessResources") {
        dependsOn(writeWebVersion)
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.androidx.uiautomator)

    // Force Espresso 3.7.0 to fix NoSuchMethodException on Android 16 (API 36).
    // espresso-core 3.5.x/3.6.x uses InputManager.getInstance() which was removed in API 36.
    configurations.configureEach {
        resolutionStrategy {
            force("androidx.test.espresso:espresso-core:3.7.0")
            force("androidx.test.espresso:espresso-idling-resource:3.7.0")
        }
    }
}
