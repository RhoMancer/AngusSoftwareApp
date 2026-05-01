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
// - version (SemVer string)
// - android.versionCode (int; must increment by +1 for every Play release)

fun readRootProps(): java.util.Properties {
    val f = rootProject.file("gradle.properties")
    return java.util.Properties().apply { f.inputStream().use { load(it) } }
}

fun writeRootProps(p: java.util.Properties) {
    val f = rootProject.file("gradle.properties")
    f.writer().use { w -> p.store(w, null) }
}

data class SemVer(val current: String, val maj: Int, val min: Int, val pat: Int)

fun readVersion(): SemVer {
    val p = readRootProps()
    val currentVersion = (
        p.getProperty("version")
            ?: (project.findProperty("version") as String?)
            ?: project.version.toString().takeIf { it.isNotBlank() && it != "unspecified" }
        ) ?: throw GradleException(
        "Missing 'version'. Add 'version=x.y.z' to root gradle.properties."
    )
    val parts = currentVersion.split('.')
    return SemVer(
        currentVersion,
        parts.getOrNull(0)?.toIntOrNull() ?: 0,
        parts.getOrNull(1)?.toIntOrNull() ?: 0,
        parts.getOrNull(2)?.toIntOrNull() ?: 0,
    )
}

fun bumpAndWrite(newVer: String, currentVersion: String) {
    val p = readRootProps()
    p.setProperty("version", newVer)
    writeRootProps(p)
    println("[version] $currentVersion -> $newVer")
}

tasks.register("bumpPatch") {
    doLast {
        val v = readVersion()
        bumpAndWrite("${v.maj}.${v.min}.${v.pat + 1}", v.current)
    }
}

tasks.register("bumpMinor") {
    doLast {
        val v = readVersion()
        bumpAndWrite("${v.maj}.${v.min + 1}.0", v.current)
    }
}

tasks.register("bumpMajor") {
    doLast {
        val v = readVersion()
        bumpAndWrite("${v.maj + 1}.0.0", v.current)
    }
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
