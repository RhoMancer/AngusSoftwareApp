# Test Strategy — Anvil Testing System

This document describes the testing strategy for the AngusSoftwareApp Kotlin Multiplatform
Compose project, and how it is validated by the Anvil Testing System (mutation testing +
tiered quality gates) in CI.

## KMP Architecture

The `composeApp` module targets multiple platforms via Kotlin Multiplatform. Sources are
organized by source set:

- `src/commonMain/kotlin` — Platform-agnostic business logic, UI (Compose Multiplatform),
  domain models, and shared abstractions. This is where the majority of testable logic
  lives.
- `src/androidMain/kotlin` — Android-specific implementations (Activity, platform
  preferences, network glue, `expect`/`actual` declarations that require the Android SDK).
- `src/jvmMain/kotlin` — JVM-specific `actual` implementations used for headless unit
  testing of `commonMain` code. Provides plain JVM equivalents for platform APIs so that
  the shared logic can be exercised without an Android emulator.
- `src/wasmJsMain/kotlin` — Web/Wasm target (browser). Not part of the JVM test tier.

Test source sets mirror this layout:

- `src/commonTest/kotlin` — Common tests, executed on every runnable target.
- `src/jvmTest/kotlin` — JVM-only unit tests (Robolectric-backed where Android APIs are
  needed). This is the primary tier consumed by mutation testing and `check-tier.sh`.
- `src/androidInstrumentedTest/kotlin` — Instrumented (on-device) tests running on the
  Android emulator; used for integration/UI validation that cannot run on a plain JVM.

## JVM Unit Tests with Robolectric

Unit tests in `jvmTest` are the source of truth for the mutation-testing tier. Robolectric
is used to simulate the Android framework where thin shims are unavoidable, so that
`commonMain` code paths that touch Android abstractions can still be exercised entirely on
the JVM (no emulator, no device). This keeps the loop fast and mutation-testing feasible.

Run locally:

```
./gradlew :composeApp:jvmTest
```

Mutation testing (opt-in):

```
./gradlew :composeApp:pitest -Ppit.enabled=true
```

## Instrumented Tests

Instrumented tests (`connectedDebugAndroidTest`) run on an Android emulator in a separate
Woodpecker pipeline. They cover UI flows and integrations that require a real Android
runtime. They contribute to unified coverage (JaCoCo) but are **not** input to the mutation
tier — mutation testing operates strictly on the JVM classes and JVM tests.

## What is Excluded from Coverage and Why

The following are excluded (see `pit-config.json` and the `unifiedReport` exclusions in
`composeApp/build.gradle.kts`), because they are either generated, synthetic, or platform
glue that cannot be meaningfully unit-tested on the JVM tier:

- **Generated resources**: `**/generated/**`, `BuildConfig`, `R.class`, `Manifest*`,
  Compose Multiplatform resource accessors. Purely generated — no logic to mutate.
- **Compose compiler artifacts**: `ComposableSingletons*`, `$lambda`, `$WhenMappings`,
  and other compiler-synthesized classes. These are re-emitted by the Compose compiler
  plugin and mutations against them are noise, not signal.
- **Android framework classes**: `android/**`, `androidx/**`, `MainActivity*`,
  `*_androidKt` platform actuals (Network, LocaleHelper, ThemePreferences, Platform).
  These require a device/emulator and are validated by instrumented tests instead.
- **Third-party packages**: Koin, Arkivanov Decompose, Turbine, Kotlin(x) stdlib — not
  our code.

## Reference: `check-tier.sh`

The mutation-tier gate is enforced by `check-tier.sh`, sourced from the shared
`RhoMancer/ci-scripts` repository. It is invoked in `.woodpecker/build.yml` after the
JVM unit tests complete. The script consumes:

- Compiled classes: `composeApp/build/classes/kotlin/jvm/main`
- Compiled test classes: `composeApp/build/classes/kotlin/jvm/test`
- JUnit XML: `composeApp/build/test-results/jvmTest`
- Mutation XML: `composeApp/build/reports/pitest/mutations.xml`
- Exclusions: `pit-config.json`
- Sources: `composeApp/src/jvmMain/kotlin`, `composeApp/src/jvmTest/kotlin`

It grades the module against tiered thresholds (Bronze → Silver → Gold → Platinum). The
current floor is **Bronze**; the pipeline fails if the module regresses below it.
