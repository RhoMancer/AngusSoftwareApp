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
        // Note: Plugin repositories do not support repository content filtering.
        // Keep plain google() here; the helper applies only to dependency repositories.
        google()

        // Declare credentials/constants locally: resolve from gradle.properties first, then fall back to env.
        val githubOwner: String = providers.gradleProperty("githubOwner").orNull ?: System.getenv("GITHUB_OWNER") ?: "RhoMancer"
        val githubUsername: String? = providers.gradleProperty("githubUser").orNull ?: System.getenv("GITHUB_USER")
        val githubPassword: String? = providers.gradleProperty("githubToken").orNull ?: System.getenv("GITHUB_TOKEN")

        // Resolve Angus Gradle Tools plugin markers from GitHub Packages instead of mavenLocal
        maven {
            url = uri("https://maven.pkg.github.com/$githubOwner/angus-gradle-tools")
            credentials {
                username = githubUsername
                password = githubPassword
            }
        }
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Resolve dependency artifacts from GitHub Packages; read owner/creds from gradle.properties with env fallback
val githubOwner: String = providers.gradleProperty("githubOwner").orNull ?: System.getenv("GITHUB_OWNER") ?: "RhoMancer"
val githubUsername: String? = providers.gradleProperty("githubUser").orNull ?: System.getenv("GITHUB_USER")
val githubPassword: String? = providers.gradleProperty("githubToken").orNull ?: System.getenv("GITHUB_TOKEN")

dependencyResolutionManagement {
    repositories {
        google {
            androidxAndGoogleOnly()
        }
        mavenLocal()
        mavenCentral()

        maven {
            url = uri("https://maven.pkg.github.com/$githubOwner/Angus-Software-Theming")
            credentials {
                username = githubUsername
                password = githubPassword
            }
        }
    }
}

include(":composeApp")
