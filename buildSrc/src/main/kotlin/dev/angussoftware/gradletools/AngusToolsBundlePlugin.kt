package dev.angussoftware.gradletools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Angus Gradle Tools — Bundle plugin
 *
 * Purpose
 * - Provide a one-line setup that applies:
 *   - Root-scoped failure analysis plugin: dev.angussoftware.gradle-tools.failure-analysis (to the root project)
 *   - Module-scoped coverage plugin: dev.angussoftware.gradle-tools.coverage (to selected subprojects)
 *
 * Minimal extension
 * angusToolsBundle {
 *   includeProjects = listOf(":composeApp")
 *   autoWireCoverageDependsOn = true
 * }
 *
 * Notes
 * - This is optional sugar. You can still apply the two plugins separately.
 * - The coverage plugin already wires a dependency on a task named
 *   "androidConnectedTestCoverageReport" if present; the autoWire flag exists for future expansion.
 */
class AngusToolsBundlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create(
            "angusToolsBundle",
            AngusToolsBundleExtension::class.java,
            project.objects,
        )
        // Defaults
        ext._includeProjects.convention(listOf(":composeApp"))
        ext._autoWireCoverageDependsOn.convention(true)

        // Always apply the failure-analysis plugin to the root project
        val root = project.rootProject
        root.pluginManager.apply("dev.angussoftware.gradle-tools.failure-analysis")

        // Apply coverage plugin to selected subprojects once all projects are known
        project.gradle.projectsEvaluated {
            val includes = ext._includeProjects.getOrElse(emptyList())
            if (includes.isEmpty()) return@projectsEvaluated

            includes.forEach { path ->
                val target: Project? = root.allprojects.firstOrNull { it.path == path }
                if (target == null) {
                    project.logger.lifecycle("[AngusTools] Bundle: includeProjects contains '$path' which was not found.")
                } else {
                    target.pluginManager.apply("dev.angussoftware.gradle-tools.coverage")
                }
            }
        }
    }
}

abstract class AngusToolsBundleExtension @Inject constructor(objects: ObjectFactory) {
    // Backing properties so we can offer simple var-like DSL
    internal val _includeProjects: ListProperty<String> = objects.listProperty(String::class.java)
    internal val _autoWireCoverageDependsOn: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Subproject paths to apply the coverage plugin to. Example: listOf(":composeApp", ":feature:foo")
     */
    var includeProjects: List<String>
        get() = _includeProjects.orNull ?: emptyList()
        set(value) { _includeProjects.set(value) }

    /**
     * If true, lets the coverage plugin auto-wire a dependency on a known JaCoCo task when present.
     */
    var autoWireCoverageDependsOn: Boolean
        get() = _autoWireCoverageDependsOn.orNull ?: true
        set(value) { _autoWireCoverageDependsOn.set(value) }
}
