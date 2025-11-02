package dev.angussoftware.gradletools

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Simple demo task that intentionally fails to exercise the build-failure analysis flow.
 *
 * Usage example:
 *   gradlew :buildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureMessage="Explain this demo failure"
 */
abstract class BuildFailureDemoTask : DefaultTask() {
    @get:Input
    abstract val message: Property<String>

    @TaskAction
    fun failNow() {
        throw GradleException("build-failure demo: ${message.get()}")
    }
}
