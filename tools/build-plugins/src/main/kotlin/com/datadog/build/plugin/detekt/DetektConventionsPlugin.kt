/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.detekt

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

/**
 * Applies Detekt with the shared `detekt.yml` configuration.
 *
 * Rules requiring type resolution are silently skipped by the plain `detekt` task, because it has no
 * classpath. Detekt registers type-resolved tasks for JVM/Android compilations (e.g. `detektMainAndroid`,
 * `detektTestJvm`), covering all source sets of the compilation (including `commonMain`/`commonTest`),
 * and for Android application modules (`detektMain`, `detektTest`), so `detekt` is made to depend on them.
 * The plain `detekt` task covers the rest: source sets only compiled for Apple targets and the module
 * scripts, so that each file is analyzed exactly once.
 */
class DetektConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(DETEKT_PLUGIN_ID)

        // Source directories analyzed by the type-resolved Detekt tasks (JVM/Android compilations).
        val typeResolvedSourceDirs = target.files()

        configureExtension(target, typeResolvedSourceDirs)
        configureTasks(target)

        target.pluginManager.withPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID) {
            configureTypeResolvedTasks(target, typeResolvedSourceDirs)
        }
        target.pluginManager.withPlugin(ANDROID_APPLICATION_PLUGIN_ID) {
            configureAndroidApplicationTypeResolvedTasks(target, typeResolvedSourceDirs)
        }
    }

    private fun configureExtension(target: Project, typeResolvedSourceDirs: ConfigurableFileCollection) {
        target.extensions.configure<DetektExtension> {
            config.setFrom(target.rootProject.file(CONFIG_FILE_NAME))
            basePath.set(target.rootProject.layout.projectDirectory)
            buildUponDefaultConfig.set(false)
            parallel.set(true)
            val allSources = target.fileTree(target.layout.projectDirectory) {
                include("src/**/*.kt", "src/**/*.kts", "*.kts")
            }
            source.setFrom(
                target.provider {
                    // resolved once per evaluation of the task sources, not for every file
                    val excludedDirs = typeResolvedSourceDirs.files
                    allSources.filter { file -> excludedDirs.none { dir -> file.startsWith(dir) } }
                }
            )
        }
    }

    private fun configureTasks(target: Project) {
        val buildDir = target.layout.buildDirectory.get().asFile
        target.tasks.withType<Detekt>().configureEach {
            jvmTarget.set(JVM_TARGET)
            exclude { it.file.startsWith(buildDir) }
            reports {
                sarif.required.set(false)
                html.required.set(false)
                markdown.required.set(false)
                checkstyle.required.set(false)
            }
        }
    }

    private fun configureTypeResolvedTasks(target: Project, typeResolvedSourceDirs: ConfigurableFileCollection) {
        val kotlin = target.extensions.getByType<KotlinMultiplatformExtension>()
        kotlin.targets
            .matching { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm }
            .all {
                val kotlinTarget = this
                compilations.all {
                    val compilation = this
                    val taskName = DETEKT_TASK_NAME +
                        compilation.name.replaceFirstChar(Char::uppercase) +
                        kotlinTarget.name.replaceFirstChar(Char::uppercase)
                    registerTypeResolvedTasks(
                        target,
                        typeResolvedSourceDirs,
                        taskNames = setOf(taskName),
                        sourceDirs = { compilation.allKotlinSourceSets.map { it.kotlin.sourceDirectories } }
                    )
                }
            }
    }

    private fun configureAndroidApplicationTypeResolvedTasks(
        target: Project,
        typeResolvedSourceDirs: ConfigurableFileCollection
    ) {
        // `detektMain`/`detektTest` cover the production and test sources of all variants
        registerTypeResolvedTasks(
            target,
            typeResolvedSourceDirs,
            taskNames = setOf(DETEKT_MAIN_TASK_NAME, DETEKT_TEST_TASK_NAME),
            sourceDirs = { listOf(target.layout.projectDirectory.dir("src")) }
        )
    }

    /**
     * Makes the plain `detekt` task depend on the given type-resolved tasks and excludes their sources
     * from it. Sources are only excluded if all the tasks exist, so that a task naming mismatch (e.g. after a
     * Detekt or AGP update) doesn't silently remove files from the analysis.
     */
    private fun registerTypeResolvedTasks(
        target: Project,
        typeResolvedSourceDirs: ConfigurableFileCollection,
        taskNames: Set<String>,
        sourceDirs: () -> List<Any>
    ) {
        typeResolvedSourceDirs.from(
            target.provider {
                if (target.tasks.names.containsAll(taskNames)) sourceDirs() else emptyList()
            }
        )
        target.tasks.named(DETEKT_TASK_NAME) {
            // not filtering by `Detekt` type: Android `detektMain`/`detektTest` are lifecycle tasks
            dependsOn(target.tasks.named { it in taskNames })
        }
    }

    private companion object {
        const val DETEKT_PLUGIN_ID = "dev.detekt"
        const val KOTLIN_MULTIPLATFORM_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"
        const val ANDROID_APPLICATION_PLUGIN_ID = "com.android.application"
        const val DETEKT_TASK_NAME = "detekt"
        const val DETEKT_MAIN_TASK_NAME = "detektMain"
        const val DETEKT_TEST_TASK_NAME = "detektTest"
        const val CONFIG_FILE_NAME = "detekt.yml"
        const val JVM_TARGET = "17"
    }
}
