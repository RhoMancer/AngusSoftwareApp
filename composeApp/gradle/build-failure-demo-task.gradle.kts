// --- composeApp build-failure analysis demo task ----------------------------------------------
// This task intentionally fails to let you verify build-failure analysis is wired up.
// Usage examples:
//   gradlew :composeApp:composeAppBuildFailureDemo --no-configuration-cache -PbuildFailureEnabled=true -PbuildFailureMessage="Explain why this sample failed"
// Notes:
// - It is configuration-cache friendly for configuration but throws at execution.
// - End-of-build analysis requires --no-configuration-cache (Gradle skips end-of-build listeners otherwise).
//   See the policy comments in composeApp/build.gradle.kts for how to temporarily allow configuration cache.

abstract class ComposeAppBuildFailureDemoTask : org.gradle.api.DefaultTask() {
    @get:org.gradle.api.tasks.Input
    abstract val message: org.gradle.api.provider.Property<String>

    @org.gradle.api.tasks.TaskAction
    fun failNow(): Unit = throw org.gradle.api.GradleException("composeApp build-failure demo: ${message.get()}")
}

tasks.register("composeAppBuildFailureDemo", ComposeAppBuildFailureDemoTask::class.java) {
    group = "Help"
    description = "Intentionally fails (composeApp) to demo build-failure analysis; customize with -PbuildFailureMessage."
    message.convention(
        project.providers
            .gradleProperty("buildFailureMessage")
            .orElse("Sample failure triggered by composeAppBuildFailureDemo"),
    )
}
// ------------------------------------------------------------------------------------------------
