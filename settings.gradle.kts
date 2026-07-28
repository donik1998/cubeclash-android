pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "cubeclash-android"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":core:model")
include(":core:domain")
include(":core:designsystem")
include(":core:ui")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:realtime")
include(":core:testing")

include(":feature:auth")
include(":feature:timer")
include(":feature:race")
include(":feature:stats")
include(":feature:profile")
