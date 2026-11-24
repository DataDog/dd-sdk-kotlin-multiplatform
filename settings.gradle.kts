import com.datadog.gradle.repositories.DatadogRepositories

pluginManagement {
    // Repository definitions shared with tools/build-plugins.
    includeBuild("./tools/settings-plugin")
    includeBuild("./tools/build-plugins")
}

plugins {
    id("com.datadog.gradle.repositories")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val datadogRepositories = DatadogRepositories(providers)

pluginManagement.repositories {
    datadogRepositories.googleDepotProxied(this)
    datadogRepositories.gradlePluginPortalDepotProxied(this)
    datadogRepositories.mavenCentralDepotProxied(this)
    datadogRepositories.jitpackConstrained(this)
}

rootProject.name = "dd-sdk-kotlin-multiplatform"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        datadogRepositories.googleDepotProxied(this)
        datadogRepositories.mavenCentralDepotProxied(this)
    }
}

include(":sample:androidApp")
include(":sample:shared")
include(":sample:shared-ui")
include(":core")
include(":features:logs")
include(":features:rum")
include(":features:webview")
include(":features:session-replay")
include(":integrations:ktor")
include(":integrations:ktor3")
include(":integrations:coil3")
include(":tools:unit")
