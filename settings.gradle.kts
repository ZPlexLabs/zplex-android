pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ZPlex"
include(":app")
include(":mpv")
include(":common")
include(":feature-auth")
include(":googledrive")
include(":zplex-api")
include(":feature-home")
include(":feature-movies")
include(":feature-shows")
include(":feature-downloads")
