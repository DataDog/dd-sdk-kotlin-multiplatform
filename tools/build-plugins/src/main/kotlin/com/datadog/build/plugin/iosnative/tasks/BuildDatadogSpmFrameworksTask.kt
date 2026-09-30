/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

/**
 * Builds a scheme of the synthetic Datadog Swift package for a single platform slice and
 * repackages the SwiftPM build output as static frameworks consumable by cinterop and the K/N linker.
 *
 * SwiftPM emits a relocatable object file per module, with the generated `-Swift.h` headers and module maps
 * living in the intermediates. Each of the [productModules] becomes a standalone `<Module>.framework`, while all
 * other modules (transitive dependencies, like DatadogInternal or KSCrash) are bundled into [umbrellaFramework]:
 * unlike CocoaPods frameworks, SwiftPM objects don't carry autolinking entries for their package dependencies.
 *
 * Transitive Swift modules additionally get a header-only framework (their code already lives in the umbrella
 * binary), because depending on the Swift compiler version, generated headers of the product modules may
 * `@import` them (e.g. `DatadogCore-Swift.h` importing `DatadogInternal`).
 */
abstract class BuildDatadogSpmFrameworksTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val packageManifest: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val packageResolvedFile: RegularFileProperty

    @get:Input
    abstract val scheme: Property<String>

    @get:Input
    abstract val sdk: Property<String>

    @get:Input
    abstract val destination: Property<String>

    @get:Input
    abstract val productModules: ListProperty<String>

    @get:Input
    abstract val umbrellaFramework: Property<String>

    @get:Internal
    abstract val clonedSourcePackagesDirectory: DirectoryProperty

    @get:Internal
    abstract val derivedDataDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun build() {
        val derivedDataDir = derivedDataDirectory.get().asFile
        execOperations.exec {
            workingDir(packageManifest.get().asFile.parentFile)
            commandLine(
                "xcodebuild",
                "-quiet",
                "-scheme", scheme.get(),
                "-configuration", CONFIGURATION,
                "-sdk", sdk.get(),
                "-destination", destination.get(),
                "-derivedDataPath", derivedDataDir.absolutePath,
                "-clonedSourcePackagesDirPath", clonedSourcePackagesDirectory.get().asFile.absolutePath,
                "-disableAutomaticPackageResolution",
                "ONLY_ACTIVE_ARCH=NO",
                "SKIP_INSTALL=NO",
                "BUILD_LIBRARY_FOR_DISTRIBUTION=YES",
                "CODE_SIGNING_ALLOWED=NO",
                "build"
            )
        }

        val productsDir = derivedDataDir.resolve("Build/Products/$CONFIGURATION-${sdk.get()}")
        val generatedHeadersDir = derivedDataDir.resolve("Build/Intermediates.noindex/GeneratedModuleMaps-${sdk.get()}")
        val outputDir = outputDirectory.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val builtModules = productsDir.listFiles { file -> file.isFile && file.extension == "o" }
            .orEmpty()
            .map { it.nameWithoutExtension }
            .toSet()

        val products = productModules.get()
        val missingProducts = products - builtModules
        if (missingProducts.isNotEmpty()) {
            throw GradleException(
                "SwiftPM build of scheme ${scheme.get()} for ${sdk.get()} didn't produce modules $missingProducts " +
                    "(found: ${builtModules.sorted()})."
            )
        }

        products.forEach { module ->
            val frameworkDir = outputDir.resolve("$module.framework")
            assembleStaticFramework(frameworkDir, listOf(productsDir.resolve("$module.o")))
            assembleFrameworkModule(frameworkDir, productsDir, generatedHeadersDir)
        }

        val transitiveModules = (builtModules - products.toSet()).sorted()
        assembleStaticFramework(
            outputDir.resolve("${umbrellaFramework.get()}.framework"),
            transitiveModules.map { productsDir.resolve("$it.o") }
        )
        transitiveModules
            .filter { generatedHeadersDir.resolve("$it-Swift.h").exists() }
            .forEach { module ->
                assembleFrameworkModule(outputDir.resolve("$module.framework"), productsDir, generatedHeadersDir)
            }

        patchCrossDDCoreLoggerLevelModuleEnumForwardDeclaration(
            consumerHeader = outputDir.resolve("DatadogCore.framework/Headers/DatadogCore-Swift.h"),
            definingHeader = generatedHeadersDir.resolve("DatadogInternal-Swift.h")
        )
    }

    private fun assembleFrameworkModule(frameworkDir: File, productsDir: File, generatedHeadersDir: File) {
        val module = frameworkDir.nameWithoutExtension
        val swiftHeader = generatedHeadersDir.resolve("$module-Swift.h")
        if (!swiftHeader.exists()) {
            throw GradleException("Generated Objective-C header for $module is not found at $swiftHeader.")
        }
        swiftHeader.copyTo(frameworkDir.resolve("Headers/${swiftHeader.name}"))

        val modulesDir = frameworkDir.resolve("Modules")
        modulesDir.mkdirs()
        modulesDir.resolve("module.modulemap").writeText(
            """
            |framework module $module {
            |  header "${swiftHeader.name}"
            |  requires objc
            |  export *
            |}
            |
            """.trimMargin()
        )
        productsDir.resolve("$module.swiftmodule")
            .takeIf { it.isDirectory }
            ?.copyRecursively(modulesDir.resolve("$module.swiftmodule"))
    }

    private fun assembleStaticFramework(frameworkDir: File, objectFiles: List<File>) {
        frameworkDir.mkdirs()
        val binary = frameworkDir.resolve(frameworkDir.nameWithoutExtension)
        execOperations.exec {
            commandLine(
                listOf("libtool", "-static", "-no_warning_for_no_symbols", "-o", binary.absolutePath) +
                    objectFiles.map { it.absolutePath }
            )
        }
    }

    private companion object {
        const val CONFIGURATION = "Release"

        // TODO RUM-16671 DatadogCore Obj-C API declares DDCoreLoggerLevel from DatadogInternal
        //
        // Xcode 26 changed the Swift compiler's ObjC header generation to forward-declare
        // cross-module enum types (SWIFT_ENUM_FWD_DECL) rather than emit their full definition.
        // Kotlin/Native cinterop only processes a single framework's headers, so enum constants
        // defined in a dependency framework become unresolved. This patches each affected
        // framework header by replacing the forward declaration with the full definition sourced
        // from the module that actually defines the type.
        fun patchCrossDDCoreLoggerLevelModuleEnumForwardDeclaration(consumerHeader: File, definingHeader: File) {
            if (!consumerHeader.exists() || !definingHeader.exists()) return
            val consumerContent = consumerHeader.readText()
            val forwardDecl = "SWIFT_ENUM_FWD_DECL(NSInteger, DDCoreLoggerLevel)"
            if (!consumerContent.contains(forwardDecl)) return

            val enumDef = Regex(
                """typedef SWIFT_ENUM_NAMED\(NSInteger, DDCoreLoggerLevel[^;]*\};"""
            ).find(definingHeader.readText())?.value ?: return

            consumerHeader.writeText(consumerContent.replace(forwardDecl, enumDef))
        }
    }
}
