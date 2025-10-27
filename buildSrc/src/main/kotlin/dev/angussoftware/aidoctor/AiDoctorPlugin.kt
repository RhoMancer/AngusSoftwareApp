package dev.angussoftware.aidoctor

import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * AI Doctor Gradle plugin (local Ollama CLI).
 *
 * What it does
 * - Registers a single end-of-build hook. If the build failed and the feature is enabled, it asks the
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

        // Hook once per build, at the end. Register only when explicitly enabled and when
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

                            val isCi = System.getenv("CI")?.equals("true", ignoreCase = true) == true
                            val ciEnabled = (ciEnabledProvider.orNull == "true")
                            if (isCi && !ciEnabled) return

                            val failure = result.failure ?: return

                            val model = modelProvider.orNull ?: "gemma3"
                            val ollamaCmd = cmdProvider.orNull ?: defaultOllamaCommand()
                            val timeoutSec = (timeoutSecProvider.orNull?.toLongOrNull() ?: 60L).coerceIn(5L, 120L)
                            val maxPrompt = (maxPromptProvider.orNull?.toIntOrNull() ?: 6000).coerceIn(1000, 30000)
                            val redact =
                                when (redactProvider.orNull?.lowercase()) {
                                    null, "", "true", "1", "yes", "y" -> true
                                    else -> false
                                }

                            // Build prompt
                            val stack = failure.stackTraceToString()
                            val clippedStack = clip(stack, maxPrompt / 2) // keep room for headers
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

                            val prompt = if (redact) redactSensitive(promptRaw, project) else promptRaw
                            val clippedPrompt = clip(prompt, maxPrompt)

                            val analysis =
                                try {
                                    askOllamaCli(
                                        command = ollamaCmd,
                                        model = model,
                                        prompt = clippedPrompt,
                                        workingDir = project.rootDir,
                                        timeout = Duration.ofSeconds(timeoutSec),
                                    )
                                } catch (e: Exception) {
                                    "AI analysis failed: ${e::class.simpleName}: ${e.message}"
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

    private fun defaultOllamaCommand(): String = if (isWindows()) "ollama.exe" else "ollama"

    private fun isWindows(): Boolean = System.getProperty("os.name").lowercase().contains("win")

    private fun clip(
        text: String,
        maxLen: Int,
    ): String = if (text.length <= maxLen) text else text.take(maxLen) + "\n... [clipped]"

    private fun redactSensitive(
        input: String,
        project: Project,
    ): String {
        var out = input
        val home = System.getProperty("user.home")?.let { File(it).absolutePath }
        val root = project.rootDir.absolutePath
        if (!home.isNullOrBlank()) {
            // Normalize slashes for both Windows and Unix paths
            val homeEscaped = Regex.escape(home)
            out = out.replace(Regex(homeEscaped, RegexOption.IGNORE_CASE), "<HOME>")
        }
        val rootEscaped = Regex.escape(root)
        out = out.replace(Regex(rootEscaped, RegexOption.IGNORE_CASE), "<PROJECT_ROOT>")
        return out
    }

    private fun askOllamaCli(
        command: String,
        model: String,
        prompt: String,
        workingDir: File,
        timeout: Duration,
    ): String {
        // Use: `ollama run <model>` and feed the prompt via STDIN (portable across CLI versions)
        val cmd = listOf(command, "run", model)
        val processBuilder =
            ProcessBuilder(cmd)
                .directory(workingDir)
                .redirectErrorStream(false)

        val process: Process =
            try {
                processBuilder.start()
            } catch (e: Exception) {
                throw GradleException("Failed to start Ollama CLI ('$command'). Is it installed and on PATH? ${e.message}")
            }

        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val outThread = Thread { process.inputStream.copyTo(stdout) }
        val errThread = Thread { process.errorStream.copyTo(stderr) }
        outThread.start()
        errThread.start()

        // Write the prompt to stdin and close it, so the CLI starts generating
        try {
            val bytes = prompt.toByteArray(Charsets.UTF_8)
            process.outputStream.use { os ->
                os.write(bytes)
                os.flush()
            }
        } catch (_: Exception) {
            // Ignore stdin write errors; we'll surface stderr/stdout below
        }

        val finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)
        if (!finished) {
            process.destroyForcibly()
            outThread.join(100)
            errThread.join(100)
            return "Ollama timed out after ${timeout.seconds}s. You can increase with -PaiDoctorTimeoutSec."
        }

        outThread.join(200)
        errThread.join(200)

        val code = process.exitValue()
        val outStr = stdout.toString("UTF-8")
        val errStr = stderr.toString("UTF-8")

        if (code != 0 && outStr.isBlank()) {
            return "Ollama exited with code $code. Error: ${errStr.ifBlank { "<no stderr>" }}"
        }
        // The CLI prints the model’s full response to stdout
        return outStr.ifBlank { errStr.ifBlank { "<no output>" } }
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
