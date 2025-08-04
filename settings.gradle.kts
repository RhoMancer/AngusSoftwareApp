rootProject.name = "AngusSoftwareApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }

        mavenCentral()
        gradlePluginPortal()
    }
}

val githubUsername=
    System.getenv("GITHUB_USER")
val githubPassword =
    System.getenv("GITHUB_TOKEN")

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