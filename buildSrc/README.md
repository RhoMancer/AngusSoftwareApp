# Angus Gradle Tools — Failure Analysis, Coverage Gaps, and Screenshot Utilities

`buildSrc` provides three Gradle plugins and one cacheable task that help you diagnose failed builds and improve test coverage. AI features use a local Ollama CLI (no HTTP).

At a glance
- Plugins:
  - `dev.angussoftware.gradle-tools.failure-analysis` → end‑of‑build failure analysis banner (root‑applied, opt‑in per run)
  - `dev.angussoftware.gradle-tools.coverage` → wires JaCoCo XML and registers a branch‑gaps report task
  - `dev.angussoftware.gradle-tools` → bundle (applies the above to selected modules and can register screenshot helpers)
- Tasks (registered/available):
  - `buildFailureDemo` → intentionally fails (root) to demo the failure analysis banner
  - `androidConnectedTestCoverageReport` → generates JaCoCo HTML/XML for instrumented tests
  - `androidInstrumentedCoverage` → convenience task; depends on `androidConnectedTestCoverageReport`
  - `androidBranchCoverageGaps` → runs `BranchCoverageGapsReportTask` against the JaCoCo XML
  - `androidInstrumentedCoverageWithBranchGaps` → runs instrumented coverage + branch gaps in sequence
  - `fullCoverageReport` → unit (Kover) + instrumented coverage
  - `createScreenshotDirectory` → adb: create `/sdcard/Download/<appId>/<buildType>/screenshots`
  - `fetchScreenshots` → adb: pull from device to `<moduleDir>/screenshots`
  - `clearScreenshots` → adb: remove device screenshots directory

Note: Screenshot tasks are auto‑registered by the bundle plugin when `registerScreenshotTasks=true` (default).

---

## Requirements
- Local Ollama CLI on PATH or pass full path via properties.
- A pulled model (e.g., `gemma3`, `llama3.1:8b`, `qwen2.5-coder:7b`).

Quick check:
```bat
ollama pull gemma3

echo "Say hi" | ollama run gemma3
```

---

## Quickstart
1) Apply the bundle plugin at the root:
```kotlin
plugins {
    id("dev.angussoftware.gradle-tools")
}

angusToolsBundle {
    includeProjects = listOf(":composeApp")  // modules to wire
    // registerScreenshotTasks = true        // default: true
}
```

2) Generate coverage + branch gaps for composeApp (no AI):
```bat
gradlew :composeApp:androidInstrumentedCoverageWithBranchGaps -PbranchCoverageEnabled=true
```

3) Include AI suggestions (opt‑in, allowed on CI only when explicitly enabled):
```bat
gradlew :composeApp:androidInstrumentedCoverageWithBranchGaps -PbranchCoverageEnabled=true -PbranchCoverageAiEnabled=true -PbranchCoverageCiEnabled=true
```

4) Try the failure‑analysis demo at the root:
```bat
gradlew :buildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureMessage="Diagnose this sample failure"
```

---

## Failure Analysis Plugin (`dev.angussoftware.gradle-tools.failure-analysis`)
What it does
- Registers a single `buildFinished` listener only when `-PbuildFailureEnabled=true`.
- If the build failed, it builds a redacted, clipped prompt and calls `ollama run <model>` via STDIN. The model’s response prints under a clear banner.

Key flags (with defaults)
- `-PbuildFailureEnabled=true|false` → master switch (default false)
- `-PbuildFailureModel=<name>` → model (default `gemma3`)
- `-PbuildFailureOllamaCmd=<path|name>` → CLI (default auto; `ollama` or `ollama.exe` on Windows)
- `-PbuildFailureTimeoutSec=<5..120>` → timeout seconds (default 60)
- `-PbuildFailureMaxPrompt=<1000..30000>` → prompt clamp (default 6000)
- `-PbuildFailureRedact=true|false` → redact HOME/PROJECT_ROOT (default true)
- `-PbuildFailureCiEnabled=true|false` → allow on CI when `CI=true` (default false)
- `-PbuildFailureEnforceNoConfigCache=true|false` → when CC is requested and this is true (default), the plugin throws with guidance; set false to keep CC (listener will be skipped)

Notes
- Uses `Logger.quiet` for the final banner. Progress logs include a 1‑second ticker while the model runs.
- Demo task `:buildFailureDemo` is auto‑registered at the root.

---

## Branch Coverage Gaps Task (`BranchCoverageGapsReportTask`)
What it does
- Parses JaCoCo XML for lines with missed branches (`mb > 0`).
- Resolves files via `sourceRoots`, builds per‑line context windows (or whole file with `contextLines=-1`).
- Writes:
  - JSON (`branch-gaps.json`) — machine‑readable summary (files, lines, windows, classifications)
  - Markdown (`branch-gaps.md`) — human report with numbered code windows and `▶` marking the missed line
  - AI Markdown (`branch-gaps-ai.md`) — optional AI guidance, including the exact prompt used
  - Meta JSON (`branch-gaps.meta.json`) — flags, prompt details, output paths
- Enforces thresholds and can fail the build (per file or global).

Security and robustness
- XML parser disables external DTD/entity resolution and uses an empty `EntityResolver` (no FS/network access during parse).
- Prompts are clipped to `maxPrompt` (clamped to `1000..30000`) and can be redacted.

AI selection and response shaping
- A line is eligible when `cb >= minCoveredBranchesForAi` (default 1).
- Selection priority when trimming to `maxAiAnalyses` (default 20):
  1) higher coverage ratio `cb / (cb + mb)`; 2) then higher `cb`; 3) then file/line order.
- Response post‑processing injects matching code windows into the model output when headings/line anchors are detected; otherwise a code appendix is appended.

Task inputs
- `xmlReport` (RegularFile), `sourceRoots` (List<String>)
- Flags: `branchCoverageEnabled`, `aiEnabled`, `ciEnabled`
- Tuning: `contextLines` (default 5; `-1` = whole file), optional `topNFiles`
- Thresholds: `failIfMissedBranches`, `failIfMissedBranchesPerFile`
- AI: `model`, `ollamaCmd`, `timeoutSec (5..120)`, `maxPrompt (1000..30000)`, `redact`

Outputs
- `outputJson`, `outputMd`, `outputAiMd`, `outputMeta` — typically placed next to the JaCoCo XML.

---

## Coverage Plugin (`dev.angussoftware.gradle-tools.coverage`)
What it registers
- `androidConnectedTestCoverageReport` (JaCoCo XML/HTML for instrumented tests). Depends on `connectedDebugAndroidTest` and ensures debug class outputs are produced. Prints report paths at the end.
- `androidInstrumentedCoverage` → depends on `androidConnectedTestCoverageReport`.
- `androidBranchCoverageGaps` → runs `BranchCoverageGapsReportTask` against that XML; `onlyIf { branchCoverageEnabled }`.
- `androidInstrumentedCoverageWithBranchGaps` → runs both.
- `fullCoverageReport` → runs unit (Kover) and instrumented coverage.

Defaults and wiring
- `angusCoverage.xmlReport` → `build/reports/jacoco/androidConnectedTest/report.xml`
- `angusCoverage.sourceRoots` → `src/commonMain/kotlin`, `src/androidMain/kotlin` (absolute paths)
- Class directories include typical debug outputs; excludes R/BuildConfig/Manifest/tests, generated artifacts, Compose singletons/previews.
- Forwards all `-PbranchCoverage*` properties with sensible defaults and clamps.

Common invocations
```bat
:: without AI
gradlew :<module>:androidInstrumentedCoverageWithBranchGaps -PbranchCoverageEnabled=true

:: with AI (allowed on CI only if explicitly enabled)
gradlew :<module>:androidInstrumentedCoverageWithBranchGaps -PbranchCoverageEnabled=true -PbranchCoverageAiEnabled=true -PbranchCoverageCiEnabled=true
```

---

## Bundle Plugin (`dev.angussoftware.gradle-tools`)
- Applies the failure‑analysis plugin to the root and the coverage plugin to listed subprojects.
- Extension (defaults):
  - `includeProjects = listOf(":composeApp")`
  - `registerScreenshotTasks = true` (registers Android screenshot helper tasks in included modules)
  - `autoWireCoverageDependsOn = true` (reserved for future expansion)

Example
```kotlin
plugins { id("dev.angussoftware.gradle-tools") }

angusToolsBundle {
    includeProjects = listOf(":composeApp")
    registerScreenshotTasks = true
}
```

---

## Screenshot Tasks (Android convenience)
Registered when the bundle plugin has `registerScreenshotTasks=true`.

Tasks (idempotent)
- `createScreenshotDirectory` → `adb shell mkdir -p /sdcard/Download/<appId>/<buildType>/screenshots`
- `fetchScreenshots` → `adb pull <deviceDir> <moduleDir>/screenshots`
- `clearScreenshots` → `adb shell rm -rf <deviceDir>`

Orchestration
- `connectedDebugAndroidTest` depends on `createScreenshotDirectory`, then is finalized by `fetchScreenshots`, which is finalized by `clearScreenshots`.

Properties
- `-PscreenshotAppId=<applicationId>` (default `dev.angussoftware.app`)
- `-PscreenshotBuildType=<buildType>` (default `debug`)

All three Exec tasks use `ignoreExitValue=true` to avoid failing the build if a device is absent or no screenshots exist.

---

## Operator’s Guide (for teammates)
- Install and test Ollama:
  ```bat
  ollama pull gemma3
  echo "Say hi" | ollama run gemma3
  ```
- Generate coverage + gaps (no AI):
  ```bat
  gradlew :composeApp:androidInstrumentedCoverageWithBranchGaps -PbranchCoverageEnabled=true
  ```
- Add AI suggestions (local or CI with opt‑in):
  ```bat
  gradlew :composeApp:androidInstrumentedCoverageWithBranchGaps -PbranchCoverageEnabled=true -PbranchCoverageAiEnabled=true -PbranchCoverageCiEnabled=true
  ```
- Failure analysis on any failing build:
  ```bat
  gradlew <your:task> --no-configuration-cache -PbuildFailureEnabled=true
  :: or keep CC but skip listener registration
  gradlew <your:task> -PbuildFailureEnabled=true -PbuildFailureEnforceNoConfigCache=false
  ```
- Use a specific model and path on Windows:
  ```bat
  gradlew :buildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureModel=llama3.1:8b -PbuildFailureOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"
  ```
- Screenshot helpers (auto‑wired):
  ```bat
  gradlew :composeApp:connectedDebugAndroidTest -PscreenshotAppId=dev.angussoftware.app -PscreenshotBuildType=debug
  ```

---

## Properties (reference)
Failure analysis (`buildFailure*`)
- `buildFailureEnabled`, `buildFailureModel`, `buildFailureOllamaCmd`, `buildFailureTimeoutSec (5..120)`, `buildFailureMaxPrompt (1000..30000)`, `buildFailureRedact`, `buildFailureCiEnabled`, `buildFailureEnforceNoConfigCache`

Branch gaps (`branchCoverage*`)
- `branchCoverageEnabled`, `branchCoverageAiEnabled`, `branchCoverageCiEnabled`, `branchCoverageContextLines`, `branchCoverageTopNFiles`, `branchCoverageFailIfMissedBranches`, `branchCoverageFailIfMissedBranchesPerFile`, `branchCoverageModel`, `branchCoverageOllamaCmd`, `branchCoverageTimeoutSec (5..120)`, `branchCoverageMaxPrompt (1000..30000)`, `branchCoverageRedact`, `branchCoverageMinCoveredBranchesForAi`, `branchCoverageMaxAiAnalyses`

Screenshots (`screenshot*`)
- `screenshotAppId`, `screenshotBuildType`

---

## Troubleshooting (quick)
- No analysis banner: ensure the build actually failed and `-PbuildFailureEnabled=true`. With CC: use `--no-configuration-cache` or `-PbuildFailureEnforceNoConfigCache=false` (listener will be skipped).
- `ollama` not found: add to PATH or pass `-PbuildFailureOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"` (and `-PbranchCoverageOllamaCmd=...`). Verify `ollama run <model>` works.
- AI timeout/empty output: raise `*TimeoutSec`, try a smaller model, or check local model availability.
- Branch coverage XML not found: run `androidConnectedTestCoverageReport` first or check `angusCoverage.xmlReport`.
- No source context: ensure all source roots are listed.
- Privacy: prompts redact HOME/PROJECT_ROOT by default. Disable with `*Redact=false` if needed.

---

## Reuse in other projects
- Copy `buildSrc` to another Gradle project.
- At root, apply the failure analysis plugin or the bundle:
  ```kotlin
  plugins { id("dev.angussoftware.gradle-tools.failure-analysis") }
  // or
  plugins { id("dev.angussoftware.gradle-tools") }
  ```
- In modules with JaCoCo XML, either apply `dev.angussoftware.gradle-tools.coverage` or keep the bundle and list modules in `includeProjects`.

---

## Source files
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/AngusFailureAnalysisPlugin.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/BranchCoverageGapsReportTask.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/AngusCoveragePlugin.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/AngusToolsBundlePlugin.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/ScreenshotTasks.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/BuildFailureDemoTask.kt`
- `buildSrc/src/main/kotlin/dev/angussoftware/gradletools/GradleToolsCommon.kt`
- This README: `buildSrc/README.md`
