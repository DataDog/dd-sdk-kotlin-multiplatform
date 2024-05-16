/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import com.datadog.build.utils.taskConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

class ApiSurfacePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val commonGenerateApiSurfaceTask = target.tasks.create("generateApiSurface") {
            group = "datadog"
            description = "Generate the API surface for all eligible source sets"
        }

        val commonCheckApiSurfaceChangesTask = target.tasks.create("checkApiSurfaceChanges") {
            group = "datadog"
            description = "Check the API surface changes for all eligible source sets"
        }

        target.kotlinExtension.sourceSets.all {
            val sourceSetName = name
            if (sourceSetName.contains("test", ignoreCase = true) ||
                !sourceSetName.contains("main", ignoreCase = true)
            ) {
                return@all
            }

            val sourceFiles = kotlin.sourceDirectories
            // no generated files yet, but let's keep it for the future, we will probably need to generate models
            // for the mappers
            val generatedFiles =
                target.files(File(target.layout.buildDirectory.dir("generated").get().asFile, "json2kotlin"))
            val apiDir = File(File(target.projectDir, "api"), name)
            val surfaceFile = File(apiDir, FILE_NAME)
            val generateApiSurfaceTaskName = createGenerateApiSurfaceTaskName(sourceSetName)
            val checkApiSurfaceTaskName = createCheckApiSurfaceChangesTaskName(sourceSetName)

            val generateApiSurfaceTask = target.tasks
                .register(generateApiSurfaceTaskName, GenerateApiSurfaceTask::class.java) {
                    this.sourceFiles = sourceFiles
                    this.generatedFiles = generatedFiles
                    this.surfaceFile = surfaceFile
                    this.description = "Generate the API surface of the $sourceSetName source set"
                    commonGenerateApiSurfaceTask.dependsOn(this)
                }
            target.tasks
                .register(checkApiSurfaceTaskName, CheckApiSurfaceTask::class.java) {
                    this.sourceSetName = sourceSetName
                    this.description = "Check the API surface of the $sourceSetName source set"
                    this.surfaceFile = surfaceFile
                    dependsOn(generateApiSurfaceTask)
                    commonCheckApiSurfaceChangesTask.dependsOn(this)
                }

            target.taskConfig<KotlinCompile> {
                finalizedBy(generateApiSurfaceTask)
            }
        }
    }

    companion object {
        const val FILE_NAME = "apiSurface"

        internal fun createGenerateApiSurfaceTaskName(sourceSetName: String) =
            "generate${sourceSetName.capitalize()}ApiSurface"

        private fun createCheckApiSurfaceChangesTaskName(sourceSetName: String) =
            "check${sourceSetName.capitalize()}ApiSurfaceChanges"
    }
}
