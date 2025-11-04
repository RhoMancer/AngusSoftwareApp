rootProject.name = "AngusSoftwareApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val githubUsername: String? =
    System.getenv("GITHUB_USER")
val githubPassword: String? =
    System.getenv("GITHUB_TOKEN")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // Resolve Angus Gradle Tools plugin markers from GitHub Packages instead of mavenLocal
        maven {
            url = uri("https://maven.pkg.github.com/RhoMancer/angus-gradle-tools")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
//        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
//        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/RhoMancer/Angus-Software-Theming")
            credentials {
                username = githubUsername
                password = githubPassword
            }
        }
    }
}

include(":composeApp")
