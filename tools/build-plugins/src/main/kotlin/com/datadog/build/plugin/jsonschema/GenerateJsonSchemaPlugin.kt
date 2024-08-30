/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema

import com.datadog.build.plugin.gitclone.GitCloneDependenciesExtension
import com.datadog.build.plugin.gitclone.GitCloneDependenciesTask
import com.datadog.build.utils.ddCapitalize
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class GenerateJsonSchemaPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<GenerateJsonSchemaExtension>("jsonSchemaGenerator")
        target.afterEvaluate {
            extension.jsonSchemas.forEach { (name, schema) ->
                when (val location = schema.location.get()) {
                    is SchemaLocation.Git -> {
                        target.tasks.register<GitCloneDependenciesTask>("clone${name.ddCapitalize()}Schema") {
                            this.extension = GitCloneDependenciesExtension().apply {
                                dependencies.add(
                                    GitCloneDependenciesExtension.Dependency(
                                        originRepository = location.repo,
                                        originSubFolder = location.subFolder,
                                        excludedPrefixes = location.excludedPrefixes,
                                        originRef = location.ref,
                                        destinationFolder = location.destinationFolder
                                    )
                                )
                            }
                        }
                    }

                    else -> {
                        // do nothing
                    }
                }

                registerModelsGenerationTask(target, name, schema)

                target.tasks.withType<KotlinCompile> {
                    dependsOn(target.tasks.withType<GenerateJsonSchemaTask>())
                }
            }
        }

        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            target.extensions.getByType<KotlinMultiplatformExtension>().run {
                target.afterEvaluate {
                    sourceSets.commonMain {
                        kotlin.srcDir("build/generated/json2kotlin/commonMain/kotlin")
                    }
                    val jsonSchemas = extension.jsonSchemas.values
                    if (jsonSchemas.any { it.androidModelsMappingGeneration.enabled.getOrElse(false) }) {
                        sourceSets.androidMain {
                            kotlin.srcDir("build/generated/json2kotlin/androidMain/kotlin")
                        }
                    }
                    if (jsonSchemas.any { it.iosModelsMappingGeneration.enabled.getOrElse(false) }) {
                        sourceSets.iosMain {
                            kotlin.srcDir("build/generated/json2kotlin/iosMain/kotlin")
                        }
                    }
                }
            }
        }
    }

    private fun registerModelsGenerationTask(target: Project, schemaName: String, schema: JsonSchema) {
        target.tasks.register<GenerateJsonSchemaTask>("generate${schemaName.ddCapitalize()}ModelsFromJson") {
            inputDirPath = when (val location = schema.location.get()) {
                is SchemaLocation.Git -> location.destinationFolder
                is SchemaLocation.Local -> location.path
            }
            targetPackageName = schema.targetPackageName.get()
            ignoredFiles = schema.ignoredFiles.getOrElse(emptyList()).toTypedArray()
            inputNameMapping = schema.inputNameMapping.getOrElse(emptyMap())
            androidModelsMappingGeneration.set(schema.androidModelsMappingGeneration)
            iosModelsMappingGeneration.set(schema.iosModelsMappingGeneration)
        }
    }
}
