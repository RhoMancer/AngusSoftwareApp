import org.gradle.testing.jacoco.tasks.JacocoReport

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
