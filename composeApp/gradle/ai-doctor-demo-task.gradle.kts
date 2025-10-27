// --- composeApp AI Doctor demo task -----------------------------------------------------------
// This task intentionally fails to let you verify AI Doctor is wired up in this module.
// Usage examples:
//   gradlew :composeApp:composeAppAiDoctorFail --no-configuration-cache -PaiDoctor=true -PaiMessage="Explain why this sample failed"
// Notes:
// - It is configuration-cache friendly: the message is configured via @Input Property.
// - AI Doctor requires --no-configuration-cache to run (Gradle skips end-of-build listeners otherwise).
//   See the policy comments in composeApp/build.gradle.kts for how to temporarily allow configuration cache.

abstract class ComposeAppAiDoctorFailTask : org.gradle.api.DefaultTask() {
    @get:org.gradle.api.tasks.Input
    abstract val message: org.gradle.api.provider.Property<String>

    @org.gradle.api.tasks.TaskAction
    fun failNow(): Unit = throw org.gradle.api.GradleException("composeApp AI Doctor sample failure: ${message.get()}")
}

tasks.register("composeAppAiDoctorFail", ComposeAppAiDoctorFailTask::class.java) {
    group = "Help"
    description = "Intentionally fails (composeApp) to demo AI Doctor; customize with -PaiMessage or -PaiDoctorMessage."
    message.convention(
        project.providers
            .gradleProperty("aiMessage")
            .orElse(project.providers.gradleProperty("aiDoctorMessage"))
            .orElse("Sample failure triggered by composeAppAiDoctorFail"),
    )
}
// ------------------------------------------------------------------------------------------------
