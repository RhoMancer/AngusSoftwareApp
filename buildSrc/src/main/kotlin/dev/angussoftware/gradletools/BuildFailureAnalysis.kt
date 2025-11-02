package dev.angussoftware.gradletools

import org.gradle.api.Project
import java.time.Duration

/**
 * Encapsulates the build-failure analysis flow invoked at end of build.
 * Builds a prompt, optionally redacts paths, calls the local Ollama CLI, and returns the analysis text.
 */
internal object BuildFailureAnalysis {
    fun analyze(
        project: Project,
        failure: Throwable,
        model: String,
        ollamaCmd: String,
        timeoutSec: Long,
        maxPrompt: Int,
        redact: Boolean,
    ): String {
        val logger = project.logger
        val stack = failure.stackTraceToString()
        val clippedStack = AiText.clip(stack, maxPrompt / 2) // keep room for headers
        val envMeta =
            buildString {
                appendLine("Gradle: ${project.gradle.gradleVersion}")
                appendLine("Root project: ${project.rootProject.name}")
                appendLine("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
                appendLine("Java: ${System.getProperty("java.version")} (${System.getProperty("java.vendor")})")
            }
        val promptRaw =
            """
            You are an expert Gradle build assistant. Analyze the failure and propose a root cause and concise, actionable fixes.

            ${envMeta.trim()}

            Exception summary: ${failure::class.qualifiedName}: ${failure.message}

            Full stack trace (clipped if too long):
            $clippedStack

            Output format:
            - Root cause hypothesis (1–3 bullets)
            - What to try next (ordered, specific commands/edits)
            - Any relevant Gradle flags or diagnostics to run
            """.trimIndent()

        val prompt = if (redact) AiText.redactSensitive(promptRaw, project.rootDir) else promptRaw
        val clippedPrompt = AiText.clip(prompt, maxPrompt)

        logger.lifecycle(
            "[BuildFailure] Build failed; starting analysis (model=$model, timeout=${timeoutSec}s, maxPrompt=$maxPrompt, redact=$redact).",
        )
        logger.lifecycle("[BuildFailure] Prompt prepared (length=${clippedPrompt.length} chars).")
        logger.lifecycle("[BuildFailure] Invoking Ollama CLI '$ollamaCmd'... This may take a while.")

        val startNs = System.nanoTime()
        val analysis =
            try {
                OllamaClient.run(
                    config =
                        OllamaClient.Config(
                            command = ollamaCmd,
                            model = model,
                            workingDir = project.rootDir,
                            timeout = Duration.ofSeconds(timeoutSec),
                            logTag = "BuildFailure",
                        ),
                    prompt = clippedPrompt,
                    logger = logger,
                )
            } catch (e: Exception) {
                logger.lifecycle("[BuildFailure] Ollama invocation failed: ${e::class.simpleName}: ${e.message}")
                "Build failure analysis failed: ${e::class.simpleName}: ${e.message}"
            }.also {
                val durSec = (System.nanoTime() - startNs) / 1_000_000_000.0
                logger.lifecycle("[BuildFailure] Analysis finished in ${"%.1f".format(durSec)}s (responseLength=${it.length}).")
            }

        return analysis
    }
}
