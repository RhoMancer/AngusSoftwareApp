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

// --- Build Failure analysis policy for composeApp --------------------------------------------
// This module can run build failure analysis by default and avoid the Gradle configuration cache
// when the analysis is enabled. The analysis relies on an end-of-build listener that Gradle skips when
// the configuration cache is requested, so diagnostics would not run under the cache.
//
// Defaults (see gradle.properties):
// - buildFailureEnabled=true  -> enables analysis by default so failed builds print a diagnosis
// - org.gradle.configuration-cache=false -> disables the configuration cache by default for reliability
//
// How to temporarily override per run:
// - Disable analysis for this run:
//     gradlew <tasks> -PbuildFailureEnabled=false
// - OR allow configuration cache (diagnostics will be skipped):
//     gradlew <tasks> --configuration-cache -PbuildFailureEnforceNoConfigCache=false
//
// Guard: fail fast if configuration cache is requested while enforcement is on.
val buildFailureEnabledForRun = (providers.gradleProperty("buildFailureEnabled").orNull ?: "true").equals("true", ignoreCase = true)
val buildFailureEnforceNoConfigCache =
    (
        providers
            .gradleProperty(
                "buildFailureEnforceNoConfigCache",
            ).orNull ?: "true"
    ).equals("true", ignoreCase = true)
if (buildFailureEnabledForRun && buildFailureEnforceNoConfigCache && gradle.startParameter.isConfigurationCacheRequested) {
    throw org.gradle.api.GradleException(
        "composeApp: Configuration cache requested but build-failure analysis is enabled. Rerun with --no-configuration-cache, " +
            "or override with -PbuildFailureEnforceNoConfigCache=false, or disable analysis with -PbuildFailureEnabled=false. " +
            "Note: under configuration cache Gradle skips end-of-build listeners, so analysis will not run.",
    )
}
// ------------------------------------------------------------------------------------------------

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

apply(from = "gradle/screenshot-tasks.gradle.kts")
apply(from = "gradle/coverage-tasks.gradle.kts")
apply(from = "gradle/build-failure-demo-task.gradle.kts")
