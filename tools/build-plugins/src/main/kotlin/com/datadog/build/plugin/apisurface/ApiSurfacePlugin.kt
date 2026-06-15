/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import com.datadog.build.utils.taskConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.presetName
import java.util.Locale
import kotlin.io.path.Path

class ApiSurfacePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val commonGenerateApiSurfaceTask = target.tasks.register("generateApiSurface") {
            group = "datadog"
            description = "Generate the API surface for all eligible source sets"
        }
        val commonGenerateCompilerMetadataTask = target.tasks.register("generateCompilerMetadata") {
            group = "datadog"
            description = "Generate compiler metadata for all eligible targets"
        }

        val commonCheckApiSurfaceChangesTask = target.tasks.register("checkApiSurfaceChanges") {
            group = "datadog"
            description = "Check the API surface changes for all eligible source sets"
        }
        val commonCheckCompilerMetadataChangesTask = target.tasks.register("checkCompilerMetadataChanges") {
            group = "datadog"
            description = "Check compiler metadata changes for all eligible targets"
        }

        target.kotlinExtension.sourceSets.all {
            val sourceSetName = name
            if (sourceSetName.contains("test", ignoreCase = true) ||
                !sourceSetName.contains("main", ignoreCase = true)
            ) {
                return@all
            }

            val sourceFiles = kotlin.sourceDirectories
            if (sourceFiles.isEmpty || sourceFiles.none { it.exists() }) return@all

            val surfaceFile = target.layout.projectDirectory.file(
                Path("api", name, API_SURFACE_FILE_NAME).toString()
            )
            val generateApiSurfaceTaskName = createGenerateApiSurfaceTaskName(sourceSetName)
            val checkApiSurfaceTaskName = createCheckApiSurfaceChangesTaskName(sourceSetName)

            val generateApiSurfaceTask = target.tasks
                .register(generateApiSurfaceTaskName, GenerateApiSurfaceTask::class.java) {
                    this.sourceFiles.from(sourceFiles)
                    this.surfaceFile.set(surfaceFile)
                    this.description = "Generate the API surface of the $sourceSetName source set"
                }
            commonGenerateApiSurfaceTask.configure { dependsOn(generateApiSurfaceTask) }
            val checkApiSurfaceTask = target.tasks
                .register(checkApiSurfaceTaskName, CheckApiSurfaceTask::class.java) {
                    this.sourceSetName = sourceSetName
                    this.description = "Check the API surface of the $sourceSetName source set"
                    this.surfaceFile.set(generateApiSurfaceTask.flatMap { it.surfaceFile })
                }
            commonCheckApiSurfaceChangesTask.configure { dependsOn(checkApiSurfaceTask) }

            target.taskConfig<KotlinCompile> {
                finalizedBy(generateApiSurfaceTask)
            }
        }

        target.afterEvaluate {
            target.registerCompilerMetadataTasks(
                commonGenerateCompilerMetadataTask = commonGenerateCompilerMetadataTask,
                commonCheckCompilerMetadataChangesTask = commonCheckCompilerMetadataChangesTask
            )
        }
    }

    private fun Project.registerCompilerMetadataTasks(
        commonGenerateCompilerMetadataTask: TaskProvider<Task>,
        commonCheckCompilerMetadataChangesTask: TaskProvider<Task>
    ) {
        val androidTarget = extensions.getByType<KotlinMultiplatformExtension>().targets
            .first { it.platformType == KotlinPlatformType.androidJvm }
        registerJvmCompilerMetadataTask(
            compilation = checkNotNull(androidTarget.compilations.findByName(COMPILATION_DEBUG)),
            commonGenerateCompilerMetadataTask = commonGenerateCompilerMetadataTask,
            commonCheckCompilerMetadataChangesTask = commonCheckCompilerMetadataChangesTask
        )

        if (!System.getProperty("os.name").lowercase().contains("mac")) {
            logger.info(
                "Generating Apple targets is not supported on the current system," +
                    " skipping Klib compiler metadata info tasks creation."
            )
            return
        }

        val nativeTargets = extensions.getByType<KotlinMultiplatformExtension>().targets
            .filterIsInstance<KotlinNativeTarget>()
        val multipleAppleFamilies = nativeTargets
            .filter { it.konanTarget.family.isAppleFamily }
            .map { it.konanTarget.family }
            .toSet()
            .count() > 1
        val appleTarget = nativeTargets
            .first {
                it.konanTarget.family == Family.IOS &&
                    it.konanTarget.presetName.lowercase().contains("simulator")
            }
        registerKlibCompilerMetadataTask(
            sourceSetName = if (multipleAppleFamilies) SOURCE_SET_APPLE_MAIN else SOURCE_SET_IOS_MAIN,
            kotlinCompileTask = checkNotNull(appleTarget.compilations.findByName(COMPILATION_MAIN)).compileTaskProvider,
            commonGenerateCompilerMetadataTask = commonGenerateCompilerMetadataTask,
            commonCheckCompilerMetadataChangesTask = commonCheckCompilerMetadataChangesTask
        )
    }

    private fun Project.registerJvmCompilerMetadataTask(
        compilation: KotlinCompilation<*>,
        commonGenerateCompilerMetadataTask: TaskProvider<Task>,
        commonCheckCompilerMetadataChangesTask: TaskProvider<Task>
    ) {
        val compilerMetadata = compilation.jvmCompilerMetadataOrNull(this) ?: return

        val generateCompilerMetadataTask = registerJvmCompilerMetadataTask(
            target = this,
            sourceSetName = compilerMetadata.sourceSetName,
            compilation = compilation,
            apiDir = compilerMetadata.apiDir
        )
        commonGenerateCompilerMetadataTask.configure { dependsOn(generateCompilerMetadataTask) }

        val checkCompilerMetadataTask = registerCheckCompilerMetadataTask(
            target = this,
            sourceSetName = compilerMetadata.sourceSetName,
            generateCompilerMetadataTaskName = generateCompilerMetadataTask.name,
            compilerMetadataFile = compilerMetadata.apiDir.compilerMetadataFile()
        ) {
            dependsOn(generateCompilerMetadataTask)
        }
        commonCheckCompilerMetadataChangesTask.configure { dependsOn(checkCompilerMetadataTask) }
    }

    private fun Project.registerKlibCompilerMetadataTask(
        sourceSetName: String,
        kotlinCompileTask: TaskProvider<KotlinNativeCompile>,
        commonGenerateCompilerMetadataTask: TaskProvider<Task>,
        commonCheckCompilerMetadataChangesTask: TaskProvider<Task>
    ) {
        val compilerMetadata = compilerMetadata(
            sourceSetName = sourceSetName,
            project = this
        )

        val generateCompilerMetadataTask = registerKlibCompilerMetadataTask(
            target = this,
            sourceSetName = compilerMetadata.sourceSetName,
            kotlinCompileTask = kotlinCompileTask,
            apiDir = compilerMetadata.apiDir
        )
        commonGenerateCompilerMetadataTask.configure { dependsOn(generateCompilerMetadataTask) }

        val checkCompilerMetadataTask = registerCheckCompilerMetadataTask(
            target = this,
            sourceSetName = compilerMetadata.sourceSetName,
            generateCompilerMetadataTaskName = generateCompilerMetadataTask.name,
            compilerMetadataFile = compilerMetadata.apiDir.compilerMetadataFile()
        ) {
            dependsOn(generateCompilerMetadataTask)
        }
        commonCheckCompilerMetadataChangesTask.configure { dependsOn(checkCompilerMetadataTask) }
    }

    private fun KotlinCompilation<*>.jvmCompilerMetadataOrNull(project: Project): CompilerMetadataRegistration? {
        if (compilationName != COMPILATION_DEBUG) return null

        return compilerMetadata(
            sourceSetName = SOURCE_SET_ANDROID_MAIN,
            project = project
        )
    }

    private fun compilerMetadata(
        sourceSetName: String,
        project: Project
    ): CompilerMetadataRegistration {
        return CompilerMetadataRegistration(
            sourceSetName = sourceSetName,
            apiDir = project.layout.projectDirectory.dir(Path("api", sourceSetName).toString())
        )
    }

    private fun registerJvmCompilerMetadataTask(
        target: Project,
        sourceSetName: String,
        compilation: KotlinCompilation<*>,
        apiDir: Directory
    ): TaskProvider<GenerateJvmCompilerMetadataTask> {
        val generateTask = target.tasks.register<GenerateJvmCompilerMetadataTask>(
            createGenerateCompilerMetadataTaskName(sourceSetName)
        ) {
            description = "Generate compiler metadata of the $sourceSetName source set"
            compiledClassesDirectory.set(
                compilation.compileTaskProvider.flatMap { (it as KotlinCompileTool).destinationDirectory }
            )
            metadataInfoFile.set(apiDir.compilerMetadataFile())
        }
        compilation.compileTaskProvider.configure { finalizedBy(generateTask) }
        return generateTask
    }

    private fun registerKlibCompilerMetadataTask(
        target: Project,
        sourceSetName: String,
        kotlinCompileTask: TaskProvider<KotlinNativeCompile>,
        apiDir: Directory
    ): TaskProvider<GenerateKlibCompilerMetadataTask> {
        val generateTask = target.tasks.register<GenerateKlibCompilerMetadataTask>(
            createGenerateCompilerMetadataTaskName(sourceSetName)
        ) {
            description = "Generate compiler metadata of the $sourceSetName source set"
            compiledKlibDirectory.set(kotlinCompileTask.flatMap { it.klibDirectory })
            metadataInfoFile.set(apiDir.compilerMetadataFile())
            // klibDirectory above is @Internal, so doesn't create explicit dependency
            dependsOn(kotlinCompileTask)
        }
        kotlinCompileTask.configure { finalizedBy(generateTask) }
        return generateTask
    }

    private fun registerCheckCompilerMetadataTask(
        target: Project,
        sourceSetName: String,
        generateCompilerMetadataTaskName: String,
        compilerMetadataFile: RegularFile,
        configure: CheckCompilerMetadataTask.() -> Unit
    ) = target.tasks.register<CheckCompilerMetadataTask>(
        createCheckCompilerMetadataChangesTaskName(sourceSetName)
    ) {
        this.generationTaskName = generateCompilerMetadataTaskName
        this.metadataInfoFile.set(compilerMetadataFile)
        this.description = "Check compiler metadata of the $sourceSetName source set"
        configure()
    }

    private fun Directory.compilerMetadataFile(): RegularFile = file(COMPILER_METADATA_FILE_NAME)

    private data class CompilerMetadataRegistration(
        val sourceSetName: String,
        val apiDir: Directory
    )

    companion object {
        const val API_SURFACE_FILE_NAME = "apiSurface"
        const val COMPILER_METADATA_FILE_NAME = "compiler-meta.txt"
        private const val SOURCE_SET_ANDROID_MAIN = "androidMain"
        private const val SOURCE_SET_IOS_MAIN = "iosMain"
        private const val SOURCE_SET_APPLE_MAIN = "appleMain"
        private const val COMPILATION_DEBUG = "debug"
        private const val COMPILATION_MAIN = "main"

        private fun String.capitalize() =
            replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }

        internal fun createGenerateApiSurfaceTaskName(sourceSetName: String) =
            "generate${sourceSetName.capitalize()}ApiSurface"

        private fun createCheckApiSurfaceChangesTaskName(sourceSetName: String) =
            "check${sourceSetName.capitalize()}ApiSurfaceChanges"

        internal fun createGenerateCompilerMetadataTaskName(sourceSetName: String) =
            "generate${sourceSetName.capitalize()}CompilerMetadata"

        private fun createCheckCompilerMetadataChangesTaskName(sourceSetName: String) =
            "check${sourceSetName.capitalize()}CompilerMetadataChanges"
    }
}
