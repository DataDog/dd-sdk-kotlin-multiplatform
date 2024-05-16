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
}

/**
 * Task necessary to be compliant with the shared Android static analysis pipeline
 */
tasks.register("checkGeneratedFiles") { }

fun registerApiSurfaceAggregationTask(aggregationTaskName: String, projectTaskName: String) {
    tasks.register(aggregationTaskName) {
        val aggregationTask = this
        allprojects.forEach {
            it.pluginManager.withPlugin("api-surface") {
                val projectTask = it.tasks.named(projectTaskName)
                aggregationTask.dependsOn(projectTask)
            }
        }
    }
}

registerApiSurfaceAggregationTask("checkApiSurfaceChangesAll", "checkApiSurfaceChanges")
registerApiSurfaceAggregationTask("generateApiSurfaceAll", "generateApiSurface")
