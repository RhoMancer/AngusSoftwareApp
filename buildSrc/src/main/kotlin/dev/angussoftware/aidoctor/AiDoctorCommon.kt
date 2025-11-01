package dev.angussoftware.aidoctor

import org.gradle.api.GradleException
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Shared utilities for AiDoctorPlugin and BranchCoverageDoctorTask.
 * Kept internal to the module and dependency-free.
 */
internal object OllamaClient {
    data class Config(
        val command: String,
        val model: String,
        val workingDir: File,
        val timeout: Duration,
        /** Log prefix inside square brackets, e.g. "AI Doctor" or "BranchDoctor [AI]" */
        val logTag: String = "AI Doctor",
    )

    /**
     * Runs `ollama run <model>` and feeds the prompt via STDIN.
     * Returns stdout when available; otherwise stderr; otherwise "<no output>".
     * On startup failure throws GradleException; on timeout returns partial output plus a timeout note.
     */
    fun run(
        config: Config,
        prompt: String,
        logger: org.gradle.api.logging.Logger,
    ): String {
        val cmd = listOf(config.command, "run", config.model)
        val pb = ProcessBuilder(cmd).directory(config.workingDir).redirectErrorStream(false)
        val proc = try {
            pb.start()
        } catch (e: Exception) {
            throw GradleException("Failed to start Ollama CLI ('${config.command}'). Is it installed and on PATH? ${e.message}")
        }

        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val tOut = Thread { proc.inputStream.copyTo(stdout) }
        val tErr = Thread { proc.errorStream.copyTo(stderr) }
        tOut.start()
        tErr.start()

        // Send prompt and close stdin so generation begins
        try {
            proc.outputStream.use { os ->
                os.write(prompt.toByteArray(Charsets.UTF_8))
                os.flush()
            }
        } catch (_: Exception) {
            // ignore stdin errors; we'll rely on stdout/stderr
        }

        // Progress ticker each second
        val totalSec = config.timeout.seconds.coerceAtLeast(1)
        var elapsedSec = 0L
        while (true) {
            val finishedOneSec = proc.waitFor(1, TimeUnit.SECONDS)
            elapsedSec = (elapsedSec + 1).coerceAtMost(totalSec)
            val remaining = (totalSec - elapsedSec).coerceAtLeast(0)
            logger.lifecycle("[${config.logTag}] ${elapsedSec} / ${totalSec}s , ${remaining}s until AI timeout")
            if (finishedOneSec) break
            if (elapsedSec >= totalSec) break
        }

        if (proc.isAlive) {
            // Timeout path: collect partial output
            logger.lifecycle("[${config.logTag}] Timed out after ${elapsedSec}s. Collecting partial output...")
            proc.destroyForcibly()
            tOut.join(200)
            tErr.join(200)
            val partial = stdout.toString("UTF-8").ifBlank { stderr.toString("UTF-8") }
            return buildString {
                append(partial)
                if (partial.isNotBlank()) append("\n\n")
                append("AI timed out after ${elapsedSec} seconds")
            }
        }

        // Completed
        tOut.join(200)
        tErr.join(200)
        val code = proc.exitValue()
        val out = stdout.toString("UTF-8")
        val err = stderr.toString("UTF-8")
        if (code != 0 && out.isBlank()) return "Ollama exited with code ${code}. Error: ${err.ifBlank { "<no stderr>" }}"
        return out.ifBlank { err.ifBlank { "<no output>" } }
    }
}

internal object AiText {
    fun redactSensitive(
        input: String,
        projectRoot: File,
        userHome: File? = System.getProperty("user.home")?.let(::File),
    ): String {
        var out = input
        userHome?.absolutePath?.takeIf { it.isNotBlank() }?.let { home ->
            val homeEscaped = Regex.escape(home)
            out = out.replace(Regex(homeEscaped, RegexOption.IGNORE_CASE), "<HOME>")
        }
        val rootEscaped = Regex.escape(projectRoot.absolutePath)
        out = out.replace(Regex(rootEscaped, RegexOption.IGNORE_CASE), "<PROJECT_ROOT>")
        return out
    }

    fun clip(
        text: String,
        maxLen: Int,
        hardMin: Int = 1000,
        hardMax: Int = 30000,
    ): String {
        val m = maxLen.coerceIn(hardMin, hardMax)
        return if (text.length <= m) text else text.take(m) + "\n... [clipped]"
    }
}

internal object Os {
    fun defaultOllamaCommand(): String = if (System.getProperty("os.name").lowercase().contains("win")) "ollama.exe" else "ollama"
    fun isCi(): Boolean = System.getenv("CI")?.equals("true", ignoreCase = true) == true
}

internal object AiGate {
    fun shouldRun(
        aiEnabled: Boolean,
        ciEnabled: Boolean,
        ciEnv: Boolean = Os.isCi(),
    ): Boolean = aiEnabled && (!ciEnv || ciEnabled)
}

internal fun jsonEsc(s: String?): String = s?.replace("\\", "\\\\")?.replace("\"", "\\\"")?.replace("\n", "\\n") ?: ""
