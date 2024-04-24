enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
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
