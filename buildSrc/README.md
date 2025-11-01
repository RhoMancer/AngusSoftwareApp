# Angus Gradle Tools — Build Failure Analysis and Branch Coverage Gaps Task

This repository’s buildSrc module provides two reusable capabilities that leverage a local Ollama LLM:
- AngusGradleToolsPlugin (plugin id `dev.angussoftware.gradle-tools`) — prints a build-failure analysis to the Gradle console when a build fails (end-of-build listener, opt‑in per run).
- BranchCoverageGapsReportTask — parses a JaCoCo XML report to highlight missed branches, maps them back to source, emits JSON/Markdown reports, and can optionally include AI suggestions for improving test coverage.

Both live under buildSrc, so they’re automatically available to all modules in this build (including composeApp).

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
  - Registers a single buildFinished listener (only when explicitly enabled).
  - If the build failed, sends a prompt to Ollama via STDIN (`ollama run <model>`) containing Gradle/OS/Java metadata, the exception summary, and a clipped stack trace.
  - Prints the model’s response under an “AI diagnosis” banner in the Gradle output.
- Activation and constraints
  - Opt‑in per run with `-PbuildFailureEnabled=true`.
  - The listener is NOT registered when the configuration cache is requested; run with `--no-configuration-cache` when you want the diagnosis.
  - CI: when `CI=true`, the plugin is skipped by default; enable with `-PbuildFailureCiEnabled=true`.
- Redaction and privacy
  - By default, HOME and the project root paths are redacted to `<HOME>` and `<PROJECT_ROOT>` in the prompt. Disable with `-PbuildFailureRedact=false`.
- Config flags and defaults
  - `-PbuildFailureEnabled=true`      Enable build-failure analysis for this build (required to activate)
  - `-PbuildFailureModel=<name>`     Model (default `gemma3`)
  - `-PbuildFailureOllamaCmd=<path>`  Ollama CLI (`ollama`/`ollama.exe`) — default auto-detected
  - `-PbuildFailureTimeoutSec=<n>`    Timeout seconds (default 60, range 5–120)
  - `-PbuildFailureMaxPrompt=<n>`     Max prompt size (default 6000, range 1000–30000)
  - `-PbuildFailureRedact=<true|false>` Redact HOME/PROJECT_ROOT (default true)
  - `-PbuildFailureCiEnabled=<true|false>` Allow on CI when `CI=true` (default false)
- Demo task
  - A demo task is provided in `composeApp/gradle/build-failure-demo-task.gradle.kts` as `:composeApp:composeAppBuildFailureDemo`.
  - Examples:
    ```bat
    gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureMessage="Explain why this sample failed"
    gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureModel=llama3.1:8b
    gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"
    ```

Source: buildSrc/src/main/kotlin/dev/angussoftware/gradletools/AngusGradleToolsPlugin.kt


### BranchCoverageGapsReportTask — JaCoCo branch‑gaps reporter with optional AI guidance
- What it does
  - Parses a JaCoCo XML report to collect lines with missed branches (`mb > 0`).
  - Locates the corresponding source lines using configured source roots and emits:
    - outputJson: machine‑readable summary of files/lines, context windows, and classifications.
    - outputMd: human‑readable Markdown with numbered code windows, highlighting the missed line (`▶`).
    - outputAiMd: optional AI suggestions (includes the exact prompt sent and the model’s response).
    - outputMeta: metadata JSON capturing all flags, prompt details, and output paths.
  - Can enforce thresholds and fail the build if too many branches are missed (globally or per file).
- Key inputs (set when registering the task)
  - xmlReport (RegularFileProperty) — path to the JaCoCo XML report.
  - sourceRoots (ListProperty<String>) — absolute paths to source directories (e.g., `src/commonMain/kotlin`, `src/androidMain/kotlin`).
  - Feature flags: branchCoverageEnabled, aiEnabled, ciEnabled.
  - Tuning: contextLines (default 5; `-1` includes whole file), optional topNFiles (limit output to top offenders).
  - Thresholds: optional failIfMissedBranches (global), failIfMissedBranchesPerFile.
  - AI: model, ollamaCmd, timeoutSec, maxPrompt, redact.
- Outputs (you choose the file paths in your build script)
  - outputJson, outputMd, outputAiMd, outputMeta.
- AI behavior and CI gating
  - AI suggestions run only when aiEnabled=true. If `CI=true`, they additionally require ciEnabled=true.
  - Lines are eligible for AI only if their covered branches (cb) >= `branchDoctorMinCoveredBranchesForAi` (default 1). By default, lines with cb=0 are not sent to AI.
  - The prompt is clipped to maxPrompt characters and can redact HOME/PROJECT_ROOT.
  - Ollama is invoked as `listOf(command, "run", model)`; the prompt is fed via STDIN; a timeout is enforced.
- Threshold enforcement
  - If any file exceeds failIfMissedBranchesPerFile, the task throws a GradleException.
  - If the global total exceeds failIfMissedBranches, the task throws a GradleException.
- Classification heuristics
  - Heuristically labels the missed context as `when-expression`, `if-else`, `if`, `boolean-op`, `elvis`, `safe-call`, `not-null-assertion`, `early-return`, or `unknown`.
- XML parser safety
  - External DTD/entity resolution is disabled and an empty EntityResolver is installed to avoid network/filesystem lookups (prevents JaCoCo DTD errors and XXE risks).

Source: buildSrc/src/main/kotlin/dev/angussoftware/gradletools/BranchCoverageGapsReportTask.kt

---

## Using these in composeApp
This repo wires BranchCoverageDoctorTask for composeApp in composeApp/gradle/coverage-tasks.gradle.kts. It produces outputs next to the JaCoCo XML report:
- composeApp\build\reports\jacoco\androidConnectedTest\branch-gaps.json
- composeApp\build\reports\jacoco\androidConnectedTest\branch-gaps.md
- composeApp\build\reports\jacoco\androidConnectedTest\branch-gaps-ai.md
- composeApp\build\reports\jacoco\androidConnectedTest\branch-gaps.meta.json
- JaCoCo HTML: composeApp\build\reports\jacoco\androidConnectedTest\html\index.html

Convenience tasks in composeApp:
- androidConnectedTestCoverageReport — runs connectedDebugAndroidTest and generates JaCoCo HTML/XML (XML at reports/jacoco/androidConnectedTest/report.xml).
- androidBranchCoverageGaps — runs BranchCoverageGapsReportTask over that XML.
- androidInstrumentedCoverageWithBranchGaps — runs both in sequence.

Examples:
```bat
:: Generate coverage + branch doctor reports (no AI)
gradlew :composeApp:androidInstrumentedCoverageWithBranchDoctor -PbranchDoctorEnabled=true

:: Include AI suggestions too (allowed on CI only if branchDoctorCiEnabled=true)
gradlew :composeApp:androidInstrumentedCoverageWithBranchDoctor -PbranchDoctorEnabled=true -PbranchDoctorAiEnabled=true
```

AI configuration properties used by composeApp wiring:
- -PbranchDoctorEnabled=true|false           Master switch to run the task.
- -PbranchDoctorAiEnabled=true|false         Turn on AI suggestions.
- -PbranchDoctorCiEnabled=true|false         Allow AI on CI when CI=true.
- -PbranchDoctorContextLines=<int>           Context lines around each missed line (default 5; -1 = whole file).
- -PbranchDoctorTopNFiles=<int>              Limit output to top N files by missed branches.
- -PbranchDoctorFailIfMissedBranches=<int>   Fail build if total missed branches exceed this number.
- -PbranchDoctorFailIfMissedBranchesPerFile=<int>  Per-file fail threshold.
- -PbranchDoctorModel=<name>                 Ollama model (default gemma3).
- -PbranchDoctorOllamaCmd=<path|name>        Ollama CLI (default auto; `ollama` or `ollama.exe` on Windows).
- -PbranchDoctorTimeoutSec=<5..120>          CLI timeout seconds (default 60).
- -PbranchDoctorMaxPrompt=<1000..30000>      Max prompt characters (default 6000).
- -PbranchDoctorRedact=true|false            Redact HOME/PROJECT_ROOT in prompt (default true).
- -PbranchDoctorMinCoveredBranchesForAi=<int> Minimum covered branches (cb) for a line to be considered by AI (default 1).
- -PbranchDoctorMaxAiAnalyses=<int>     Maximum number of lines to include in AI analysis across all files (default 20).

Notes:
- The XML is generated at composeApp/build/reports/jacoco/androidConnectedTest/report.xml by the provided task.
- Source roots are configured to src/commonMain/kotlin and src/androidMain/kotlin.

---

## Using AiDoctorPlugin alongside the branch doctor
If a threshold in BranchCoverageDoctorTask fails the build, and AiDoctorPlugin is enabled for the run, you’ll also see an end‑of‑build “AI diagnosis” in the Gradle console describing the failure.

Examples:
```bat
:: Demo the end-of-build diagnosis
gradlew aiDoctorFail --no-configuration-cache -PaiDoctor=true -PaiMessage="Diagnose this sample failure"

:: Run coverage with branch doctor and enable end-of-build diagnosis too
gradlew :composeApp:androidInstrumentedCoverageWithBranchDoctor -PbranchDoctorEnabled=true -PbranchDoctorAiEnabled=true --no-configuration-cache -PaiDoctor=true
```

---

## Troubleshooting
- No “AI diagnosis” banner printed
  - The build likely did not fail, or the configuration cache was enabled (listener is skipped). Use `--no-configuration-cache` and ensure `-PaiDoctor=true`.
- `ollama` not found
  - Add Ollama to PATH or pass `-PaiDoctorOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"` (and for branch doctor via `-PbranchDoctorOllamaCmd=...`).
- Timeout or empty output from Ollama
  - Increase timeout (`-PaiDoctorTimeoutSec` / `-PbranchDoctorTimeoutSec`), try a smaller/faster model, or verify `ollama run <model>` works in your shell.
- Prompt too long
  - Adjust `-PaiDoctorMaxPrompt` or `-PbranchDoctorMaxPrompt`.
- Privacy concerns
  - Redaction is on by default. Disable with `-PaiDoctorRedact=false` / `-PbranchDoctorRedact=false` if needed.
- Branch doctor: XML report not found
  - Ensure the JaCoCo report task ran and the `xmlReport` path is correct (composeApp writes to `.../report.xml`).
- Branch doctor: no source context shown
  - Check that `sourceRoots` includes all source directories where files can be found.

---

## Reuse in other projects
Since these live in buildSrc, they are automatically available to this build. To reuse elsewhere, copy the buildSrc directory into another Gradle project and:
- Apply the plugin (for end‑of‑build analysis) in the root build.gradle.kts:
  ```kotlin
  plugins {
      id("dev.angussoftware.ai-doctor")
  }
  ```
- Register BranchCoverageDoctorTask in the module(s) that produce JaCoCo XML, providing the inputs, flags, and outputs as shown above.

---

## Source files
- buildSrc/src/main/kotlin/dev/angussoftware/aidoctor/AiDoctorPlugin.kt
- buildSrc/src/main/kotlin/dev/angussoftware/aidoctor/BranchCoverageDoctorTask.kt
- This README: buildSrc/README.md
