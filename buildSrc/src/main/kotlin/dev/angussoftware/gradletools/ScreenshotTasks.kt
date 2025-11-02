package dev.angussoftware.gradletools

import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.api.tasks.Exec
import java.io.File

/**
 * Screenshot tasks registrar — registers three convenience Exec tasks around Android instrumented tests
 * and wires them to run before/after `connectedDebugAndroidTest`.
 *
 * Tasks registered (idempotent):
 * - createScreenshotDirectory: adb shell mkdir -p /sdcard/Download/<appId>/<buildType>/screenshots
 * - fetchScreenshots:          adb pull <deviceDir> <moduleDir>/screenshots
 * - clearScreenshots:          adb shell rm -rf <deviceDir>
 *
 * Properties (optional overrides):
 * - -PscreenshotAppId=<applicationId>    Defaults to "dev.angussoftware.app" if not provided.
 * - -PscreenshotBuildType=<buildType>    Defaults to "debug" if not provided.
 */
internal object ScreenshotTasks {
    fun register(project: Project) {
        // Resolve properties with sensible defaults
        val appId = project.providers.gradleProperty("screenshotAppId").orElse("dev.angussoftware.app").get()
        val buildType = project.providers.gradleProperty("screenshotBuildType").orElse("debug").get()

        val deviceDir = "/sdcard/Download/$appId/$buildType/screenshots"
        val localDir: File = project.layout.projectDirectory.dir("screenshots").asFile

        // Task: createScreenshotDirectory
        if (project.tasks.findByName("createScreenshotDirectory") == null) {
            project.tasks.register<Exec>("createScreenshotDirectory") {
                group = "verification"
                description = "Creates screenshot directory on connected Android device"
                commandLine("adb", "shell", "mkdir", "-p", deviceDir)
                isIgnoreExitValue = true // idempotent
                doFirst {
                    println("Creating screenshot directory on device: $deviceDir")
                }
                doLast {
                    println("Screenshot directory created successfully")
                }
            }
        }

        // Task: fetchScreenshots
        if (project.tasks.findByName("fetchScreenshots") == null) {
            project.tasks.register<Exec>("fetchScreenshots") {
                group = "verification"
                description = "Fetches screenshots from device to local project directory"
                commandLine("adb", "pull", deviceDir, localDir.absolutePath)
                isIgnoreExitValue = true // do not fail build if none exist
                doFirst {
                    println("Fetching screenshots from device...")
                    println("Device path: $deviceDir")
                    println("Local path: ${localDir.absolutePath}")
                    if (!localDir.exists()) {
                        localDir.mkdirs()
                        println("Created local screenshots directory")
                    }
                }
                doLast {
                    println("Screenshots fetched successfully to: ${localDir.absolutePath}")
                }
            }
        }

        // Task: clearScreenshots
        if (project.tasks.findByName("clearScreenshots") == null) {
            project.tasks.register<Exec>("clearScreenshots") {
                group = "verification"
                description = "Removes screenshots from device storage"
                commandLine("adb", "shell", "rm", "-rf", deviceDir)
                isIgnoreExitValue = true
                doFirst { println("Clearing screenshots from device: $deviceDir") }
                doLast { println("Device screenshots cleared") }
            }
        }

        // Orchestration: before/after connectedDebugAndroidTest
        project.tasks.matching { it.name == "connectedDebugAndroidTest" }.configureEach {
            dependsOn("createScreenshotDirectory")
            finalizedBy("fetchScreenshots")
        }
        project.tasks.matching { it.name == "fetchScreenshots" }.configureEach {
            finalizedBy("clearScreenshots")
        }
    }
}
