package dev.angussoftware.gradletools

import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Angus Gradle Tools plugin — build failure analysis via local Ollama CLI.
 *
 * What it does
 * - Registers a single end-of-build hook. If the build failed and the feature is enabled, it asks the
 *   local Ollama CLI to analyze the failure and prints suggestions to the Gradle console.
 *
 * Activation and scope
 * - Opt-in only. Enable per run with `-PbuildFailureEnabled=true`.
 * - The end-of-build listener is NOT registered when the configuration cache is requested; Gradle forbids
 *   listeners in that mode. Use `--no-configuration-cache` when you want the analysis to run.
 * - Guarded for CI: if `CI=true`, the plugin skips unless `-PbuildFailureCiEnabled=true` is provided.
 *
 * Ollama invocation
 * - Uses the local CLI: `ollama run <model>`, feeding the prompt via STDIN (portable across CLI versions).
 *   This avoids reliance on CLI flags like `-p` which may not be available on all versions.
 * - No HTTP requests are made.
 *
 * Redaction and privacy
 * - By default, the prompt sent to the model has the user HOME and project root paths redacted as `<HOME>`
 *   and `<PROJECT_ROOT>` respectively. Disable with `-PbuildFailureRedact=false` if needed.
 *
 * Defaults and toggles
 * - Model default: `gemma3` (override with `-PbuildFailureModel=...`).
 * - Timeout default: 60 seconds (override with `-PbuildFailureTimeoutSec=<n>`, range 5–120).
 * - Prompt length clipping: default 6000 characters (override with `-PbuildFailureMaxPrompt=<n>`, range 1000–30000).
 */
class AngusFailureAnalysisPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Read config providers lazily where practical (neutral naming)
        val enabledProvider = project.providers.gradleProperty("buildFailureEnabled")
        val modelProvider = project.providers.gradleProperty("buildFailureModel")
        val cmdProvider = project.providers.gradleProperty("buildFailureOllamaCmd")
        val timeoutSecProvider = project.providers.gradleProperty("buildFailureTimeoutSec")
        val maxPromptProvider = project.providers.gradleProperty("buildFailureMaxPrompt")
        val redactProvider = project.providers.gradleProperty("buildFailureRedact")
        val ciEnabledProvider = project.providers.gradleProperty("buildFailureCiEnabled")
        val enforceNoCcProvider = project.providers.gradleProperty("buildFailureEnforceNoConfigCache")

        // Hook once per build, at the end. Register only when explicitly enabled and when
        // configuration cache policy allows it.
        val willEnable = (enabledProvider.orNull == "true")
        val ccRequested =
            try {
                project.gradle.startParameter.isConfigurationCacheRequested
            } catch (e: Throwable) {
                false
            }
        if (willEnable) {
            if (ccRequested) {
                val enforceNoCC = (enforceNoCcProvider.orNull ?: "true").equals("true", ignoreCase = true)
                if (enforceNoCC) {
                    throw org.gradle.api.GradleException(
                        "Configuration cache requested but build-failure analysis is enabled. " +
                            "Rerun with --no-configuration-cache, or override with -PbuildFailureEnforceNoConfigCache=false, " +
                            "or disable analysis with -PbuildFailureEnabled=false. Note: under configuration cache Gradle skips end-of-build listeners, so analysis will not run.",
                    )
                } else {
                    project.logger.lifecycle(
                        "[BuildFailure] Configuration cache requested; skipping build-failure analysis listener (enforcement disabled).",
                    )
                }
            } else {
                project.gradle.buildFinished(
                    object : Action<BuildResult> {
                        override fun execute(result: BuildResult) {
                            val enabled = (enabledProvider.orNull == "true")
                            if (!enabled) return

                            val isCi = Os.isCi()
                            val ciEnabled = (ciEnabledProvider.orNull == "true")
                            if (isCi && !ciEnabled) {
                                project.logger.lifecycle("[BuildFailure] Skipped on CI (buildFailureCiEnabled=false).")
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

                            val analysis =
                                BuildFailureAnalysis.analyze(
                                    project = project,
                                    failure = failure,
                                    model = model,
                                    ollamaCmd = ollamaCmd,
                                    timeoutSec = timeoutSec,
                                    maxPrompt = maxPrompt,
                                    redact = redact,
                                )

                            project.logger.quiet(
                                "\n================ Build failure analysis (model=$model) ================\n$analysis\n===================================================================\n",
                            )
                        }
                    },
                )
            }
        }
    }
}
