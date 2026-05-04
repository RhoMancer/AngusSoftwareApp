# Angus Software App — Kotlin Multiplatform (Android + Web/Wasm)

Angus Software application for showcasing my portfolio and hosting my blog.

- Current targets: Android, Web/Wasm
- Future: iOS planned; Desktop not planned
- Live demo: <https://rhomancer.github.io/AngusSoftwareApp/>
- Google Play listing: Available on Internal Testing track

## Quick start

Prerequisites
- Java 17 (JDK 17+)
- Android Studio (latest stable) with Android SDK
- Gradle Wrapper (included): use `gradlew` on Windows, `./gradlew` on macOS/Linux

Clone and build
```bash
# from repository root
gradlew help
```

Run
- Android (Debug):
  - With Android Studio: open the project, select an emulator or device, and Run.
  - Via CLI: `gradlew :composeApp:installDebug` (then run the app on your device/emulator)
- Web/Wasm (Dev server):
  - `gradlew :composeApp:wasmJsBrowserDevelopmentRun` and open the URL printed in the console

Configuration
- Blog RSS feed URL lives in `composeApp/src/commonMain/kotlin/dev/angussoftware/app/navigation/NavHost.kt` as `RSS_FEED_URL`.
  Change it if you want to point to a different feed.

## Deployment

This project includes automatic deployment for both Android and Web/Wasm platforms.

**Automatic deployment triggers:**
- Push to `release/**` branches automatically deploys to:
  - Google Play Store (Internal Testing track)
  - GitHub Pages (<https://rhomancer.github.io/AngusSoftwareApp/>)

**Setup instructions:**
See [DEPLOYMENT.md](DEPLOYMENT.md) for complete step-by-step guide including:
- Creating Android keystore for release signing
- Setting up Google Play Console and service account
- Configuring GitHub secrets for CI/CD
- Enabling GitHub Pages
- Running your first deployment

**Quick deployment:**
```bash
# Bump version for a patch release and versionCode (+1) in gradle.properties
gradlew releasePatch

# Commit the bump and push a release branch (if your workflow triggers on release/*)
git add gradle.properties
git commit -m "chore: release $(grep ^version= gradle.properties | cut -d= -f2)"
git checkout -b release/v$(grep ^version= gradle.properties | cut -d= -f2)
git push origin HEAD

# Monitor deployment at: https://github.com/RhoMancer/AngusSoftwareApp/actions
```

## Automated versioning

This project uses file-based versioning. The single source of truth lives in `gradle.properties`:
- `version` — human-readable SemVer (e.g., `1.2.1`), used as Android `versionName` and shown on the Web/Wasm build.
- `android.versionCode` — integer for Google Play; must increase by exactly +1 on every Play release.

Gradle tasks:
- `bumpPatch` — increment `version` patch (x.y.z → x.y.(z+1)).
- `bumpMinor` — increment minor (x.y.z → x.(y+1).0).
- `bumpMajor` — increment major ((x+1).0.0).
- `bumpVersionCode` — increment Play `versionCode` by +1.
- Composite convenience tasks:
  - `releasePatch` = `bumpPatch` + `bumpVersionCode`
  - `releaseMinor` = `bumpMinor` + `bumpVersionCode`
  - `releaseMajor` = `bumpMajor` + `bumpVersionCode`

Android reads `versionName` and `versionCode` from these properties at build time, so you no longer need to edit `composeApp/build.gradle.kts` for version bumps.

Web/Wasm displays the same `version` in a small badge (sourced from a generated `version.txt`).

## Tech stack
- Kotlin Multiplatform (KMP)
- Compose Multiplatform UI (Material3, components/resources, icons)
- AndroidX Navigation Compose
- Kotlinx Serialization (as configured)
- Web/Wasm target via Kotlin/Wasm
- Optional theming dependency: "Angus Software Theming" (private)

## Features
- Blog powered by an external RSS feed (parsing and downloading posts)
- Navigation across Home, Projects, Blog, and individual Blog Post pages
- Responsive UI primitives (NavigationBar/Rail / adaptive info helpers)
- Shared UI components and screen state helpers

## Architecture overview
- Single-activity app (Android) with Compose Navigation (`NavHost`) controlling screens
- Shared screens in `commonMain`:
  - `HomeScreen`, `ProjectsScreen`, `BlogScreen`, `BlogPostScreen`
- Blog data
  - `BlogRepository` pulls and parses posts from `RSS_FEED_URL`
  - `RssParser` parses entries; `NetworkClient` abstracts fetching per platform
- Navigation routes
  - `Screen` enum defines destinations
  - Blog posts addressed as `BlogPost/{postIndex}`
  - Helpers: `parsePostIndex(route)`, `createLoadingBlogPost(title)`, `createErrorBlogPost(title)`
- Testing hooks
  - `NAV_HOST_TEST_TAG` test tag on the NavHost root
  - Unit tests for navigation/blog parsing in `commonTest`
  - Android instrumented UI test in `androidInstrumentedTest`

## Internal conventions and patterns
- State handling
  - Loading/error BlogPost placeholders are constructed via `createLoadingBlogPost` and `createErrorBlogPost`
  - Simple `remember` state + `LaunchedEffect` for async loading in `BlogPost` route
- Navigation
  - Destinations in a typed `Screen` enum; route parameters passed as strings
  - `parsePostIndex` is lenient and returns 0 on invalid input
- Responsiveness
  - `WindowAdaptiveInfo` helpers to decide between NavigationBar/Rail
- Testability
  - Important nodes tagged (e.g., `NAV_HOST_TEST_TAG`) to simplify UI testing

## Project structure (high level)
```
composeApp/
  src/
    androidMain/                 # Android-specific code (Activity, platform utils)
    androidInstrumentedTest/     # Android UI tests (on device/emulator)
    commonMain/                  # Shared code (UI, navigation, blog domain)
    commonTest/                  # Shared tests (run on JVM via Android unit test task)
    wasmJsMain/                  # Web/Wasm entrypoint and resources
```
Key files
- `navigation/NavHost.kt` — routes, start destination, BlogPost loader, `RSS_FEED_URL`
- `blog/BlogRepository.kt` — fetches/limits posts
- `blog/RssParser.kt` — RSS parsing
- Screens under `screens/`
- UI utilities and common components under `ui/`

## Testing
Reference: Compose Multiplatform testing guide
<https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html>

Run all unit tests (JVM, includes commonTest via Android unit test task)
```bash
gradlew :composeApp:testDebugUnitTest
```

Run a single unit test class or method
```bash
# whole class
gradlew :composeApp:testDebugUnitTest --tests "dev.angussoftware.app.navigation.NavHostTest"
# single method (example)
gradlew :composeApp:testDebugUnitTest --tests "dev.angussoftware.app.blog.RssParserTest.parse_valid_feed"
```

Android instrumented tests (device/emulator must be running)
```bash
gradlew :composeApp:connectedDebugAndroidTest
```

Run a single instrumented test class
```bash
gradlew :composeApp:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=dev.angussoftware.app.navigation.NavHostComposableTest
```

Where to find reports
- Unit tests: `composeApp/build/reports/tests/testDebugUnitTest/index.html`
- Instrumented tests: `composeApp/build/reports/androidTests/connected/index.html`

## Roadmap
- iOS target: planned
- Desktop: not planned

## Theming dependency (optional/private)
This project currently depends on a private "Angus Software Theming" library. If you don’t have access, you can remove it:
- In `composeApp/build.gradle.kts`, remove occurrences of `libs.angusSoftware.theming.compose` from `androidMain`, `commonMain`, and `commonTest` dependencies.
- Remove/replace any imports, types, or theme usages coming from that library.
The app should work with standard Compose Material3 theming if you wire colors/typography/shapes directly.

## Troubleshooting
- Use JDK 17+ (both Gradle and Android compile options target JVM 17)
- If the Wasm dev server doesn’t open automatically, copy the printed URL from the Gradle task output
- If Android instrumented tests fail to discover devices, ensure an emulator is running or a device is connected (USB debugging enabled)

## Contributing
See CONTRIBUTING.md

## License
No open-source license has been selected yet. Until a license is added, all rights are reserved by the author. If you intend to use code from this repository, please open an issue to discuss licensing first.

## Code Coverage

This project is configured to generate code coverage reports for both:
- Unit tests (via Kover)
- Android instrumented tests (via the Android Gradle Plugin + JaCoCo)

Prerequisites
- An emulator or device must be running for instrumented tests.

Generate unit test coverage (HTML)
```bash
# Module-level Kover report
gradlew :composeApp:koverHtmlReport
```
Report location:
- composeApp/build/reports/kover/html/index.html

Generate Android instrumented test coverage (HTML)
```bash
# Runs connected instrumented tests and produces a JaCoCo coverage report
gradlew :composeApp:androidInstrumentedCoverage
```
Report location:
- composeApp/build/reports/jacoco/androidConnectedTest/html/index.html

Generate both coverage reports together
```bash
gradlew :composeApp:fullCoverageReport
```
This will:
- Run unit-test coverage (Kover) → composeApp/build/reports/kover/html/index.html
- Run Android instrumented coverage (JaCoCo) → composeApp/build/reports/jacoco/androidConnectedTest/html/index.html

Notes
- Coverage for instrumented tests is enabled for the debug build type (test coverage is turned on in Gradle).
- If the instrumented coverage task isn’t found, ensure you are on a recent Android Gradle Plugin and try running with `--info` to inspect available tasks.
