# Contributing to Angus Software App

Thanks for your interest in contributing! This repository is developer‑focused and aims to be straightforward to build, run, test, and extend.

Note on licensing: no open‑source license has been selected yet. By submitting a contribution (issue, patch, or PR), you agree that your contribution may be incorporated into the project and will be distributed under the project’s future license once selected.

## Ways to contribute
- Report bugs or request features via GitHub Issues
- Improve documentation (README, tests, comments)
- Fix bugs or implement small features
- Add tests and improve test coverage

## Development setup
Prerequisites
- Java 11 (JDK 11)
- Android Studio (latest stable) with Android SDK
- Gradle Wrapper (included): use `gradlew` on Windows, `./gradlew` on macOS/Linux

Clone and sanity check
```bash
# from repository root
gradlew help
```

### Running the app
- Android (Debug)
  - Android Studio: select a device/emulator and Run
  - CLI: `gradlew :composeApp:installDebug` (then run on device/emulator)
- Web/Wasm (Dev server)
  - `gradlew :composeApp:wasmJsBrowserDevelopmentRun`

### Tests
Please run tests locally before opening a PR.

Reference: Compose Multiplatform testing guide
<https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html>

Commands
```bash
# Unit tests (JVM: executes commonTest via Android unit test task)
gradlew :composeApp:testDebugUnitTest

# Single unit test class or method
gradlew :composeApp:testDebugUnitTest --tests "dev.angussoftware.app.navigation.NavHostTest"

# Android instrumented tests (emulator/device must be running)
gradlew :composeApp:connectedDebugAndroidTest

# Single instrumented test class
gradlew :composeApp:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=dev.angussoftware.app.navigation.NavHostComposableTest
```

Reports
- Unit tests: `composeApp/build/reports/tests/testDebugUnitTest/index.html`
- Instrumented tests: `composeApp/build/reports/androidTests/connected/index.html`

## Coding style & conventions
- No formal style tool is enforced yet (ktlint/Detekt not configured)
- Prefer idiomatic Kotlin and Compose best practices
- Internal patterns to follow:
  - Use `Screen` for navigation destinations; route params are strings
  - Use helper creators for loading/error posts (`createLoadingBlogPost`, `createErrorBlogPost`)
  - Tag important nodes for testing (e.g., `NAV_HOST_TEST_TAG`)
  - Keep `RSS_FEED_URL` centralized in `NavHost.kt`

## Theming dependency (private/optional)
This project references a private theming library. If you fork and don’t have access:
- Remove `libs.angusSoftware.theming.compose` from `composeApp/build.gradle.kts` (in `androidMain`, `commonMain`, `commonTest`)
- Replace any imports/usages with standard Compose Material3 theming

## Branching and PRs
- Create feature branches from `main`: `feature/<short-description>` or `fix/<short-description>`
- Keep PRs small and focused
- Describe what/why in the PR description and link related issues

## Commit messages
- Use clear, descriptive messages (imperative mood)
- Mention scope when helpful, e.g., `nav: fix parsing invalid postIndex`

## Security & private data
- Don’t commit secrets or private tokens
- The app uses a public RSS feed by default; if you change it, ensure it’s safe to publish

## Code of Conduct
Be respectful and constructive. If problems arise, open an issue.
