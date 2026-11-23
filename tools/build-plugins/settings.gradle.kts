/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

import com.datadog.gradle.repositories.DatadogRepositories

pluginManagement {
    includeBuild("../settings-plugin")
}

plugins {
    id("com.datadog.gradle.repositories")
}

val datadogRepositories = DatadogRepositories(providers)

pluginManagement.repositories {
    datadogRepositories.gradlePluginPortalDepotProxied(this)
}

rootProject.name = "build-plugins"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
    @Suppress("UnstableApiUsage")
    repositories {
        datadogRepositories.googleDepotProxied(this)
        datadogRepositories.mavenCentralDepotProxied(this)
        datadogRepositories.gradlePluginPortalDepotProxied(this)
        datadogRepositories.jitpackConstrained(this)
    }
}
