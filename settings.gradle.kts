enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
            mavenContent {
                includeGroupByRegex("com\\.github\\..*")
            }
        }
    }
    includeBuild("./tools/build-config")
}

rootProject.name = "dd-sdk-kotlin-multiplatform"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

include(":sample:androidApp")
include(":sample:shared")
include(":core")
include(":features:logs")
include(":features:rum")
include(":features:webview")
include(":tools:unit")
