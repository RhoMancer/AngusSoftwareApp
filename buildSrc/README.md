# AI Doctor (Ollama CLI) — Gradle buildSrc plugin

A small reusable Gradle plugin that prints an AI diagnosis to the Gradle console when a build fails.
It uses the local Ollama CLI (no HTTP) and runs once at the end of the build. Includes a demo task to
trigger a failure on demand.

Plugin id: `dev.angussoftware.ai-doctor`

## What it does
- Registers a single end-of-build hook and, if the build failed and the feature is enabled, asks a local
  Ollama model to analyze the exception and propose next steps.
- Prints the model’s response under an "AI diagnosis" banner in the Gradle output.
- Adds a sample task `aiDoctorFail` that intentionally fails so you can demo the flow.

## Requirements
- Ollama installed locally and available on PATH (or pass the full path via `-PaiDoctorOllamaCmd`).
- A model pulled (examples: `gemma3`, `llama3.1:8b`, `qwen2.5-coder:7b`).

Quick check:
```bash
ollama pull gemma3
# Or another model you prefer

# Sanity check the CLI
echo "Say hi" | ollama run gemma3
```

## How it works (Option A: single end-of-build analysis)
- Opt-in feature. Enable per run with `-PaiDoctor=true`.
- The listener is NOT registered when the configuration cache is requested. Run with
  `--no-configuration-cache` when you want the diagnosis.
- On failure, the plugin sends a prompt to Ollama via STDIN (`ollama run <model>`), collects stdout, and
  prints it.
- The prompt includes Gradle/OS/Java metadata, the exception summary, and a clipped stack trace.
- By default, your HOME and the project root paths are redacted in the prompt.

## Quickstart (Windows examples)
List the demo task to confirm it’s available:
```bat
gradlew tasks --all | findstr /I aiDoctorFail
```

Run the demo (intentionally fails, shows the diagnosis):
```bat
gradlew aiDoctorFail --no-configuration-cache -PaiDoctor=true -PaiMessage="Explain why this sample failed and what to try next"
```

Pick a specific model:
```bat
gradlew aiDoctorFail --no-configuration-cache -PaiDoctor=true -PaiDoctorModel=llama3.1:8b -PaiMessage="Diagnose the failure"
```

Point to a specific Ollama executable:
```bat
gradlew aiDoctorFail --no-configuration-cache -PaiDoctor=true -PaiDoctorOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"
```

## Use it on real failures
Run any task with the feature enabled and config cache disabled:
```bat
gradlew test --no-configuration-cache -PaiDoctor=true
```
The diagnosis banner appears only if the build fails.

## Configuration flags
- `-PaiDoctor=true`                 Enable the feature for this build (required to activate).
- `-PaiDoctorModel=<name>`          Ollama model name. Default: `gemma3`.
- `-PaiDoctorOllamaCmd=<path>`      Path or name of the Ollama CLI (`ollama`/`ollama.exe`). Default: auto-detect.
- `-PaiDoctorTimeoutSec=<n>`        CLI timeout (seconds). Default: 60 (min 5, max 120).
- `-PaiDoctorMaxPrompt=<n>`         Max prompt size (characters). Default: 6000 (1000–30000).
- `-PaiDoctorRedact=<true|false>`   Redact HOME and PROJECT_ROOT paths (default: true).
- `-PaiDoctorCiEnabled=<true|false>`Allow running in CI when `CI=true` (default: false).

Demo task message props:
- `-PaiMessage="..."` or `-PaiDoctorMessage="..."` to customize the demo failure message.

## Configuration cache and CI
- Configuration cache: the end-of-build listener is skipped when the cache is requested. Use
  `--no-configuration-cache` to activate the diagnosis.
- Demo task: the sample `aiDoctorFail` reads project properties in its `@TaskAction`, so it also requires
  `--no-configuration-cache`.
- CI: when `CI=true`, the plugin is disabled by default; enable with `-PaiDoctorCiEnabled=true` if needed.

## Troubleshooting
- No banner printed: the build likely did not fail, or the configuration cache was enabled (listener skipped).
- `ollama` not found: add Ollama to PATH or pass `-PaiDoctorOllamaCmd="C:\\Program Files\\Ollama\\ollama.exe"`.
- Timeout or empty output: increase `-PaiDoctorTimeoutSec`, try a smaller/faster model, or verify `ollama run` works.
- Prompt too long: adjust `-PaiDoctorMaxPrompt`.
- Privacy: set `-PaiDoctorRedact=false` to disable path redaction (default is true).

## Reuse in other projects
This plugin lives under `buildSrc`, so it’s automatically on the classpath of this build.
To reuse it elsewhere, copy the `buildSrc` directory into another Gradle project and apply the plugin id in the root `build.gradle.kts`:
```kotlin
plugins {
    id("dev.angussoftware.ai-doctor")
}
```

## Notes
- The plugin uses the Ollama CLI with STDIN to pass the prompt for portability across CLI versions.
- Only one analysis is produced per failing build.
