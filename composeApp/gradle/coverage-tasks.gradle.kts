import org.gradle.testing.jacoco.tasks.JacocoReport
import dev.angussoftware.aidoctor.BranchCoverageDoctorTask

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


// Branch coverage doctor over the JaCoCo XML
val branchDoctorEnabledProvider = providers.gradleProperty("branchDoctorEnabled").map { it.equals("true", ignoreCase = true) }.orElse(false)

val androidJacocoTask = tasks.named<JacocoReport>("androidConnectedTestCoverageReport")

tasks.register<BranchCoverageDoctorTask>("androidCoverageBranchDoctor") {
    group = "verification"
    description = "Parses JaCoCo XML to list exact lines with missed branches and suggests tests (optional AI)."

    // Ensure the XML exists
    dependsOn(androidJacocoTask)

    // Inputs: point to the XML produced by the Jacoco task (use known path to avoid taskless output provider issues)
    val jacocoReportXml = layout.buildDirectory.file("reports/jacoco/androidConnectedTest/report.xml")
    xmlReport.set(jacocoReportXml)

    // Where to find sources mapped by <package>/<sourcefile>
    sourceRoots.set(
        listOf(
            layout.projectDirectory.dir("src/commonMain/kotlin").asFile.absolutePath,
            layout.projectDirectory.dir("src/androidMain/kotlin").asFile.absolutePath,
        )
    )

    // Core toggles (defaults: disabled, AI off)
    branchDoctorEnabled.set(branchDoctorEnabledProvider)
    aiEnabled.set(providers.gradleProperty("branchDoctorAiEnabled").map { it.equals("true", true) }.orElse(false))
    ciEnabled.set(providers.gradleProperty("branchDoctorCiEnabled").map { it.equals("true", true) }.orElse(false))

    // Context lines (default 5; -1 = whole file)
    contextLines.set(providers.gradleProperty("branchDoctorContextLines").map { it.toIntOrNull() ?: 5 }.orElse(5))

    // Optional limits — set only if properties are provided
    if (providers.gradleProperty("branchDoctorTopNFiles").isPresent) {
        topNFiles.set(providers.gradleProperty("branchDoctorTopNFiles").map { it.toIntOrNull() ?: 0 })
    }
    if (providers.gradleProperty("branchDoctorFailIfMissedBranches").isPresent) {
        failIfMissedBranches.set(providers.gradleProperty("branchDoctorFailIfMissedBranches").map { it.toIntOrNull() ?: 0 })
    }
    if (providers.gradleProperty("branchDoctorFailIfMissedBranchesPerFile").isPresent) {
        failIfMissedBranchesPerFile.set(providers.gradleProperty("branchDoctorFailIfMissedBranchesPerFile").map { it.toIntOrNull() ?: 0 })
    }

    // AI config (defaults mirror AiDoctor style)
    model.set(providers.gradleProperty("branchDoctorModel").orElse("gemma3"))
    val defaultOllama = if (System.getProperty("os.name").lowercase().contains("win")) "ollama.exe" else "ollama"
    ollamaCmd.set(providers.gradleProperty("branchDoctorOllamaCmd").orElse(defaultOllama))
    timeoutSec.set(providers.gradleProperty("branchDoctorTimeoutSec").map { (it.toIntOrNull() ?: 60).coerceIn(5, 120) }.orElse(60))
    maxPrompt.set(providers.gradleProperty("branchDoctorMaxPrompt").map { (it.toIntOrNull() ?: 6000).coerceIn(1000, 30000) }.orElse(6000))
    redact.set(providers.gradleProperty("branchDoctorRedact").map { it.equals("true", true) }.orElse(true))

    // AI selection thresholds
    minCoveredBranchesForAi.set(providers.gradleProperty("branchDoctorMinCoveredBranchesForAi").map { it.toIntOrNull() ?: 1 }.orElse(1))
    maxAiAnalyses.set(providers.gradleProperty("branchDoctorMaxAiAnalyses").map { (it.toIntOrNull() ?: 20).coerceAtLeast(1) }.orElse(20))

    // Outputs live next to the JaCoCo XML
    val reportDirProvider = xmlReport.map { it.asFile.parentFile }
    outputJson.set(layout.file(reportDirProvider.map { File(it, "branch-gaps.json") }))
    outputMd.set(layout.file(reportDirProvider.map { File(it, "branch-gaps.md") }))
    outputAiMd.set(layout.file(reportDirProvider.map { File(it, "branch-gaps-ai.md") }))
    outputMeta.set(layout.file(reportDirProvider.map { File(it, "branch-gaps.meta.json") }))

    // Global switch to skip the task entirely
    onlyIf { branchDoctorEnabled.get() }
}

// Convenience lifecycle task: run tests + report + branch doctor
tasks.register("androidInstrumentedCoverageWithBranchDoctor") {
    group = "verification"
    description = "Runs instrumented tests, generates JaCoCo, and runs Branch Coverage Doctor."
    dependsOn("androidConnectedTestCoverageReport")
    dependsOn("androidCoverageBranchDoctor")
}
