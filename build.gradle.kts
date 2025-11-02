plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kover)
    id("dev.angussoftware.gradle-tools")
}

// Configure the bundle plugin to apply coverage to selected subprojects
angusToolsBundle {
    // Default to composeApp; add more modules here as needed
    includeProjects = listOf(":composeApp")
    autoWireCoverageDependsOn = true
}

// Centralized Kover aggregation for the whole build
kover {
    merge {
        subprojects()
    }
}
