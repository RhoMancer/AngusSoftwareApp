plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.angusGradleTools)
}

// Project version (propagates to subprojects). Reads from gradle.properties `version=`
version = (findProperty("version") as String?) ?: version

// Configure the bundle plugin to apply coverage to selected subprojects
angusToolsBundle {
    // Default to composeApp; add more modules here as needed
    includeProjects = listOf(":composeApp")
    autoWireCoverageDependsOn = true
}

// Centralized Kover aggregation for the whole build
kover {
    merge {
        subprojects()
    }
}

// --- Version bump utilities (file-driven) ---
// Source of truth: gradle.properties
// - version (SemVer string) → used as Android versionName and Web/Wasm display version
// - android.versionCode (int; must increment by +1 for every Play release)
//
// NOTE: These tasks only update `gradle.properties`. The Web/Wasm `version.txt` file
// (in composeApp/src/wasmJsMain/resources/) is regenerated at BUILD TIME, not here.
// When CI (GitHub Actions) runs the build, `version.txt` will automatically reflect
// the new version from gradle.properties.

fun readRootProps(): java.util.Properties {
    val f = rootProject.file("gradle.properties")
    return java.util.Properties().apply { f.inputStream().use { load(it) } }
}

fun writeRootProps(p: java.util.Properties) {
    val f = rootProject.file("gradle.properties")
    f.writer().use { w -> p.store(w, null) }
}

data class SemVer(val major: Int, val minor: Int, val patch: Int) {
    override fun toString() = "$major.$minor.$patch"
}

fun resolveCurrentVersion(p: java.util.Properties): Pair<String, SemVer> {
    val currentVersion = (
        p.getProperty("version")
            ?: (project.findProperty("version") as String?)
            ?: project.version.toString().takeIf { it.isNotBlank() && it != "unspecified" }
        ) ?: throw GradleException(
        "Missing 'version'. Add 'version=x.y.z' to root gradle.properties."
    )
    val parts = currentVersion.split('.')
    val semver = SemVer(
        parts.getOrNull(0)?.toIntOrNull() ?: 0,
        parts.getOrNull(1)?.toIntOrNull() ?: 0,
        parts.getOrNull(2)?.toIntOrNull() ?: 0,
    )
    return currentVersion to semver
}

fun bumpAndWrite(bump: (SemVer) -> SemVer) {
    val p = readRootProps()
    val (current, semver) = resolveCurrentVersion(p)
    val newVer = bump(semver)
    p.setProperty("version", newVer.toString())
    writeRootProps(p)
    println("[version] $current -> $newVer")
}

tasks.register("bumpPatch") {
    doLast { bumpAndWrite { (maj, min, pat) -> SemVer(maj, min, pat + 1) } }
}

tasks.register("bumpMinor") {
    doLast { bumpAndWrite { (maj, min, _) -> SemVer(maj, min + 1, 0) } }
}

tasks.register("bumpMajor") {
    doLast { bumpAndWrite { (maj, _, _) -> SemVer(maj + 1, 0, 0) } }
}

tasks.register("bumpVersionCode") {
    doLast {
        val p = readRootProps()
        val currentCodeStr = p.getProperty("android.versionCode")
            ?: (project.findProperty("android.versionCode") as String?)
        val newCode = (currentCodeStr?.toIntOrNull()?.plus(1)) ?: 1
        p.setProperty("android.versionCode", newCode.toString())
        writeRootProps(p)
        if (currentCodeStr == null) {
            println("[versionCode] <missing> -> $newCode (initialized)")
        } else {
            println("[versionCode] ${currentCodeStr.toInt()} -> $newCode")
        }
    }
}

// Composite release tasks that bump human version and versionCode together
tasks.register("releasePatch") {
    dependsOn("bumpPatch", "bumpVersionCode")
}

tasks.register("releaseMinor") {
    dependsOn("bumpMinor", "bumpVersionCode")
}

tasks.register("releaseMajor") {
    dependsOn("bumpMajor", "bumpVersionCode")
}

// Helper: print current versions from file (or Gradle props fallback)
tasks.register("printVersions") {
    group = "versioning"
    description = "Prints current version and android.versionCode"
    doLast {
        val p = readRootProps()
        val ver = p.getProperty("version") ?: (project.findProperty("version") as String?) ?: "<missing>"
        val code = p.getProperty("android.versionCode") ?: (project.findProperty("android.versionCode") as String?) ?: "<missing>"
        println("version=$ver")
        println("android.versionCode=$code")
    }
}
