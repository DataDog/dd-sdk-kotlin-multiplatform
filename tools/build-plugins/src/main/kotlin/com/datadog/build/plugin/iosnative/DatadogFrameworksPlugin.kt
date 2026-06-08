/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative

import com.datadog.build.plugin.iosnative.tasks.GenerateDatadogCInteropDefsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

class DatadogFrameworksPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<DatadogFrameworksExtension>("datadogFrameworks")

        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            configureKmpProject(target, extension)
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    private fun configureKmpProject(project: Project, extension: DatadogFrameworksExtension) {
        val kmpExtension = project.extensions.getByType<KotlinMultiplatformExtension>()

        project.afterEvaluate {
            val rootBuildTask = project.rootProject.tasks.named(extension.rootBuildTaskName.get())
            val cinteropFrameworks = extension.frameworks.filter { !it.linkOnly.get() }
            val generateDefsTask = project.tasks.register<GenerateDatadogCInteropDefsTask>(
                "generateDatadogCInteropDefs"
            ) {
                frameworkNames.set(cinteropFrameworks.map { it.name })
                outputDirectory.set(project.layout.buildDirectory.dir("generated/datadog-cinterop-defs"))
            }

            kmpExtension.targets.withType<KotlinNativeTarget>().all {
                if (!konanTarget.family.isAppleFamily) return@all

                val frameworksDirectoryPath = project.rootProject.layout.buildDirectory
                    .dir(
                        when (konanTarget) {
                            KonanTarget.IOS_ARM64 -> "datadog-pods-build/ios-device"
                            KonanTarget.IOS_X64, KonanTarget.IOS_SIMULATOR_ARM64 -> "datadog-pods-build/ios-simulator"
                            KonanTarget.TVOS_ARM64 -> "datadog-pods-build/tvos-device"
                            KonanTarget.TVOS_X64, KonanTarget.TVOS_SIMULATOR_ARM64 ->
                                "datadog-pods-build/tvos-simulator"

                            else -> error("Unsupported Apple target for Datadog root pods linkage: $konanTarget")
                        }
                    )
                    .get()
                    .asFile
                    .absolutePath

                val mainCompilation = compilations.getByName("main")
                cinteropFrameworks
                    .filter { it.targetFamilies.get().contains(konanTarget.family) }
                    .forEach { spec ->
                        mainCompilation.cinterops.maybeCreate(spec.name).apply {
                            defFile(
                                generateDefsTask.map { task ->
                                    task.outputDirectory.file("${spec.name}.def").get().asFile
                                }
                            )
                            packageName(spec.packageName.get())
                            compilerOpts(*(spec.compilerOpts.get() + "-F$frameworksDirectoryPath").toTypedArray())
                        }
                    }

                val umbrellaFramework = when (konanTarget.family) {
                    Family.IOS -> extension.iosUmbrellaFramework.get()
                    Family.TVOS -> extension.tvosUmbrellaFramework.get()
                    else -> error("Unsupported Apple family for Datadog frameworks processing: ${konanTarget.family}")
                }

                val frameworkArgs = buildString {
                    append("-F$frameworksDirectoryPath -framework $umbrellaFramework")
                    extension.frameworks.forEach { framework ->
                        append(" -framework ${framework.name}")
                    }
                    val extraFrameworks = extension.extraFrameworks.get().toMutableList()
                    extraFrameworks.distinct().forEach { framework ->
                        append(" -framework $framework")
                    }
                    if (extension.includeObjcCategoryLinkerFlag.get()) {
                        append(" -ObjC")
                    }
                    if (extension.includeSwiftCompatibilityWorkaround.get()) {
                        swiftCompatibilityLibrarySearchPath(konanTarget)?.let { searchPath ->
                            append(" -L$searchPath")
                        }
                        append(" -lswiftCompatibilityConcurrency")
                        append(
                            " -U __swift_FORCE_LOAD_\$_swiftCompatibility50 " +
                                "-U __swift_FORCE_LOAD_\$_swiftCompatibility51 " +
                                "-U __swift_FORCE_LOAD_\$_swiftCompatibility56 " +
                                "-U __swift_FORCE_LOAD_\$_swiftCompatibilityConcurrency " +
                                "-U __swift_FORCE_LOAD_\$_swiftCompatibilityDynamicReplacements " +
                                "-U __swift_FORCE_LOAD_\$_swiftCompatibilityPacks"
                        )
                    }
                }

                compilerOptions {
                    freeCompilerArgs.addAll(
                        listOf(
                            "-linker-options",
                            frameworkArgs
                        )
                    )
                }

                binaries.configureEach {
                    linkTaskProvider.configure {
                        dependsOn(rootBuildTask, generateDefsTask)
                    }
                }
            }

            project.tasks.configureEach {
                if (name.startsWith("cinterop")) {
                    dependsOn(rootBuildTask, generateDefsTask)
                }
            }
        }
    }

    private fun swiftCompatibilityLibrarySearchPath(konanTarget: KonanTarget): String? {
        val platformDirectory = when (konanTarget) {
            KonanTarget.IOS_ARM64 -> "iphoneos"
            KonanTarget.IOS_X64, KonanTarget.IOS_SIMULATOR_ARM64 -> "iphonesimulator"
            KonanTarget.TVOS_ARM64 -> "appletvos"
            KonanTarget.TVOS_X64, KonanTarget.TVOS_SIMULATOR_ARM64 -> "appletvsimulator"
            else -> return null
        }

        return swiftToolchainUsrDirectory
            ?.resolve("lib/swift/$platformDirectory")
            ?.takeIf { it.isDirectory }
            ?.absolutePath
    }

    companion object {
        private val swiftToolchainUsrDirectory: File? by lazy { findSwiftToolchainUsrDirectory() }

        private fun findSwiftToolchainUsrDirectory(): File? {
            return runCatching {
                val process = ProcessBuilder("/usr/bin/xcrun", "--toolchain", "XcodeDefault", "--find", "swiftc")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText().trim()
                if (process.waitFor() == 0 && output.isNotBlank()) {
                    File(output).parentFile?.parentFile
                } else {
                    null
                }
            }.getOrNull()
        }
    }
}
