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
    repositories {
        google()
        mavenCentral()
    }
}

include(":sample:androidApp")
include(":sample:shared")
include(":dd-sdk-kotlin-multiplatform-core")
include(":features:dd-sdk-kotlin-multiplatform-logs")
include(":features:dd-sdk-kotlin-multiplatform-rum")
include(":tools:unit:jvm")
