package dev.angussoftware.aidoctor

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import java.time.Duration
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parses a JaCoCo XML report to identify lines with missed branches (mb > 0),
 * maps them back to source files, emits JSON/Markdown summaries, and (optionally)
 * asks a local Ollama model for remediation suggestions.
 *
 * All toggles are provided by the registering build script (usually from -P properties).
 */
@CacheableTask
abstract class BranchCoverageDoctorTask : DefaultTask() {
    init {
        // Default: exclude lines with zero covered branches from AI suggestions
        minCoveredBranchesForAi.convention(1)
        // Default: cap the number of lines analyzed by AI to avoid overly long prompts
        maxAiAnalyses.convention(20)
    }

    // Inputs
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val xmlReport: RegularFileProperty

    /** Absolute paths to source roots (e.g., src/commonMain/kotlin, src/androidMain/kotlin) */
    @get:Input
    abstract val sourceRoots: ListProperty<String>

    @get:Input
    abstract val branchDoctorEnabled: Property<Boolean>

    @get:Input
    abstract val aiEnabled: Property<Boolean>

    @get:Input
    abstract val ciEnabled: Property<Boolean>

    @get:Input
    abstract val contextLines: Property<Int> // default 5, -1 = whole file

    /** Minimum covered branches (cb) required for a line to be included in AI analysis; default 1. */
    @get:Input
    abstract val minCoveredBranchesForAi: Property<Int>

    /** Maximum number of lines to include in AI analysis across all files; default 20. */
    @get:Input
    abstract val maxAiAnalyses: Property<Int>

    @get:Optional
    @get:Input
    abstract val topNFiles: Property<Int>

    @get:Optional
    @get:Input
    abstract val failIfMissedBranches: Property<Int>

    @get:Optional
    @get:Input
    abstract val failIfMissedBranchesPerFile: Property<Int>

    // AI properties
    @get:Input
    abstract val model: Property<String> // default gemma3

    @get:Input
    abstract val ollamaCmd: Property<String> // default auto (ollama/ollama.exe)

    @get:Input
    abstract val timeoutSec: Property<Int> // 60 (5–120)

    @get:Input
    abstract val maxPrompt: Property<Int> // 6000 (1000–30000)

    @get:Input
    abstract val redact: Property<Boolean> // true

    // Outputs
    @get:OutputFile
    abstract val outputJson: RegularFileProperty

    @get:OutputFile
    abstract val outputMd: RegularFileProperty

    @get:OutputFile
    abstract val outputAiMd: RegularFileProperty

    @get:OutputFile
    abstract val outputMeta: RegularFileProperty

    @TaskAction
    fun run() {
        if (!branchDoctorEnabled.getOrElse(false)) {
            logger.lifecycle("[BranchDoctor] Skipped (branchDoctorEnabled=false)")
            return
        }

        val xmlFile = xmlReport.asFile.get()
        if (!xmlFile.exists()) {
            throw GradleException("BranchDoctor: XML report not found: ${xmlFile.absolutePath}. Ensure Jacoco report task ran.")
        }

        val sources = sourceRoots.getOrElse(emptyList()).map(::File)
        val ctx = contextLines.getOrElse(5)
        val topN = topNFiles.orNull?.takeIf { it > 0 }
        val minCbAi = minCoveredBranchesForAi.getOrElse(1).coerceAtLeast(0)
        val maxAi = maxAiAnalyses.getOrElse(20).coerceAtLeast(1)

        val willRunAi = aiShouldRun()
        var finalPromptUsed: String? = null
        var aiResponse: String? = null

        // Parse JaCoCo XML
        val findings = parseMissedBranches(xmlFile)
        if (findings.totalMissed == 0 || findings.files.isEmpty()) {
            // Still emit minimal files
            outputJson.asFile.get().writeText("{\n  \"totalMissedBranches\": 0,\n  \"files\": []\n}")
            outputMd.asFile.get().writeText("No missed branches found. ✅")
            if (willRunAi) {
                outputAiMd.asFile.get().writeText(
                    "## Branch Coverage Doctor — AI Suggestions\n\nNo missed branches to analyze.\n",
                )
            }

            // Write metadata for debugging
            writeMeta(
                outputMeta.asFile.get(),
                xmlFile.absolutePath,
                sources.map { it.absolutePath },
                branchDoctorEnabled.getOrElse(false),
                aiEnabled.getOrElse(false),
                ciEnabled.getOrElse(false),
                Os.isCi(),
                ctx,
                topNFiles.orNull,
                failIfMissedBranches.orNull,
                failIfMissedBranchesPerFile.orNull,
                minCbAi,
                maxAi,
                model.getOrElse("gemma3"),
                ollamaCmd.getOrElse(Os.defaultOllamaCommand()),
                timeoutSec.getOrElse(60),
                maxPrompt.getOrElse(6000),
                redact.getOrElse(true),
                willRunAi,
                finalPromptUsed,
                0,
                outputJson.asFile.get(),
                outputMd.asFile.get(),
                outputAiMd.asFile.get(),
            )

            logger.lifecycle("[BranchDoctor] No missed branches. Reports written.")
            return
        }

        // Attach source context and compute per-file totals
        val enriched = enrichWithSource(findings, sources, ctx)
        val sortedFiles = enriched.files.sortedByDescending { it.totalMissed }
        val limited = if (topN != null) enriched.copy(files = sortedFiles.take(topN)) else enriched.copy(files = sortedFiles)

        // Write JSON/Markdown
        writeJson(limited, outputJson.asFile.get())
        writeMarkdown(limited, outputMd.asFile.get(), xmlFile.parentFile)

        // Optional AI step
        if (willRunAi) {
            logger.lifecycle("[BranchDoctor] [AI] Preparing analysis from coverage data...")
            // Filter lines for AI based on covered branches threshold
            // Then cap the total number of lines analyzed by AI across all files (maxAiAnalyses)
            // Prioritize lines by highest percentage of covered branches (cb / (cb + mb)) so that
            // when trimming occurs, the most partially-covered lines remain.
            data class Eligible(val file: FileFindingsDetailed, val line: EnrichedLine, val pctCovered: Double)
            val allEligible = mutableListOf<Eligible>()
            limited.files.forEach { f ->
                f.lines.forEach { l ->
                    if (l.cb >= minCbAi) {
                        val total = (l.cb + l.mb).coerceAtLeast(1) // guard against 0
                        val pct = l.cb.toDouble() / total.toDouble()
                        allEligible += Eligible(f, l, pct)
                    }
                }
            }
            val totalEligible = allEligible.size
            val selectedTriples = allEligible
                .sortedWith(
                    compareByDescending<Eligible> { it.pctCovered }
                        .thenByDescending { it.line.cb }
                        .thenBy { it.file.path }
                        .thenBy { it.line.line }
                )
                .take(maxAi)
            val selected = selectedTriples.map { it.file to it.line }
            if (totalEligible > 0) {
                logger.lifecycle(
                    "[BranchDoctor] [AI] Eligible lines: $totalEligible, selected: ${selected.size} (maxAiAnalyses=$maxAi, ordering=coverage% desc)",
                )
            }
            val grouped: List<FileFindingsDetailed> =
                if (selected.isEmpty()) {
                    emptyList()
                } else {
                    selected
                        .groupBy({ it.first.path }) { it }
                        .map { (_, pairs) ->
                            val file = pairs.first().first
                            val lines = pairs.map { it.second }
                            file.copy(totalMissed = lines.sumOf { it.mb }, lines = lines)
                        }
                }
            val aiData = limited.copy(totalMissed = grouped.sumOf { it.totalMissed }, files = grouped)

            if (aiData.files.isEmpty()) {
                logger.lifecycle("[BranchDoctor] [AI] No eligible lines for AI (minCoveredBranchesForAi=$minCbAi). Skipping Ollama call.")
                outputAiMd.asFile.get().writeText(
                    "## Branch Coverage Doctor — AI Suggestions\n\n" +
                        "Model: ${model.get()}\n\n" +
                        "Ollama: ${ollamaCmd.get()}  | Timeout: ${timeoutSec.get()}s  | MaxPrompt: ${maxPrompt.get()}  | Redact: ${redact.getOrElse(
                            true,
                        )}  | MinCoveredBranchesForAi: $minCbAi  | MaxAiAnalyses: ${maxAi}\n\n" +
                        "No lines eligible for AI analysis. Threshold minCoveredBranchesForAi=$minCbAi excluded lines with cb < $minCbAi.\n",
                )
            } else {
                val fileCount = aiData.files.size
                val lineCount = aiData.files.sumOf { it.lines.size }
                logger.lifecycle(
                    "[BranchDoctor] [AI] Building prompt for $fileCount file(s), $lineCount line(s) (contextLines=$ctx, minCbAi=$minCbAi, maxAiAnalyses=$maxAi, redact=${redact.getOrElse(
                        true,
                    )}).",
                )
                val prompt = buildAiPrompt(aiData)
                val clipped = AiText.clip(prompt, maxPrompt.get())
                finalPromptUsed = if (redact.getOrElse(true)) AiText.redactSensitive(clipped, project.rootDir) else clipped
                logger.lifecycle(
                    "[BranchDoctor] [AI] Prompt prepared (length=${finalPromptUsed!!.length} chars, maxPrompt=${maxPrompt.get()}).",
                )
                val startNs = System.nanoTime()
                logger.lifecycle(
                    "[BranchDoctor] [AI] Invoking Ollama CLI '${ollamaCmd.get()}' (model=${model.get()}, timeout=${timeoutSec.get()}s). This may take a while...",
                )
                aiResponse =
                    OllamaClient.run(
                        config = OllamaClient.Config(
                            command = ollamaCmd.get(),
                            model = model.get(),
                            workingDir = project.rootDir,
                            timeout = Duration.ofSeconds(timeoutSec.get().toLong()),
                            logTag = "BranchDoctor [AI]",
                        ),
                        prompt = finalPromptUsed!!,
                        logger = logger,
                    )
                val durSec = (System.nanoTime() - startNs) / 1_000_000_000.0
                logger.lifecycle(
                    "[BranchDoctor] [AI] Ollama call finished in ${"%.1f".format(durSec)}s (responseLength=${aiResponse?.length ?: 0}).",
                )

                // Inject code windows next to the model's per-line recommendations where possible.
                val (enhancedResponse, injectedCount) = injectCodeWindowsIntoResponse(aiResponse ?: "", aiData)
                logger.lifecycle("[BranchDoctor] [AI] Injected $injectedCount code window(s) into AI response.")
                val finalResponse = if (injectedCount > 0) enhancedResponse else enhancedResponse + buildCodeAppendix(aiData)

                outputAiMd.asFile.get().writeText(
                    "## Branch Coverage Doctor — AI Suggestions\n\n" +
                        "Model: ${model.get()}\n\n" +
                        "Ollama: ${ollamaCmd.get()}  | Timeout: ${timeoutSec.get()}s  | MaxPrompt: ${maxPrompt.get()}  | Redact: ${redact.getOrElse(
                            true,
                        )}  | MinCoveredBranchesForAi: $minCbAi  | MaxAiAnalyses: ${maxAi}\n\n" +
                        finalResponse + "\n",
                )
                logger.lifecycle("[BranchDoctor] [AI] Suggestions written: ${outputAiMd.asFile.get().absolutePath}")
            }
        } else {
            val isCi = Os.isCi()
            val reason =
                when {
                    !aiEnabled.getOrElse(false) -> "aiEnabled=false"
                    isCi && !ciEnabled.getOrElse(false) -> "CI=true and ciEnabled=false"
                    else -> "not requested"
                }
            logger.lifecycle("[BranchDoctor] [AI] Skipped ($reason).")
        }

        // Write metadata for debugging (before enforcing thresholds)
        writeMeta(
            outputMeta.asFile.get(),
            xmlFile.absolutePath,
            sources.map { it.absolutePath },
            branchDoctorEnabled.getOrElse(false),
            aiEnabled.getOrElse(false),
            ciEnabled.getOrElse(false),
            Os.isCi(),
            ctx,
            topNFiles.orNull,
            failIfMissedBranches.orNull,
            failIfMissedBranchesPerFile.orNull,
            minCbAi,
            maxAi,
            model.getOrElse("gemma3"),
            ollamaCmd.getOrElse(Os.defaultOllamaCommand()),
            timeoutSec.getOrElse(60),
            maxPrompt.getOrElse(6000),
            redact.getOrElse(true),
            willRunAi,
            finalPromptUsed,
            enriched.totalMissed,
            outputJson.asFile.get(),
            outputMd.asFile.get(),
            outputAiMd.asFile.get(),
        )

        // Threshold enforcement
        val totalMissed = enriched.totalMissed
        val perFileLimit = failIfMissedBranchesPerFile.orNull
        if (perFileLimit != null) {
            val violator = enriched.files.firstOrNull { it.totalMissed > perFileLimit }
            if (violator != null) {
                throw GradleException("BranchDoctor: File '${violator.path}' missed branches ${violator.totalMissed} > $perFileLimit.")
            }
        }
        val globalLimit = failIfMissedBranches.orNull
        if (globalLimit != null && totalMissed > globalLimit) {
            throw GradleException("BranchDoctor: Total missed branches $totalMissed > $globalLimit.")
        }

        logger.lifecycle(
            "[BranchDoctor] Done. Total missed branches: $totalMissed\n- JSON: ${outputJson.asFile.get().absolutePath}\n- MD:   ${outputMd.asFile.get().absolutePath}${if (willRunAi) "\n- AI:   ${outputAiMd.asFile.get().absolutePath}" else ""}",
        )
    }

    private fun aiShouldRun(): Boolean {
        return AiGate.shouldRun(
            aiEnabled = aiEnabled.getOrElse(false),
            ciEnabled = ciEnabled.getOrElse(false),
        )
    }

    // ---------------- XML parsing and models ----------------

    private fun parseMissedBranches(xml: File): ReportData {
        val factory = DocumentBuilderFactory.newInstance()
        // Security and robustness: do not fetch external DTDs, but allow DOCTYPE so JaCoCo XML parses.
        factory.setNamespaceAware(false)
        factory.setValidating(false)
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        } catch (_: Exception) {
        }
        try {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        } catch (_: Exception) {
        }
        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        } catch (_: Exception) {
        }
        try {
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        } catch (_: Exception) {
        }

        val builder = factory.newDocumentBuilder()
        // Resolve any external entities (like JaCoCo's report.dtd) to an empty string to avoid FileNotFound
        builder.setEntityResolver(EntityResolver { _, _ -> InputSource(StringReader("")) })

        val doc = builder.parse(xml)
        doc.documentElement.normalize()

        val fileMap = mutableMapOf<String, MutableList<LineHit>>()
        val packages = doc.getElementsByTagName("package")
        for (i in 0 until packages.length) {
            val p = packages.item(i)
            val attrs = p.attributes
            val pkgName = attrs?.getNamedItem("name")?.nodeValue ?: ""
            val sourceFiles = p.childNodes
            for (j in 0 until sourceFiles.length) {
                val nf = sourceFiles.item(j)
                if (nf.nodeName != "sourcefile") continue
                val nfa = nf.attributes
                val sfName = nfa?.getNamedItem("name")?.nodeValue ?: continue
                val relPath = if (pkgName.isBlank()) sfName else pkgName + "/" + sfName

                val lines = nf.childNodes
                for (k in 0 until lines.length) {
                    val ln = lines.item(k)
                    if (ln.nodeName != "line") continue
                    val la = ln.attributes
                    val nr = la?.getNamedItem("nr")?.nodeValue?.toIntOrNull() ?: continue
                    val mb = la.getNamedItem("mb")?.nodeValue?.toIntOrNull() ?: 0
                    val cb = la.getNamedItem("cb")?.nodeValue?.toIntOrNull() ?: 0
                    if (mb > 0) {
                        fileMap
                            .computeIfAbsent(relPath) { mutableListOf() }
                            .add(LineHit(line = nr, mb = mb, cb = cb))
                    }
                }
            }
        }
        val files = fileMap.map { (path, lines) -> FileFindings(path = path, lines = lines.sortedBy { it.line }) }
        val totalMissed = files.sumOf { f -> f.lines.sumOf { it.mb } }
        return ReportData(totalMissed = totalMissed, files = files)
    }

    private fun enrichWithSource(
        data: ReportData,
        roots: List<File>,
        context: Int,
    ): ReportDataDetailed {
        val detailed =
            data.files.map { f ->
                val src = locateSource(roots, f.path)
                val text = src?.takeIf { it.exists() }?.readText() ?: ""
                val codeLines = if (text.isNotEmpty()) text.split("\n") else emptyList()
                val enrichedLines =
                    f.lines.map { l ->
                        val window =
                            if (context == -1) {
                                CodeWindow(start = 1, end = codeLines.size, lines = codeLines)
                            } else {
                                val start = (l.line - context).coerceAtLeast(1)
                                val end = (l.line + context).coerceAtMost(codeLines.size)
                                val slice = if (codeLines.isNotEmpty() && start <= end) codeLines.subList(start - 1, end) else emptyList()
                                CodeWindow(start = start, end = end, lines = slice)
                            }
                        EnrichedLine(line = l.line, mb = l.mb, cb = l.cb, classification = classify(window.lines), window = window)
                    }
                val total = f.lines.sumOf { it.mb }
                FileFindingsDetailed(
                    path = f.path,
                    sourceFile = src?.absolutePath ?: "<not found>",
                    totalMissed = total,
                    lines = enrichedLines,
                )
            }
        val totalAll = detailed.sumOf { it.totalMissed }
        return ReportDataDetailed(totalMissed = totalAll, files = detailed)
    }

    private fun locateSource(
        roots: List<File>,
        relPath: String,
    ): File? {
        val normalizedRel = relPath.replace('\\', '/')
        for (r in roots) {
            val f = File(r, normalizedRel)
            if (f.exists()) return f
        }
        // fallback: match by file name under roots (slow but limited scope)
        val fileName = normalizedRel.substringAfterLast('/')
        roots.forEach { root ->
            val found = root.walkTopDown().firstOrNull { it.isFile && it.name == fileName }
            if (found != null) return found
        }
        return null
    }

    private fun classify(lines: List<String>): String {
        if (lines.isEmpty()) return "unknown"
        val joined = lines.joinToString("\n")
        return when {
            Regex("\\bwhen\\s*\\(").containsMatchIn(joined) -> "when-expression"
            Regex("\\belse\\b").containsMatchIn(joined) && Regex("\\bif\\b").containsMatchIn(joined) -> "if-else"
            Regex("\\bif\\b").containsMatchIn(joined) -> "if"
            joined.contains("&&") || joined.contains("||") -> "boolean-op"
            joined.contains("?:") -> "elvis"
            joined.contains("?.") -> "safe-call"
            joined.contains("!!") -> "not-null-assertion"
            Regex("\\breturn\\b").containsMatchIn(joined) -> "early-return"
            else -> "unknown"
        }
    }

    // ---------------- Output writers ----------------

    private val LINE_ORDER: Comparator<EnrichedLine> =
        compareByDescending<EnrichedLine> { it.cb.toDouble() / (it.cb + it.mb).coerceAtLeast(1).toDouble() }
            .thenByDescending { it.cb }
            .thenBy { it.line }

    private fun writeJson(
        data: ReportDataDetailed,
        out: File,
    ) {
        fun esc(s: String) = jsonEsc(s)
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"totalMissedBranches\": ").append(data.totalMissed).append(",\n")
        sb.append("  \"files\": [\n")
        data.files.forEachIndexed { i, f ->
            if (i > 0) sb.append(",\n")
            sb.append("    {\n")
            sb.append("      \"path\": \"").append(esc(f.path)).append("\",\n")
            sb.append("      \"sourceFile\": \"").append(esc(f.sourceFile)).append("\",\n")
            sb.append("      \"totalMissed\": ").append(f.totalMissed).append(",\n")
            sb.append("      \"lines\": [\n")
            val sortedLines = f.lines.sortedWith(LINE_ORDER)
            sortedLines.forEachIndexed { j, l ->
                if (j > 0) sb.append(",\n")
                sb.append("        {\n")
                sb.append("          \"line\": ").append(l.line).append(",\n")
                sb.append("          \"mb\": ").append(l.mb).append(",\n")
                sb.append("          \"cb\": ").append(l.cb).append(",\n")
                sb.append("          \"classification\": \"").append(esc(l.classification)).append("\",\n")
                sb.append("          \"window\": {\n")
                sb.append("            \"start\": ").append(l.window.start).append(",\n")
                sb.append("            \"end\": ").append(l.window.end).append(",\n")
                sb.append("            \"lines\": [")
                l.window.lines.forEachIndexed { k, lineText ->
                    if (k > 0) sb.append(", ")
                    sb.append("\"").append(esc(lineText)).append("\"")
                }
                sb.append("]\n")
                sb.append("          }\n")
                sb.append("        }")
            }
            sb.append("\n      ]\n")
            sb.append("    }")
        }
        sb.append("\n  ]\n")
        sb.append("}\n")
        out.parentFile.mkdirs()
        out.writeText(sb.toString())
    }

    private fun writeMarkdown(
        data: ReportDataDetailed,
        out: File,
        jacocoDir: File,
    ) {
        val sb = StringBuilder()
        sb.appendLine("## Branch Coverage Doctor — Missed Branches Summary")
        sb.appendLine()
        sb.appendLine("Total missed branches: ${data.totalMissed}")
        sb.appendLine()
        data.files.forEach { f ->
            sb.appendLine("### ${f.path}  (missed: ${f.totalMissed})")
            sb.appendLine("Source: ${f.sourceFile}")
            sb.appendLine()
            val sortedLines = f.lines.sortedWith(LINE_ORDER)
            sortedLines.forEach { l ->
                sb.appendLine("- Line ${l.line}: mb=${l.mb}, cb=${l.cb}, type=${l.classification}")
                sb.append(formatCodeBlockForLine(f, l))
            }
        }
        out.parentFile.mkdirs()
        out.writeText(sb.toString())
    }

    private fun writeMeta(
        out: File,
        xmlPath: String,
        sourceRoots: List<String>,
        enabled: Boolean,
        aiEnabled: Boolean,
        ciEnabledProp: Boolean,
        ciEnv: Boolean,
        contextLines: Int,
        topNFiles: Int?,
        failIfMissedBranches: Int?,
        failIfMissedBranchesPerFile: Int?,
        minCoveredBranchesForAi: Int,
        maxAiAnalyses: Int,
        model: String,
        ollamaCmd: String,
        timeoutSec: Int,
        maxPrompt: Int,
        redact: Boolean,
        willRunAi: Boolean,
        finalPromptUsed: String?,
        totalMissedBranches: Int,
        jsonOut: File,
        mdOut: File,
        aiMdOut: File,
    ) {
        fun esc(s: String?): String = jsonEsc(s)
        val sb = StringBuilder()
        sb.append("{\n")
        sb
            .append("  \"timestamp\": \"")
            .append(
                esc(
                    java.time.OffsetDateTime
                        .now()
                        .toString(),
                ),
            ).append("\",\n")
        sb.append("  \"projectPath\": \"").append(esc(project.path)).append("\",\n")
        sb.append("  \"xmlReport\": \"").append(esc(xmlPath)).append("\",\n")
        sb.append("  \"sourceRoots\": [").append(sourceRoots.joinToString(", ") { "\"${esc(it)}\"" }).append("],\n")
        sb.append("  \"flags\": {\n")
        sb.append("    \"branchDoctorEnabled\": ").append(enabled).append(",\n")
        sb.append("    \"aiEnabled\": ").append(aiEnabled).append(",\n")
        sb.append("    \"ciEnabledProp\": ").append(ciEnabledProp).append(",\n")
        sb.append("    \"ciEnv\": ").append(ciEnv).append(",\n")
        sb.append("    \"contextLines\": ").append(contextLines).append(",\n")
        sb.append("    \"topNFiles\": ").append(topNFiles?.toString() ?: "null").append(",\n")
        sb.append("    \"failIfMissedBranches\": ").append(failIfMissedBranches?.toString() ?: "null").append(",\n")
        sb.append("    \"failIfMissedBranchesPerFile\": ").append(failIfMissedBranchesPerFile?.toString() ?: "null").append(",\n")
        sb.append("    \"minCoveredBranchesForAi\": ").append(minCoveredBranchesForAi).append(",\n")
        sb.append("    \"maxAiAnalyses\": ").append(maxAiAnalyses).append("\n")
        sb.append("  },\n")
        sb.append("  \"ai\": {\n")
        sb.append("    \"willRunAi\": ").append(willRunAi).append(",\n")
        sb.append("    \"model\": \"").append(esc(model)).append("\",\n")
        sb.append("    \"ollamaCmd\": \"").append(esc(ollamaCmd)).append("\",\n")
        sb.append("    \"timeoutSec\": ").append(timeoutSec).append(",\n")
        sb.append("    \"maxPrompt\": ").append(maxPrompt).append(",\n")
        sb.append("    \"redact\": ").append(redact).append(",\n")
        sb.append("    \"promptLength\": ").append(finalPromptUsed?.length ?: 0).append(",\n")
        sb.append("    \"prompt\": \"").append(esc(finalPromptUsed)).append("\"\n")
        sb.append("  },\n")
        sb.append("  \"results\": {\n")
        sb.append("    \"totalMissedBranches\": ").append(totalMissedBranches).append(",\n")
        sb.append("    \"outputs\": {\n")
        sb.append("      \"json\": \"").append(esc(jsonOut.absolutePath)).append("\",\n")
        sb.append("      \"md\": \"").append(esc(mdOut.absolutePath)).append("\",\n")
        sb.append("      \"aiMd\": \"").append(esc(aiMdOut.absolutePath)).append("\"\n")
        sb.append("    }\n")
        sb.append("  }\n")
        sb.append("}\n")
        out.parentFile.mkdirs()
        out.writeText(sb.toString())
    }

    // ---------------- AI helpers ----------------

    private fun buildAiPrompt(data: ReportDataDetailed): String {
        val sb = StringBuilder()
        sb.appendLine(
            "You are a senior Android/Kotlin test engineer. For each file and line below, explain which logical cases are likely untested and propose concrete test steps to hit the missing branches. Keep answers practical and concise.",
        )
        sb.appendLine()
        sb.appendLine("Project: ${project.rootProject.name}")
        sb.appendLine("Module: ${project.path}")
        sb.appendLine("Total missed branches: ${data.totalMissed}")
        sb.appendLine()
        data.files.forEach { f ->
            sb.appendLine("File: ${f.path}  (missed: ${f.totalMissed})")
            f.lines.forEach { l ->
                sb.appendLine("- Line ${l.line}: mb=${l.mb}, cb=${l.cb}, type=${l.classification}")
                val start = l.window.start
                sb.appendLine("```kotlin")
                l.window.lines.forEachIndexed { idx, t ->
                    val no = start + idx
                    val mark = if (no == l.line) "▶" else " "
                    sb.appendLine("$mark ${no.toString().padStart(4, ' ')} | $t")
                }
                sb.appendLine("```")
            }
            sb.appendLine()
        }
        sb.appendLine(
            "Output format per file:\n- Bullets of missing logical scenarios\n- Specific test steps / UI flows\n- Optional refactor suggestions to increase testability",
        )
        return sb.toString()
    }

    // Injects code windows directly after headings in the AI response.
    // Recognizes headings like "### HomeScreen.kt" and line anchors like "#### Line 47:" or "- Line 47".
    private fun injectCodeWindowsIntoResponse(
        response: String,
        data: ReportDataDetailed,
    ): Pair<String, Int> {
        if (response.isBlank()) return response to 0

        // Build lookup: baseFileName (lowercased) -> (line -> pair(file, enrichedLine))
        val fileMap: Map<String, Map<Int, Pair<FileFindingsDetailed, EnrichedLine>>> =
            data.files.associate { f ->
                val base = File(f.path).name.lowercase()
                val lines = f.lines.associateBy({ it.line }, { f to it })
                base to lines
            }

        val lines = response.lines().toMutableList()
        val out = StringBuilder()
        var injected = 0
        var i = 0
        var currentFileKey: String? = null

        fun wouldFollowWithCodeFence(startIdx: Int): Boolean {
            val end = minOf(lines.size, startIdx + 5)
            for (k in startIdx until end) {
                val t = lines[k].trimStart()
                if (t.startsWith("```")) return true
                if (t.startsWith("####") || t.startsWith("###") || t.startsWith("##")) return false
            }
            return false
        }

        while (i < lines.size) {
            val raw = lines[i]
            val trimmed = raw.trim()

            // Detect file heading patterns
            val fileHeading = extractFileKeyFromHeading(trimmed)
            if (fileHeading != null) {
                currentFileKey = fileHeading
                out.appendLine(raw)
                i++
                continue
            }

            // Detect line anchors within a file context
            val lineNum = extractLineNumber(trimmed)
            if (lineNum != null && currentFileKey != null) {
                val hit = fileMap[currentFileKey!!]?.get(lineNum)
                out.appendLine(raw)
                if (hit != null && !wouldFollowWithCodeFence(i + 1)) {
                    val code = formatCodeBlockForLine(hit.first, hit.second)
                    out.append(code)
                    injected++
                }
                i++
                continue
            }

            out.appendLine(raw)
            i++
        }

        return out.toString() to injected
    }

    private fun extractFileKeyFromHeading(heading: String): String? {
        // Examples accepted:
        //   "### HomeScreen.kt"
        //   "## File: dev/angus/.../HomeScreen.kt"
        //   "File: dev/.../HomeScreen.kt  (missed: 5)"
        val ktOrJava = Regex("""([A-Za-z0-9_]+\.(kt|java))""")
        val m = ktOrJava.find(heading)
        return m
            ?.groups
            ?.get(1)
            ?.value
            ?.lowercase()
    }

    private fun extractLineNumber(text: String): Int? {
        // Matches many variants, anywhere in the line (bold, bullets, numbered lists):
        // "1. **Line 47**:", "- Line 47", "#### Line 47:", "Line 47"
        val r = Regex("\\bLine\\s+(\\d+)", RegexOption.IGNORE_CASE)
        val m = r.find(text)
        return m
            ?.groups
            ?.get(1)
            ?.value
            ?.toIntOrNull()
    }

    private fun formatCodeBlockForLine(
        file: FileFindingsDetailed,
        l: EnrichedLine,
    ): String {
        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("```kotlin")
        val start = l.window.start
        l.window.lines.forEachIndexed { idx, t ->
            val no = start + idx
            val mark = if (no == l.line) "▶" else " "
            sb.appendLine("$mark ${no.toString().padStart(4, ' ')} | $t")
        }
        sb.appendLine("```")
        sb.appendLine()
        return sb.toString()
    }

    private fun buildCodeAppendix(data: ReportDataDetailed): String {
        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
        sb.appendLine("### Code windows (appendix)")
        data.files.forEach { f ->
            sb.appendLine()
            sb.appendLine("#### ${f.path}")
            f.lines.forEach { l ->
                sb.appendLine("Line ${l.line}: mb=${l.mb}, cb=${l.cb}, type=${l.classification}")
                sb.append(formatCodeBlockForLine(f, l))
            }
        }
        return sb.toString()
    }

}

// ---------------- Data models ----------------

data class LineHit(
    val line: Int,
    val mb: Int,
    val cb: Int,
)

data class FileFindings(
    val path: String,
    val lines: List<LineHit>,
)

data class ReportData(
    val totalMissed: Int,
    val files: List<FileFindings>,
)

data class CodeWindow(
    val start: Int,
    val end: Int,
    val lines: List<String>,
)

data class EnrichedLine(
    val line: Int,
    val mb: Int,
    val cb: Int,
    val classification: String,
    val window: CodeWindow,
)

data class FileFindingsDetailed(
    val path: String,
    val sourceFile: String,
    val totalMissed: Int,
    val lines: List<EnrichedLine>,
)

data class ReportDataDetailed(
    val totalMissed: Int,
    val files: List<FileFindingsDetailed>,
)
