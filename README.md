# Angus Software App — Kotlin Multiplatform (Android + Web/Wasm)

Angus Software application for showcasing my portfolio and hosting my blog.

- Current targets: Android, Web/Wasm
- Future: iOS planned; Desktop not planned
- Live demo: coming soon
- Google Play listing: coming soon

## Quick start

Prerequisites
- Java 11 (JDK 11)
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
https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html

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
- Use JDK 11 (both Gradle and Android compile options are set to 11)
- If the Wasm dev server doesn’t open automatically, copy the printed URL from the Gradle task output
- If Android instrumented tests fail to discover devices, ensure an emulator is running or a device is connected (USB debugging enabled)

## Contributing
See CONTRIBUTING.md

## License
No open-source license has been selected yet. Until a license is added, all rights are reserved by the author. If you intend to use code from this repository, please open an issue to discuss licensing first.