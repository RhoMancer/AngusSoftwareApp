package dev.angussoftware.aidoctor

import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.time.Duration

/**
 * AI Doctor Gradle plugin (local Ollama CLI).
 *
 * What it does
 * - Registers a single end-of-build hook. If the build failed and the feature is branchDoctorEnabled, it asks the
 *   local Ollama CLI to analyze the failure and prints a diagnosis to the Gradle console.
 * - Provides a demo task `aiDoctorFail` that intentionally fails so you can see the diagnosis flow end-to-end.
 *
 * Activation and scope
 * - Opt-in only. Enable per run with `-PaiDoctor=true`.
 * - The end-of-build listener is NOT registered when the configuration cache is requested; Gradle forbids
 *   listeners in that mode. Use `--no-configuration-cache` when you want the AI diagnosis to run.
 * - Guarded for CI: if `CI=true`, the plugin skips unless `-PaiDoctorCiEnabled=true` is provided.
 *
 * Ollama invocation
 * - Uses the local CLI: `ollama run <model>`, feeding the prompt via STDIN (portable across CLI versions).
 *   This avoids reliance on CLI flags like `-p` which may not be available on all versions.
 * - No HTTP requests are made.
 *
 * Redaction and privacy
 * - By default, the prompt sent to the model has the user HOME and project root paths redacted as `<HOME>`
 *   and `<PROJECT_ROOT>` respectively. Disable with `-PaiDoctorRedact=false` if needed.
 *
 * Defaults and toggles
 * - Model default: `gemma3` (override with `-PaiDoctorModel=...`).
 * - Timeout default: 60 seconds (override with `-PaiDoctorTimeoutSec=<n>`, range 5–120).
 * - Prompt length clipping: default 6000 characters (override with `-PaiDoctorMaxPrompt=<n>`, range 1000–30000).
 *
 * Demo task note
 * - The sample `aiDoctorFail` task reads project properties to form its message at execution time and will
 *   not be compatible with the configuration cache. Run it with `--no-configuration-cache`.
 *
 * Properties overview
 * - `-PaiDoctor=true`                 Enable the feature for this build.
 * - `-PaiDoctorModel=<name>`          Ollama model name (default `gemma3`).
 * - `-PaiDoctorOllamaCmd=<path>`      Path or executable name of the Ollama CLI (`ollama`/`ollama.exe`).
 * - `-PaiDoctorTimeoutSec=<n>`        CLI timeout in seconds (default 60, 5–120).
 * - `-PaiDoctorMaxPrompt=<n>`         Max prompt size (default 6000, 1000–30000).
 * - `-PaiDoctorRedact=<true|false>`   Redact HOME and PROJECT_ROOT in prompt (default true).
 * - `-PaiDoctorCiEnabled=<true|false>`Allow running in CI when `CI=true` (default false).
 *
 * Demo message properties (for :aiDoctorFail)
 * - `-PaiMessage="..."` or `-PaiDoctorMessage="..."` to customize the thrown failure message.
 */
class AiDoctorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Register sample failing task for quick demo
        registerSampleFailTask(project)

        // Read config providers lazily where practical
        val enabledProvider = project.providers.gradleProperty("aiDoctor")
        val modelProvider = project.providers.gradleProperty("aiDoctorModel")
        val cmdProvider = project.providers.gradleProperty("aiDoctorOllamaCmd")
        val timeoutSecProvider = project.providers.gradleProperty("aiDoctorTimeoutSec")
        val maxPromptProvider = project.providers.gradleProperty("aiDoctorMaxPrompt")
        val redactProvider = project.providers.gradleProperty("aiDoctorRedact")
        val ciEnabledProvider = project.providers.gradleProperty("aiDoctorCiEnabled")

        // Hook once per build, at the end. Register only when explicitly branchDoctorEnabled and when
        // configuration cache is NOT requested; otherwise, Gradle forbids listener registration.
        val willEnable = (enabledProvider.orNull == "true")
        val ccRequested =
            try {
                project.gradle.startParameter.isConfigurationCacheRequested
            } catch (e: Throwable) {
                false
            }
        if (willEnable) {
            if (ccRequested) {
                project.logger.lifecycle(
                    "[AI Doctor] Configuration cache requested; skipping AI diagnosis listener. Run with --no-configuration-cache to enable.",
                )
            } else {
                project.gradle.buildFinished(
                    object : Action<BuildResult> {
                        override fun execute(result: BuildResult) {
                            val enabled = (enabledProvider.orNull == "true")
                            if (!enabled) return

                            val isCi = Os.isCi()
                            val ciEnabled = (ciEnabledProvider.orNull == "true")
                            if (isCi && !ciEnabled) {
                                project.logger.lifecycle("[AI Doctor] Skipped on CI (aiDoctorCiEnabled=false).")
                                return
                            }

                            val failure = result.failure ?: return

                            // Determine model/timeout with precedence (no module coupling):
                            // - Command line or this project's gradle.properties via providers
                            // - Built-in defaults
                            val model = modelProvider.orNull ?: "gemma3"
                            val ollamaCmd = cmdProvider.orNull ?: Os.defaultOllamaCommand()
                            val timeoutSec = (timeoutSecProvider.orNull?.toLongOrNull() ?: 60L).coerceIn(5L, 120L)
                            val maxPrompt = (maxPromptProvider.orNull?.toIntOrNull() ?: 6000).coerceIn(1000, 30000)
                            val redact =
                                when (redactProvider.orNull?.lowercase()) {
                                    null, "", "true", "1", "yes", "y" -> true
                                    else -> false
                                }

                            // Build prompt
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

                            project.logger.lifecycle(
                                "[AI Doctor] Build failed; starting AI diagnosis (model=$model, timeout=${timeoutSec}s, maxPrompt=$maxPrompt, redact=$redact).",
                            )
                            project.logger.lifecycle("[AI Doctor] Prompt prepared (length=${clippedPrompt.length} chars).")
                            project.logger.lifecycle("[AI Doctor] Invoking Ollama CLI '$ollamaCmd'... This may take a while.")
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
                                                logTag = "AI Doctor",
                                            ),
                                        prompt = clippedPrompt,
                                        logger = project.logger,
                                    )
                                } catch (e: Exception) {
                                    project.logger.lifecycle("[AI Doctor] Ollama invocation failed: ${e::class.simpleName}: ${e.message}")
                                    "AI analysis failed: ${e::class.simpleName}: ${e.message}"
                                }.also {
                                    val durSec = (System.nanoTime() - startNs) / 1_000_000_000.0
                                    project.logger.lifecycle(
                                        "[AI Doctor] AI diagnosis finished in ${"%.1f".format(durSec)}s (responseLength=${it.length}).",
                                    )
                                }

                            project.logger.quiet(
                                "\n================ AI diagnosis (model=$model) ================\n$analysis\n===================================================================\n",
                            )
                        }
                    },
                )
            }
        }
    }

    private fun registerSampleFailTask(project: Project) {
        val tp = project.tasks.register("aiDoctorFail", AiDoctorFailTask::class.java)
        tp.configure {
            group = "Help"
            description = "Intentionally fails the build to demo AI diagnosis. Provide message with -PaiMessage=..."
        }
    }
}

/**
 * Demo task that intentionally fails to trigger the end-of-build AI diagnosis.
 *
 * Configuration cache: this task reads project properties at execution time,
 * so it is not compatible with the configuration cache. Run with
 * --no-configuration-cache when using this demo task.
 *
 * Pass the message via -PaiMessage or -PaiDoctorMessage.
 */
abstract class AiDoctorFailTask : DefaultTask() {
    @TaskAction
    fun failNow() {
        val msg =
            (
                project.findProperty("aiMessage")
                    ?: project.findProperty("aiDoctorMessage")
                    ?: "Sample failure triggered by aiDoctorFail"
            ).toString()
        throw GradleException("AI Doctor sample failure: $msg")
    }
}
