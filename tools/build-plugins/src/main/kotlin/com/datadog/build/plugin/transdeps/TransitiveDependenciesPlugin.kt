/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.transdeps

import com.datadog.build.utils.taskConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class TransitiveDependenciesPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // Only Android is supported
        val listTransitiveDependenciesTask =
            target.tasks.register<TransitiveDependenciesTask>(LIST_TRANSITIVE_DEPS_TASK_NAME) {
                transitiveDependenciesFile.set(target.layout.projectDirectory.file("android-transitiveDependencies"))
                resolvedArtifacts.from(
                    target.configurations.named("androidCompileClasspath").map {
                        it.incoming.artifactView {
                            componentFilter { it !is ProjectComponentIdentifier }
                        }.files
                    }
                )
            }

        target.tasks.register<CheckTransitiveDependenciesTask>(CHECK_TRANSITIVE_DEPS_TASK_NAME) {
            generationTaskName = LIST_TRANSITIVE_DEPS_TASK_NAME
            transitiveDependenciesFile.set(listTransitiveDependenciesTask.flatMap { it.transitiveDependenciesFile })
        }

        target.taskConfig<KotlinCompile> {
            finalizedBy(listTransitiveDependenciesTask)
        }
    }

    companion object {

        const val LIST_TRANSITIVE_DEPS_TASK_NAME = "listTransitiveDependencies"
        const val CHECK_TRANSITIVE_DEPS_TASK_NAME = "checkTransitiveDependenciesList"
    }
}
