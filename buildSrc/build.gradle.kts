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
        register("angusGradleTools") {
            id = "dev.angussoftware.gradle-tools"
            implementationClass = "dev.angussoftware.gradletools.AngusGradleToolsPlugin"
            displayName = "Angus Gradle Tools"
            description = "Collection of Angus Gradle tasks and build helpers (includes build failure analysis via local Ollama CLI)."
        }
    }
}
