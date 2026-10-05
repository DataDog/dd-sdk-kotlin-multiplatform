/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative

import com.datadog.build.plugin.iosnative.tasks.BuildDatadogSpmFrameworksTask
import com.datadog.build.plugin.iosnative.tasks.GenerateSyntheticDatadogPackageTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class DatadogSpmBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<DatadogSpmBuildExtension>("datadogSpmBuild")

        val syntheticPackageDir = target.layout.buildDirectory.dir("datadog-spm")
        val clonedSourcePackagesDir = syntheticPackageDir.map { it.dir("SourcePackages") }
        val frameworksBuildDir = target.layout.buildDirectory.dir(FRAMEWORKS_BUILD_DIRECTORY)

        val generateSyntheticDatadogPackage = target.tasks.register<GenerateSyntheticDatadogPackageTask>(
            "generateSyntheticDatadogPackage"
        ) {
            outputDirectory.set(syntheticPackageDir.map { it.dir("package") })
            sdkVersion.set(extension.sdkVersion)
            packageUrl.set(extension.packageUrl)
            iosDeploymentTarget.set(extension.iosDeploymentTarget)
            tvosDeploymentTarget.set(extension.tvosDeploymentTarget)
            iosProducts.set(extension.iosProducts)
            tvosProducts.set(extension.tvosProducts)
        }
        val packageDir = generateSyntheticDatadogPackage.flatMap { it.outputDirectory }

        // Resolving once upfront lets the per-slice builds share the checkouts and run without resolution
        val resolveDatadogPackage = target.tasks.register<Exec>("resolveDatadogPackage") {
            dependsOn(generateSyntheticDatadogPackage)
            workingDir(packageDir)
            commandLine(
                "xcodebuild",
                "-resolvePackageDependencies",
                "-clonedSourcePackagesDirPath",
                clonedSourcePackagesDir.get().asFile.absolutePath
            )
            inputs.file(packageDir.map { it.file("Package.swift") })
            outputs.file(packageDir.map { it.file("Package.resolved") })
            outputs.dir(clonedSourcePackagesDir)
        }

        fun registerSpmBuildTask(
            taskName: String,
            products: ListProperty<String>,
            sdk: String,
            destination: String,
            outputSubdirectory: String
        ) = target.tasks.register<BuildDatadogSpmFrameworksTask>(taskName) {
            dependsOn(resolveDatadogPackage)
            packageManifest.set(packageDir.map { it.file("Package.swift") })
            packageResolvedFile.set(packageDir.map { it.file("Package.resolved") })
            clonedSourcePackagesDirectory.set(clonedSourcePackagesDir)
            derivedDataDirectory.set(syntheticPackageDir.map { it.dir("DerivedData/$outputSubdirectory") })
            scheme.set(SYNTHETIC_TARGET_NAME)
            this.sdk.set(sdk)
            this.destination.set(destination)
            productModules.set(products)
            umbrellaFramework.set(SYNTHETIC_TARGET_NAME)
            outputDirectory.set(frameworksBuildDir.map { it.dir(outputSubdirectory) })
        }

        val buildDatadogIosDeviceFrameworks = registerSpmBuildTask(
            taskName = "buildDatadogIosDeviceFrameworks",
            products = extension.iosProducts,
            sdk = "iphoneos",
            destination = "generic/platform=iOS",
            outputSubdirectory = IOS_DEVICE_SLICE
        )

        val buildDatadogIosSimulatorFrameworks = registerSpmBuildTask(
            taskName = "buildDatadogIosSimulatorFrameworks",
            products = extension.iosProducts,
            sdk = "iphonesimulator",
            destination = "generic/platform=iOS Simulator",
            outputSubdirectory = IOS_SIMULATOR_SLICE
        )

        val buildDatadogTvosDeviceFrameworks = registerSpmBuildTask(
            taskName = "buildDatadogTvosDeviceFrameworks",
            products = extension.tvosProducts,
            sdk = "appletvos",
            destination = "generic/platform=tvOS",
            outputSubdirectory = TVOS_DEVICE_SLICE
        )

        val buildDatadogTvosSimulatorFrameworks = registerSpmBuildTask(
            taskName = "buildDatadogTvosSimulatorFrameworks",
            products = extension.tvosProducts,
            sdk = "appletvsimulator",
            destination = "generic/platform=tvOS Simulator",
            outputSubdirectory = TVOS_SIMULATOR_SLICE
        )

        target.tasks.register(BUILD_FRAMEWORKS_TASK_NAME) {
            group = "swift package manager"
            description = "Generates a synthetic Swift package, resolves Datadog SDK and builds iOS/tvOS " +
                "frameworks once at root."
            dependsOn(
                buildDatadogIosDeviceFrameworks,
                buildDatadogIosSimulatorFrameworks,
                buildDatadogTvosDeviceFrameworks,
                buildDatadogTvosSimulatorFrameworks
            )
        }
    }

    companion object {
        const val BUILD_FRAMEWORKS_TASK_NAME = "buildDatadogFrameworks"
        const val FRAMEWORKS_BUILD_DIRECTORY = "datadog-spm-build"
        const val SYNTHETIC_PACKAGE_NAME = "DatadogSynthetic"

        // Single product with platform-conditional dependencies, also serves as the umbrella framework name
        const val SYNTHETIC_TARGET_NAME = SYNTHETIC_PACKAGE_NAME

        const val IOS_DEVICE_SLICE = "ios-device"
        const val IOS_SIMULATOR_SLICE = "ios-simulator"
        const val TVOS_DEVICE_SLICE = "tvos-device"
        const val TVOS_SIMULATOR_SLICE = "tvos-simulator"
    }
}
