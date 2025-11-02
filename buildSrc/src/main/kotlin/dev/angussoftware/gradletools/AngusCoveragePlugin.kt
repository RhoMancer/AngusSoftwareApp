package dev.angussoftware.gradletools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.register
import java.io.File
import javax.inject.Inject

/**
 * Angus Coverage Plugin — auto-registers a BranchCoverageGapsReportTask with sensible defaults.
 *
 * Usage (apply per module that produces a JaCoCo XML):
 *   plugins { id("dev.angussoftware.gradle-tools.coverage") }
 *
 * It registers a task named `androidBranchCoverageGaps` and wires it to the default JaCoCo XML location
 * (build/reports/jacoco/androidConnectedTest/report.xml). If a task named
 * `androidConnectedTestCoverageReport` exists, the gaps task depends on it.
 *
 * Inputs can be adjusted via the minimal `angusCoverage` extension or using -P branchCoverage* properties.
 */
class AngusCoveragePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Minimal extension for inputs that cannot be sensibly auto-detected everywhere.
        val ext = project.extensions.create(
            "angusCoverage",
            AngusCoverageExtension::class.java,
            project.objects,
        )

        // Defaults for extension
        ext.xmlReport.convention(project.layout.buildDirectory.file("reports/jacoco/androidConnectedTest/report.xml"))
        ext.sourceRoots.convention(
            listOf(
                project.layout.projectDirectory.dir("src/commonMain/kotlin").asFile.absolutePath,
                project.layout.projectDirectory.dir("src/androidMain/kotlin").asFile.absolutePath,
            ),
        )

        // Register the gaps report task with the same behavior as manual wiring in composeApp.
        project.tasks.register<BranchCoverageGapsReportTask>("androidBranchCoverageGaps") {
            group = "verification"
            description = "Parses JaCoCo XML to list exact lines with missed branches and suggests tests (optional AI)."

            // If a known Jacoco report task exists, depend on it so XML is present.
            project.tasks.findByName("androidConnectedTestCoverageReport")?.let { jacocoTask ->
                dependsOn(jacocoTask)
            }

            // Inputs
            xmlReport.set(ext.xmlReport)
            sourceRoots.set(ext.sourceRoots)

            // Flags
            branchCoverageEnabled.set(
                project.providers.gradleProperty("branchCoverageEnabled").map { it.equals("true", true) }.orElse(false),
            )
            aiEnabled.set(
                project.providers.gradleProperty("branchCoverageAiEnabled").map { it.equals("true", true) }.orElse(false),
            )
            ciEnabled.set(
                project.providers.gradleProperty("branchCoverageCiEnabled").map { it.equals("true", true) }.orElse(false),
            )

            // Context lines (default 5; -1 = whole file)
            contextLines.set(
                project.providers.gradleProperty("branchCoverageContextLines").map { it.toIntOrNull() ?: 5 }.orElse(5),
            )

            // Optional limits — set only if properties are provided
            if (project.providers.gradleProperty("branchCoverageTopNFiles").isPresent) {
                topNFiles.set(project.providers.gradleProperty("branchCoverageTopNFiles").map { it.toIntOrNull() ?: 0 })
            }
            if (project.providers.gradleProperty("branchCoverageFailIfMissedBranches").isPresent) {
                failIfMissedBranches.set(
                    project.providers.gradleProperty("branchCoverageFailIfMissedBranches").map { it.toIntOrNull() ?: 0 },
                )
            }
            if (project.providers.gradleProperty("branchCoverageFailIfMissedBranchesPerFile").isPresent) {
                failIfMissedBranchesPerFile.set(
                    project.providers.gradleProperty("branchCoverageFailIfMissedBranchesPerFile").map { it.toIntOrNull() ?: 0 },
                )
            }

            // AI config
            model.set(project.providers.gradleProperty("branchCoverageModel").orElse("gemma3"))
            ollamaCmd.set(
                project.providers.gradleProperty("branchCoverageOllamaCmd").orElse(Os.defaultOllamaCommand()),
            )
            timeoutSec.set(
                project.providers.gradleProperty("branchCoverageTimeoutSec").map { (it.toIntOrNull() ?: 60).coerceIn(5, 120) }.orElse(60),
            )
            maxPrompt.set(
                project.providers.gradleProperty("branchCoverageMaxPrompt").map { (it.toIntOrNull() ?: 6000).coerceIn(1000, 30000) }.orElse(6000),
            )
            redact.set(project.providers.gradleProperty("branchCoverageRedact").map { it.equals("true", true) }.orElse(true))

            // AI selection thresholds
            minCoveredBranchesForAi.set(
                project.providers.gradleProperty("branchCoverageMinCoveredBranchesForAi").map { it.toIntOrNull() ?: 1 }.orElse(1),
            )
            maxAiAnalyses.set(
                project.providers.gradleProperty("branchCoverageMaxAiAnalyses").map { (it.toIntOrNull() ?: 20).coerceAtLeast(1) }.orElse(20),
            )

            // Outputs live next to the JaCoCo XML
            val reportDirProvider: Provider<File> = xmlReport.map { it.asFile.parentFile }
            outputJson.set(project.layout.file(reportDirProvider.map { File(it, "branch-gaps.json") }))
            outputMd.set(project.layout.file(reportDirProvider.map { File(it, "branch-gaps.md") }))
            outputAiMd.set(project.layout.file(reportDirProvider.map { File(it, "branch-gaps-ai.md") }))
            outputMeta.set(project.layout.file(reportDirProvider.map { File(it, "branch-gaps.meta.json") }))

            // Global switch to skip the task entirely
            onlyIf { branchCoverageEnabled.get() }
        }
    }
}

abstract class AngusCoverageExtension @Inject constructor(objects: ObjectFactory) {
    /** JaCoCo XML report location used by the task. Default: build/reports/jacoco/androidConnectedTest/report.xml */
    abstract val xmlReport: org.gradle.api.file.RegularFileProperty

    /** Absolute source roots to search for files referenced by the JaCoCo XML (package/sourcefile). */
    abstract val sourceRoots: ListProperty<String>
}