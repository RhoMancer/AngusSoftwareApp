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

tasks.register("bumpPatch") {
    doLast {
        val p = readRootProps()
        val parts = p.getProperty("version").split('.')
        val maj = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val pat = parts.getOrNull(2)?.toIntOrNull() ?: 0
        val newVer = "$maj.$min.${pat + 1}"
        p.setProperty("version", newVer)
        writeRootProps(p)
        println("[version] $newVer")
    }
}

tasks.register("bumpMinor") {
    doLast {
        val p = readRootProps()
        val parts = p.getProperty("version").split('.')
        val maj = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val newVer = "$maj.${min + 1}.0"
        p.setProperty("version", newVer)
        writeRootProps(p)
        println("[version] $newVer")
    }
}

tasks.register("bumpMajor") {
    doLast {
        val p = readRootProps()
        val parts = p.getProperty("version").split('.')
        val maj = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val newVer = "${maj + 1}.0.0"
        p.setProperty("version", newVer)
        writeRootProps(p)
        println("[version] $newVer")
    }
}

tasks.register("bumpVersionCode") {
    doLast {
        val p = readRootProps()
        val code = p.getProperty("android.versionCode").toInt()
        val newCode = code + 1
        p.setProperty("android.versionCode", newCode.toString())
        writeRootProps(p)
        println("[versionCode] $newCode")
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
