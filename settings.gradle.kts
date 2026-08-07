rootProject.name = "AngusSoftwareApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

fun MavenArtifactRepository.androidxAndGoogleOnly() {
    mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
    }
}

pluginManagement {
    repositories {
        google()

        val forgejoToken: String? = providers.gradleProperty("forgejo.token").orNull ?: System.getenv("FORGEJO_TOKEN")

        // Resolve Angus Gradle Tools plugin markers from Forgejo Maven Registry
        maven {
            url = uri("https://git.angussoftware.dev/api/packages/rhomancer/maven")
            credentials {
                username = "rhomancer"
                password = forgejoToken
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

val forgejoToken: String? = providers.gradleProperty("forgejo.token").orNull ?: System.getenv("FORGEJO_TOKEN")
val gprUsername: String? = providers.gradleProperty("githubUser").orNull ?: System.getenv("GPR_USERNAME")
val gprToken: String? = providers.gradleProperty("githubToken").orNull ?: System.getenv("GPR_TOKEN")

dependencyResolutionManagement {
    repositories {
        google {
            androidxAndGoogleOnly()
        }
        mavenCentral()

        // Forgejo Maven Registry: serves angus-gradle-tools
        maven {
            url = uri("https://git.angussoftware.dev/api/packages/rhomancer/maven")
            credentials {
                username = "rhomancer"
                password = forgejoToken
            }
        }

        // GPR: Angus-Software-Theming published here (full KMP artifacts)
        // TODO: migrate to Forgejo Maven once publish pipeline produces full KMP artifacts
        maven {
            url = uri("https://maven.pkg.github.com/RhoMancer/Angus-Software-Theming")
            credentials {
                username = gprUsername
                password = gprToken
            }
        }
    }
}

include(":composeApp")
