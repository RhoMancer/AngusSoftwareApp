package com.angussoftware.app.buildsrc

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Custom Gradle task that generates a unified coverage report combining
 * Kover (unit tests) and JaCoCo (instrumentation tests) coverage data.
 *
 * The task parses XML reports from both coverage systems and generates
 * a single markdown document. When both reports are available, combined
 * metrics are computed via a line-level OR merge: a line is counted as
 * covered if either tool covered it, eliminating double-counting.
 */
abstract class UnifiedCoverageReportTask : DefaultTask() {
    /**
     * Input directory for Kover reports (required)
     * The koverXmlReport task generates report.xml in the kover directory
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val koverReportDir: DirectoryProperty

    /**
     * Build directory path (used to locate JaCoCo reports)
     * JaCoCo reports are optional - if they don't exist, only unit test coverage is shown
     */
    @get:Input
    abstract val buildDirPath: Property<String>

    /**
     * Output file for the unified coverage report
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Class/package patterns excluded from Kover (unit test) coverage.
     * These are classes that Kover is configured to skip via its filter block.
     */
    @get:Input
    abstract val koverExclusions: ListProperty<String>

    /**
     * Class/package patterns excluded from JaCoCo (instrumentation) coverage.
     * These are the paths excluded from the classDirectories fileTree in jacocoMergedReport.
     */
    @get:Input
    abstract val jacocoExclusions: ListProperty<String>

    private val timestamp: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    // ============================================================================
    // Data classes
    // ============================================================================

    /**
     * Unique key identifying a single source line across both reports.
     */
    private data class LineKey(
        val packageName: String,
        val sourceFileName: String,
        val lineNr: Int,
    )

    /**
     * Raw per-line coverage data from either Kover or JaCoCo XML.
     * Both tools emit the same JaCoCo XML format with mi/ci/mb/cb attributes.
     */
    private data class LineData(
        val coveredInstructions: Int,
        val missedInstructions: Int,
        val coveredBranches: Int,
        val missedBranches: Int,
    ) {
        val isCovered: Boolean get() = coveredInstructions > 0
    }

    /**
     * Aggregated metrics computed from a merged line-level map.
     */
    private data class MergedMetrics(
        val lineCovered: Int,
        val lineMissed: Int,
        val branchCovered: Int,
        val branchMissed: Int,
        val instructionCovered: Int,
        val instructionMissed: Int,
    ) {
        val lineTotal: Int get() = lineCovered + lineMissed
        val branchTotal: Int get() = branchCovered + branchMissed
        val instructionTotal: Int get() = instructionCovered + instructionMissed

        val linePercentage: String get() = pct(lineCovered, lineTotal)
        val branchPercentage: String get() = pct(branchCovered, branchTotal)
        val instructionPercentage: String get() = pct(instructionCovered, instructionTotal)

        private fun pct(
            covered: Int,
            total: Int,
        ): String = if (total > 0) String.format("%.1f", covered.toDouble() / total * 100.0) else "0.0"
    }

    /**
     * Top-level summary metrics for a single report (used for individual report tables).
     */
    data class CoverageMetrics(
        val instructionCovered: Int,
        val instructionMissed: Int,
        val lineCovered: Int,
        val lineMissed: Int,
        val branchCovered: Int,
        val branchMissed: Int,
    ) {
        val instructionTotal: Int get() = instructionCovered + instructionMissed
        val lineTotal: Int get() = lineCovered + lineMissed
        val branchTotal: Int get() = branchCovered + branchMissed

        val instructionPercentage: String get() = calculatePercentage(instructionCovered, instructionTotal)
        val linePercentage: String get() = calculatePercentage(lineCovered, lineTotal)
        val branchPercentage: String get() = calculatePercentage(branchCovered, branchTotal)

        private fun calculatePercentage(
            covered: Int,
            total: Int,
        ): String =
            if (total > 0) {
                String.format("%.1f", covered.toDouble() / total * 100.0)
            } else {
                "0.0"
            }
    }

    // ============================================================================
    // Task entry point
    // ============================================================================

    @TaskAction
    fun generate() {
        val koverXmlFile = koverReportDir.get().file("report.xml").asFile
        val jacocoXmlFile = File(buildDirPath.get(), "reports/jacoco/androidConnectedTest/report.xml")

        // Parse top-level summary metrics (for individual report tables)
        val koverReport = parseTopLevelMetrics(koverXmlFile)
        val jacocoReport = jacocoXmlFile.takeIf { it.exists() }?.let { parseTopLevelMetrics(it) }

        // Parse line-level data for OR merge (only when both reports exist)
        val mergedMetrics: MergedMetrics? =
            if (jacocoReport != null) {
                val koverLines = parseLineLevelData(koverXmlFile)
                val jacocoLines = parseLineLevelData(jacocoXmlFile)
                mergeLineLevelData(koverLines, jacocoLines)
            } else {
                null
            }

        val markdown = buildMarkdown(koverReport, jacocoReport, mergedMetrics, koverExclusions.get(), jacocoExclusions.get())
        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(markdown)
        }

        println("Unified coverage report generated: ${outputFile.get().asFile.absolutePath}")
    }

    // ============================================================================
    // XML parsing
    // ============================================================================

    /**
     * Builds a reusable DocumentBuilder with DTD validation disabled.
     */
    private fun newDocumentBuilder() =
        DocumentBuilderFactory
            .newInstance()
            .apply {
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            }.newDocumentBuilder()

    /**
     * Parses top-level `<counter>` elements from a Kover or JaCoCo XML report.
     * Both tools produce counters at the root `<report>` level.
     */
    private fun parseTopLevelMetrics(file: File): CoverageMetrics {
        val doc = newDocumentBuilder().parse(file)
        val counters = doc.getElementsByTagName("counter")

        var instructionMissed = 0
        var instructionCovered = 0
        var lineMissed = 0
        var lineCovered = 0
        var branchMissed = 0
        var branchCovered = 0

        for (i in 0 until counters.length) {
            val node = counters.item(i)
            val type = node.attributes.getNamedItem("type")?.nodeValue ?: continue
            val missed =
                node.attributes
                    .getNamedItem("missed")
                    ?.nodeValue
                    ?.toInt() ?: 0
            val covered =
                node.attributes
                    .getNamedItem("covered")
                    ?.nodeValue
                    ?.toInt() ?: 0

            when (type) {
                "INSTRUCTION" -> {
                    instructionMissed = missed
                    instructionCovered = covered
                }

                "LINE" -> {
                    lineMissed = missed
                    lineCovered = covered
                }

                "BRANCH" -> {
                    branchMissed = missed
                    branchCovered = covered
                }
            }
        }

        return CoverageMetrics(
            instructionCovered = instructionCovered,
            instructionMissed = instructionMissed,
            lineCovered = lineCovered,
            lineMissed = lineMissed,
            branchCovered = branchCovered,
            branchMissed = branchMissed,
        )
    }

    /**
     * Parses a Kover or JaCoCo XML report into a line-level map keyed by
     * (packageName, sourceFileName, lineNumber).
     *
     * Both tools emit the same JaCoCo XML format:
     * ```xml
     * <package name="com/example/pkg">
     *   <sourcefile name="MyClass.kt">
     *     <line nr="25" mi="0" ci="4" mb="0" cb="2"/>
     *   </sourcefile>
     * </package>
     * ```
     */
    private fun parseLineLevelData(file: File): Map<LineKey, LineData> {
        val doc = newDocumentBuilder().parse(file)
        val result = mutableMapOf<LineKey, LineData>()

        val packages = doc.getElementsByTagName("package")
        for (pkgIdx in 0 until packages.length) {
            val pkgNode = packages.item(pkgIdx)
            val packageName = pkgNode.attributes.getNamedItem("name")?.nodeValue ?: continue

            val sourceFiles = pkgNode.childNodes
            for (sfIdx in 0 until sourceFiles.length) {
                val sfNode = sourceFiles.item(sfIdx)
                if (sfNode.nodeName != "sourcefile") continue
                val sourceFileName = sfNode.attributes.getNamedItem("name")?.nodeValue ?: continue

                val lines = sfNode.childNodes
                for (lineIdx in 0 until lines.length) {
                    val lineNode = lines.item(lineIdx)
                    if (lineNode.nodeName != "line") continue

                    val nr =
                        lineNode.attributes
                            .getNamedItem("nr")
                            ?.nodeValue
                            ?.toIntOrNull() ?: continue
                    val mi =
                        lineNode.attributes
                            .getNamedItem("mi")
                            ?.nodeValue
                            ?.toIntOrNull() ?: 0
                    val ci =
                        lineNode.attributes
                            .getNamedItem("ci")
                            ?.nodeValue
                            ?.toIntOrNull() ?: 0
                    val mb =
                        lineNode.attributes
                            .getNamedItem("mb")
                            ?.nodeValue
                            ?.toIntOrNull() ?: 0
                    val cb =
                        lineNode.attributes
                            .getNamedItem("cb")
                            ?.nodeValue
                            ?.toIntOrNull() ?: 0

                    result[LineKey(packageName, sourceFileName, nr)] =
                        LineData(
                            coveredInstructions = ci,
                            missedInstructions = mi,
                            coveredBranches = cb,
                            missedBranches = mb,
                        )
                }
            }
        }
        return result
    }

    // ============================================================================
    // Line-level OR merge
    // ============================================================================

    /**
     * Merges Kover and JaCoCo line-level maps using OR logic:
     * - A line is **covered** if either tool covered it.
     * - A line is **missed** only if both tools missed it.
     * - Lines present in only one report are taken as-is.
     *
     * For branches, we take the maximum covered/minimum missed from the two
     * reports per line, which is the best approximation possible without
     * per-branch tracking in the XML format.
     *
     * This produces a precise, non-double-counted combined total.
     */
    private fun mergeLineLevelData(
        koverLines: Map<LineKey, LineData>,
        jacocoLines: Map<LineKey, LineData>,
    ): MergedMetrics {
        val allKeys = koverLines.keys + jacocoLines.keys

        var lineCovered = 0
        var lineMissed = 0
        var branchCovered = 0
        var branchMissed = 0
        var instructionCovered = 0
        var instructionMissed = 0

        for (key in allKeys) {
            val kover = koverLines[key]
            val jacoco = jacocoLines[key]

            when {
                kover != null && jacoco != null -> {
                    // OR merge: covered if either tool covered it
                    val isCovered = kover.isCovered || jacoco.isCovered
                    if (isCovered) lineCovered++ else lineMissed++

                    // Instructions: use max(covered), min(missed) per line
                    val mergedCi = maxOf(kover.coveredInstructions, jacoco.coveredInstructions)
                    val mergedMi = minOf(kover.missedInstructions, jacoco.missedInstructions)
                    instructionCovered += mergedCi
                    instructionMissed += mergedMi

                    // Branches: best approximation — max covered, min missed
                    branchCovered += maxOf(kover.coveredBranches, jacoco.coveredBranches)
                    branchMissed += minOf(kover.missedBranches, jacoco.missedBranches)
                }

                kover != null -> {
                    if (kover.isCovered) lineCovered++ else lineMissed++
                    instructionCovered += kover.coveredInstructions
                    instructionMissed += kover.missedInstructions
                    branchCovered += kover.coveredBranches
                    branchMissed += kover.missedBranches
                }

                jacoco != null -> {
                    if (jacoco.isCovered) lineCovered++ else lineMissed++
                    instructionCovered += jacoco.coveredInstructions
                    instructionMissed += jacoco.missedInstructions
                    branchCovered += jacoco.coveredBranches
                    branchMissed += jacoco.missedBranches
                }
            }
        }

        return MergedMetrics(
            lineCovered = lineCovered,
            lineMissed = lineMissed,
            branchCovered = branchCovered,
            branchMissed = branchMissed,
            instructionCovered = instructionCovered,
            instructionMissed = instructionMissed,
        )
    }

    // ============================================================================
    // Markdown generation
    // ============================================================================

    /**
     * Builds the markdown report combining both coverage sources.
     */
    private fun buildMarkdown(
        koverReport: CoverageMetrics,
        jacocoReport: CoverageMetrics?,
        mergedMetrics: MergedMetrics?,
        koverExclusions: List<String>,
        jacocoExclusions: List<String>,
    ): String =
        buildString {
            appendLine("# Unified Coverage Report")
            appendLine()
            appendLine("**Generated:** $timestamp")
            appendLine()
            appendLine("---")
            appendLine()

            // Unit Test Coverage (Kover)
            appendLine("## Unit Test Coverage (Kover)")
            appendLine()
            appendMetricTable(koverReport)
            appendLine()
            appendLine("**Report:** [HTML](../kover/html/index.html) | [XML](../kover/report.xml)")
            appendLine()
            appendLine("**Excluded from Kover** (tested via JaCoCo instrumentation instead):")
            appendLine()
            if (koverExclusions.isEmpty()) {
                appendLine("*No exclusions configured.*")
            } else {
                koverExclusions.forEach { pattern -> appendLine("- `$pattern`") }
            }
            appendLine()
            appendLine("---")
            appendLine()

            // Instrumentation Test Coverage (JaCoCo)
            appendLine("## Instrumentation Test Coverage (JaCoCo)")
            appendLine()

            if (jacocoReport != null) {
                appendMetricTable(jacocoReport)
                appendLine()
                appendLine(
                    "**Report:** [HTML](../jacoco/androidConnectedTest/html/index.html) | [XML](../jacoco/androidConnectedTest/report.xml)",
                )
                appendLine()
                appendLine("**Excluded from JaCoCo** (external libraries and generated code):")
                appendLine()
                if (jacocoExclusions.isEmpty()) {
                    appendLine("*No exclusions configured.*")
                } else {
                    jacocoExclusions.forEach { pattern -> appendLine("- `$pattern`") }
                }
            } else {
                appendLine("*No instrumentation coverage data available.*")
                appendLine()
                appendLine("Run instrumentation tests to generate JaCoCo coverage:")
                appendLine()
                appendLine("```bash")
                appendLine("./gradlew :composeApp:connectedDebugAndroidTest :composeApp:androidConnectedTestCoverageReport")
                appendLine("```")
            }
            appendLine()
            appendLine("---")
            appendLine()

            // Combined Metrics via line-level OR merge
            if (jacocoReport != null && mergedMetrics != null) {
                appendLine("## Combined Metrics (Line-Level OR Merge)")
                appendLine()
                appendLine("Each physical line is counted exactly once: covered if **either** tool covered it.")
                appendLine()
                appendLine("| Metric | Unit Tests (Kover) | Instrumentation (JaCoCo) | Combined (OR merge) |")
                appendLine("|--------|--------------------|--------------------------|---------------------|")
                appendLine(
                    "| Line Coverage | ${koverReport.linePercentage}% (${koverReport.lineCovered}/${koverReport.lineTotal}) | ${jacocoReport.linePercentage}% (${jacocoReport.lineCovered}/${jacocoReport.lineTotal}) | **${mergedMetrics.linePercentage}%** (${mergedMetrics.lineCovered}/${mergedMetrics.lineTotal}) |",
                )
                appendLine(
                    "| Branch Coverage | ${koverReport.branchPercentage}% (${koverReport.branchCovered}/${koverReport.branchTotal}) | ${jacocoReport.branchPercentage}% (${jacocoReport.branchCovered}/${jacocoReport.branchTotal}) | **${mergedMetrics.branchPercentage}%** (${mergedMetrics.branchCovered}/${mergedMetrics.branchTotal}) |",
                )
                appendLine(
                    "| Instruction Coverage | ${koverReport.instructionPercentage}% (${koverReport.instructionCovered}/${koverReport.instructionTotal}) | ${jacocoReport.instructionPercentage}% (${jacocoReport.instructionCovered}/${jacocoReport.instructionTotal}) | **${mergedMetrics.instructionPercentage}%** (${mergedMetrics.instructionCovered}/${mergedMetrics.instructionTotal}) |",
                )
                appendLine()
                appendLine("---")
                appendLine()
            }

            // Notes section
            appendLine("## Notes")
            appendLine()
            appendLine("- **Unit tests** are measured by Kover (`commonTest` + `androidUnitTest`)")
            appendLine(
                "- **Instrumentation tests** are measured by JaCoCo (`androidInstrumentedTest`) including Compose UI Screens; external libraries excluded (listed above under Instrumentation Test Coverage)",
            )
            appendLine("- **Combined (OR merge)**: computed by parsing `<sourcefile>/<line>` elements from both XML reports")
            appendLine("  - A line is counted as **covered** if `ci > 0` in either report")
            appendLine("  - A line is counted as **missed** only if both tools missed it")
            appendLine("  - Lines unique to one report (e.g. Screen files only in JaCoCo) are taken as-is")
            appendLine("  - Branch coverage uses `max(covered)` / `min(missed)` per line as best approximation")
            appendLine("  - Each physical line is counted exactly once - no double-counting")
        }

    private fun StringBuilder.appendMetricTable(metrics: CoverageMetrics) {
        appendLine("| Metric | Covered | Missed | Total | Percentage |")
        appendLine("|--------|---------|--------|-------|------------|")
        appendLine(
            "| Instructions | ${metrics.instructionCovered} | ${metrics.instructionMissed} | ${metrics.instructionTotal} | ${metrics.instructionPercentage}% |",
        )
        appendLine("| Lines | ${metrics.lineCovered} | ${metrics.lineMissed} | ${metrics.lineTotal} | ${metrics.linePercentage}% |")
        appendLine(
            "| Branches | ${metrics.branchCovered} | ${metrics.branchMissed} | ${metrics.branchTotal} | ${metrics.branchPercentage}% |",
        )
    }
}
