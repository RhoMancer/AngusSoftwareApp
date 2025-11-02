# Angus Gradle Tools — Build Failure Analysis and Branch Coverage Gaps

This repository’s `buildSrc` module provides two reusable Gradle capabilities that leverage a local Ollama LLM:
- AngusGradleToolsPlugin (plugin id `dev.angussoftware.gradle-tools.failure-analysis`) — prints a build‑failure analysis to the Gradle console when a build fails (end‑of‑build listener; opt‑in per run).
- BranchCoverageGapsReportTask — parses a JaCoCo XML report to highlight missed branches, maps them back to source, emits JSON/Markdown reports, and can optionally include AI suggestions for improving test coverage.

Both live under `buildSrc`, so they’re automatically available to all modules in this build (including `composeApp`).

---

## Requirements
- A local Ollama CLI installation available on PATH, or pass the full path via properties.
- A pulled model (examples: `gemma3`, `llama3.1:8b`, `qwen2.5-coder:7b`).

Quick check:
```bat
ollama pull gemma3

echo "Say hi" | ollama run gemma3
```

---

## Components

### AngusGradleToolsPlugin — end‑of‑build failure analysis
- What it does
  - Registers a single `buildFinished` listener (only when explicitly enabled).
  - If the build failed, it builds a detailed prompt, sends it to Ollama via STDIN (`ollama run <model>`), and prints the model’s response under a banner in the Gradle output.
- How it works (implementation details)
  - Prompt composition is encapsulated in `BuildFailureAnalysis.analyze(...)`:
    - Collects environment metadata (Gradle version, root project, OS/Java), the exception summary, and the full stack trace.
    - Clips the stack trace and the final prompt to stay within `maxPrompt` (clamped to 1000–30000 chars).
    - Optionally redacts the user HOME and project root paths with `<HOME>` and `<PROJECT_ROOT>`.
    - Invokes `OllamaClient.run(...)` which streams STDOUT/STDERR, prints a 1‑second progress ticker, enforces a timeout, and returns partial output with a timeout note if time is exceeded.
  - CI/configuration cache behavior:
    - When `CI=true` the plugin is skipped unless `-PbuildFailureCiEnabled=true` is set.
    - The listener is NOT registered when the configuration cache is requested; use `--no-configuration-cache` to enable diagnostics for that run.
- Config flags and defaults
  - `-PbuildFailureEnabled=true`        Enable build‑failure analysis for this build (required to activate)
  - `-PbuildFailureModel=<name>`        Model (default `gemma3`)
  - `-PbuildFailureOllamaCmd=<path>`    Ollama CLI (`ollama`/`ollama.exe`) — default auto‑detected
  - `-PbuildFailureTimeoutSec=<n>`      Timeout seconds (default 60, range 5–120)
  - `-PbuildFailureMaxPrompt=<n>`       Max prompt size (default 6000, range 1000–30000)
  - `-PbuildFailureRedact=<true|false>` Redact HOME/PROJECT_ROOT (default true)
  - `-PbuildFailureCiEnabled=<true|false>` Allow on CI when `CI=true` (default false)
- Demo task (in this repo)
  - `composeApp/gradle/build-failure-demo-task.gradle.kts` registers `:composeApp:composeAppBuildFailureDemo`.
  - Examples:
    ```bat
    gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureMessage="Explain why this sample failed"
    gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureModel=llama3.1:8b
    gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"
    ```

Sources:
- Plugin: `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/AngusGradleToolsPlugin.kt`
- Failure analysis: `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/BuildFailureAnalysis.kt`

### BranchCoverageGapsReportTask — JaCoCo branch‑gaps reporter with optional AI guidance
- What it does
  - Parses a JaCoCo XML report to collect lines with missed branches (`mb > 0`).
  - Locates the corresponding source lines using configured source roots and emits:
    - `outputJson`: machine‑readable summary of files/lines, context windows, and classifications.
    - `outputMd`: human‑readable Markdown with numbered code windows, highlighting the missed line (`▶`).
    - `outputAiMd`: optional AI suggestions (includes the exact prompt sent and the model’s response).
    - `outputMeta`: metadata JSON capturing all flags, prompt details, and output paths.
  - Can enforce thresholds and fail the build if too many branches are missed (globally or per file).
- How it works (implementation details)
  - XML parsing disables external DTD/entity resolution and installs an empty entity resolver to avoid filesystem/network access.
  - Source context windows are built per line; `contextLines=-1` includes the whole file.
  - Lines eligible for AI require covered branches `cb >= branchCoverageMinCoveredBranchesForAi` (default 1). Selection prioritizes higher coverage ratio (`cb/(cb+mb)`), then `cb`, then file/line ordering.
  - Prompts are clipped to `maxPrompt` (1000–30000 clamp) and optionally redacted. Ollama is invoked via `OllamaClient.run(...)` with a timeout and progress ticker.
- Key inputs (set when registering the task)
  - `xmlReport` (`RegularFileProperty`) — path to the JaCoCo XML report.
  - `sourceRoots` (`ListProperty<String>`) — absolute paths to source directories (e.g., `src/commonMain/kotlin`, `src/androidMain/kotlin`).
  - Feature flags: `branchCoverageEnabled`, `aiEnabled`, `ciEnabled`.
  - Tuning: `contextLines` (default 5; `-1` includes whole file), optional `topNFiles` (limit output to top offenders).
  - Thresholds: optional `failIfMissedBranches` (global), `failIfMissedBranchesPerFile`.
  - AI: `model`, `ollamaCmd`, `timeoutSec`, `maxPrompt`, `redact`.
- Outputs (you choose the file paths in your build script)
  - `outputJson`, `outputMd`, `outputAiMd`, `outputMeta`.
- Threshold enforcement
  - If any file exceeds `failIfMissedBranchesPerFile`, the task throws a `GradleException`.
  - If the global total exceeds `failIfMissedBranches`, the task throws a `GradleException`.

Source: `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/BranchCoverageGapsReportTask.kt`

#### Coverage plugin (optional, auto-registers the task)
- Plugin ID: `dev.angussoftware.gradle-tools.coverage`
- Class: `dev.angussoftware.gradletools.AngusCoveragePlugin`
- What it does: when applied to a module, registers a task named `androidBranchCoverageGaps` and wires sensible defaults.
- Defaults:
  - `xmlReport`: `build/reports/jacoco/androidConnectedTest/report.xml`
  - `sourceRoots`: `src/commonMain/kotlin`, `src/androidMain/kotlin`
  - If a task named `androidConnectedTestCoverageReport` exists, the registered task depends on it.
- Minimal extension:
  - `angusCoverage.xmlReport` (RegularFile)
  - `angusCoverage.sourceRoots` (List<String>)
- It also respects all `branchCoverage*` `-P` properties documented below.
- Usage per module:
```
plugins {
    id("dev.angussoftware.gradle-tools.coverage")
}

angusCoverage {
    // optional overrides
    // xmlReport = layout.buildDirectory.file("reports/jacoco/androidConnectedTest/report.xml")
    // sourceRoots.set(listOf("src/commonMain/kotlin", "src/androidMain/kotlin"))
}
```

---

## Shared utilities
- `OllamaClient` — common process runner for `ollama run <model>` with STDIN prompt, stdout/stderr threads, progress ticker, timeout handling, and consistent return policy.
- `AiText` — shared redaction (HOME/PROJECT_ROOT) and prompt clipping (`1000..30000` clamp).
- `Os` — `defaultOllamaCommand()` and `isCi()`.
- `AiGate` — central CI gating (`aiEnabled && (!CI || ciEnabled)`).
- `jsonEsc` — simple JSON string escape used by report writers.

Source: `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/GradleToolsCommon.kt`

---

## Using these in this repository (composeApp)
This repo wires the coverage task in `composeApp/gradle/coverage-tasks.gradle.kts`. It produces outputs next to the JaCoCo XML report:
- `composeApp/build/reports/jacoco/androidConnectedTest/branch-gaps.json`
- `composeApp/build/reports/jacoco/androidConnectedTest/branch-gaps.md`
- `composeApp/build/reports/jacoco/androidConnectedTest/branch-gaps-ai.md`
- `composeApp/build/reports/jacoco/androidConnectedTest/branch-gaps.meta.json`
- JaCoCo HTML: `composeApp/build/reports/jacoco/androidConnectedTest/html/index.html`

Convenience tasks in `composeApp`:
- `androidConnectedTestCoverageReport` — runs `connectedDebugAndroidTest` and generates JaCoCo HTML/XML (XML at `reports/jacoco/androidConnectedTest/report.xml`).
- `androidBranchCoverageGaps` — runs `BranchCoverageGapsReportTask` over that XML.
- `androidInstrumentedCoverageWithBranchGaps` — runs both in sequence.
- `fullCoverageReport` — runs unit (Kover) and instrumented coverage.

Examples:
```bat
:: Generate instrumented coverage + branch gaps report (no AI)
gradlew :composeApp:androidInstrumentedCoverageWithBranchGaps -PbranchCoverageEnabled=true

:: Include AI suggestions too (allowed on CI only if branchCoverageCiEnabled=true)
gradlew :composeApp:androidInstrumentedCoverageWithBranchGaps -PbranchCoverageEnabled=true -PbranchCoverageAiEnabled=true -PbranchCoverageCiEnabled=true
```

AI configuration properties used by the composeApp wiring:
- `-PbranchCoverageEnabled=true|false`           Master switch to run the task.
- `-PbranchCoverageAiEnabled=true|false`        Turn on AI suggestions.
- `-PbranchCoverageCiEnabled=true|false`        Allow AI on CI when `CI=true`.
- `-PbranchCoverageContextLines=<int>`          Context lines around each missed line (default 5; `-1` = whole file).
- `-PbranchCoverageTopNFiles=<int>`             Limit output to top N files by missed branches.
- `-PbranchCoverageFailIfMissedBranches=<int>`  Fail build if total missed branches exceed this number.
- `-PbranchCoverageFailIfMissedBranchesPerFile=<int>`  Per-file fail threshold.
- `-PbranchCoverageModel=<name>`                Ollama model (default `gemma3`).
- `-PbranchCoverageOllamaCmd=<path|name>`       Ollama CLI (default auto; `ollama` or `ollama.exe` on Windows).
- `-PbranchCoverageTimeoutSec=<5..120>`         CLI timeout seconds (default 60).
- `-PbranchCoverageMaxPrompt=<1000..30000>`     Max prompt characters (default 6000).
- `-PbranchCoverageRedact=true|false`           Redact HOME/PROJECT_ROOT in prompt (default true).
- `-PbranchCoverageMinCoveredBranchesForAi=<int>` Minimum covered branches (cb) to include a line in AI selection (default 1).
- `-PbranchCoverageMaxAiAnalyses=<int>`         Cap the number of lines included in AI analysis across all files (default 20).

Notes:
- The XML is generated at `composeApp/build/reports/jacoco/androidConnectedTest/report.xml` by the provided task.
- Source roots are configured to `src/commonMain/kotlin` and `src/androidMain/kotlin`.

---

## Build-failure analysis demo in this repository
- Demo task file: `composeApp/gradle/build-failure-demo-task.gradle.kts`
- Task name: `:composeApp:composeAppBuildFailureDemo`
- Examples:
```bat
:: Demo the end-of-build diagnosis
gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureMessage="Diagnose this sample failure"

:: Use a specific model and Ollama path
gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureModel=llama3.1:8b -PbuildFailureOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"
```

Tip: The analysis listener is skipped when the configuration cache is requested. Run with `--no-configuration-cache` or adjust your module’s policy.

---

## Why two plugins?
- Failure analysis is a root-scoped feature that registers an end-of-build listener, so it’s implemented as a root-applied plugin (`dev.angussoftware.gradle-tools.failure-analysis`).
- Branch coverage gaps reporting is cacheable work with module-specific inputs/outputs, so it’s implemented as a task registered per module via a small plugin (`dev.angussoftware.gradle-tools.coverage`).
- This split keeps configuration local and avoids surprising auto-behavior across subprojects. If you prefer a one-line setup later, we can add a tiny bundle plugin that simply applies both.

## Migration (old → new)
This repository previously used the "AI Doctor" naming. It has been fully replaced:
- Plugin ID: `dev.angussoftware.ai-doctor` → `dev.angussoftware.gradle-tools.failure-analysis`
- Plugin class: `AiDoctorPlugin` → `AngusGradleToolsPlugin`
- Coverage task class: `BranchCoverageDoctorTask` → `BranchCoverageGapsReportTask`
- Properties (plugin): `aiDoctor*` → `buildFailure*`
- Properties (coverage): `branchDoctor*` → `branchCoverage*`
Note: there is no back‑compat shim here; use the new names going forward.

---

## Troubleshooting
- No analysis banner printed
  - The build likely did not fail, or the configuration cache was enabled (listener is skipped). Use `--no-configuration-cache` and ensure `-PbuildFailureEnabled=true`.
- `ollama` not found
  - Add Ollama to PATH or pass `-PbuildFailureOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"` (and for branch coverage via `-PbranchCoverageOllamaCmd=...`).
- Timeout or empty output from Ollama
  - Increase timeout (`-PbuildFailureTimeoutSec` / `-PbranchCoverageTimeoutSec`), try a smaller/faster model, or verify `ollama run <model>` works in your shell.
- Prompt too long
  - Adjust `-PbuildFailureMaxPrompt` or `-PbranchCoverageMaxPrompt`.
- Privacy concerns
  - Redaction is on by default. Disable with `-PbuildFailureRedact=false` / `-PbranchCoverageRedact=false` if needed.
- Branch coverage: XML report not found
  - Ensure the JaCoCo report task ran and the `xmlReport` path is correct (composeApp writes to `.../report.xml`).
- Branch coverage: no source context shown
  - Check that `sourceRoots` includes all source directories where files can be found.

---

## Reuse in other projects
Since these live in `buildSrc`, they are automatically available to this build. To reuse elsewhere, copy the `buildSrc` directory into another Gradle project and:
- Apply the plugin (for end‑of‑build analysis) in the root `build.gradle.kts`:
  ```kotlin
  plugins {
      id("dev.angussoftware.gradle-tools.failure-analysis")
  }
  ```
- Register `BranchCoverageGapsReportTask` in the module(s) that produce JaCoCo XML, providing the inputs, flags, and outputs as shown above.

---

## Source files
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/AngusGradleToolsPlugin.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/BuildFailureAnalysis.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/BranchCoverageGapsReportTask.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/GradleToolsCommon.kt`
- This README: `buildSrc/README.md`
