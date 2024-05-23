/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.dependencyLicense) apply false
    alias(libs.plugins.mokkery) apply false
}

/**
 * Task necessary to be compliant with the shared Android static analysis pipeline
 */
tasks.register("checkGeneratedFiles") { }

fun registerPluginSpecificAggregationTask(
    aggregationTaskName: String,
    projectTaskName: String,
    pluginName: String
) {
    tasks.register(aggregationTaskName) {
        val aggregationTask = this
        allprojects.forEach {
            it.pluginManager.withPlugin(pluginName) {
                val projectTask = it.tasks.named(projectTaskName)
                aggregationTask.dependsOn(projectTask)
            }
        }
    }
}

registerPluginSpecificAggregationTask(
    "checkApiSurfaceChangesAll",
    "checkApiSurfaceChanges",
    "api-surface"
)
registerPluginSpecificAggregationTask(
    "generateApiSurfaceAll",
    "generateApiSurface",
    "api-surface"
)

registerPluginSpecificAggregationTask(
    "listTransitiveDependenciesAll",
    "listTransitiveDependencies",
    "transitive-dependencies"
)

val publishableProjects = listOf(
    projects.ddSdkKotlinMultiplatformCore,
    projects.features.ddSdkKotlinMultiplatformLogs
)

val jvmUnitTestDebugAllTask = tasks.register("jvmUnitTestDebugAll") {
    val subProjectsTestTasks = publishableProjects.map {
        "${it.identityPath.path}:testDebugUnitTest"
    }
    dependsOn(subProjectsTestTasks)
}

val jvmUnitTestReleaseAllTask = tasks.register("jvmUnitTestReleaseAll") {
    val subProjectsTestTasks = publishableProjects.map {
        "${it.identityPath.path}:testReleaseUnitTest"
    }
    dependsOn(subProjectsTestTasks)
}

// will cover Android-specific tests + tests from common source set
tasks.register("jvmUnitTestAll") {
    dependsOn(jvmUnitTestDebugAllTask, jvmUnitTestReleaseAllTask)
}

tasks.register("iosUnitTestAll") {
    val subProjectsTestTasks = publishableProjects.map {
        "${it.identityPath.path}:iosSimulatorArm64Test"
    }
    dependsOn(subProjectsTestTasks)
}
