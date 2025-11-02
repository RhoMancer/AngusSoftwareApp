plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("angusFailureAnalysis") {
            id = "dev.angussoftware.gradle-tools.failure-analysis"
            implementationClass = "dev.angussoftware.gradletools.AngusFailureAnalysisPlugin"
            displayName = "Angus Gradle Tools — Failure Analysis"
            description = "End-of-build failure analysis via local Ollama CLI (part of Angus Gradle Tools)."
        }
        register("angusCoverage") {
            id = "dev.angussoftware.gradle-tools.coverage"
            implementationClass = "dev.angussoftware.gradletools.AngusCoveragePlugin"
            displayName = "Angus Gradle Tools — Coverage"
            description = "Auto-registers a BranchCoverageGapsReportTask with sensible defaults for JaCoCo XML analysis."
        }
        register("angusToolsBundle") {
            id = "dev.angussoftware.gradle-tools"
            implementationClass = "dev.angussoftware.gradletools.AngusToolsBundlePlugin"
            displayName = "Angus Gradle Tools — Bundle"
            description = "Applies failure-analysis at root and coverage to selected subprojects via a simple extension."
        }
    }
}
