plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("aiDoctor") {
            id = "dev.angussoftware.ai-doctor"
            implementationClass = "dev.angussoftware.aidoctor.AiDoctorPlugin"
            displayName = "AI Doctor (Ollama CLI)"
            description = "Provides an end-of-build AI diagnosis using the local Ollama CLI and a sample failing task."
        }
    }
}
