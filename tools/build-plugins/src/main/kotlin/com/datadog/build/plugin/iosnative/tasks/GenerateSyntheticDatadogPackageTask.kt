/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative.tasks

import com.datadog.build.plugin.iosnative.DatadogSpmBuildPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateSyntheticDatadogPackageTask : DefaultTask() {

    @get:Input
    abstract val sdkVersion: Property<String>

    @get:Input
    abstract val packageUrl: Property<String>

    @get:Input
    abstract val iosDeploymentTarget: Property<String>

    @get:Input
    abstract val tvosDeploymentTarget: Property<String>

    @get:Input
    abstract val iosProducts: ListProperty<String>

    @get:Input
    abstract val tvosProducts: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val rootDirFile = outputDirectory.get().asFile
        val target = DatadogSpmBuildPlugin.SYNTHETIC_TARGET_NAME

        // SPM requires a C target to have a public headers directory, even if it is empty.
        val targetSourcesDir = File(rootDirFile, "Sources/$target")
        File(targetSourcesDir, "include").mkdirs()
        File(targetSourcesDir, "include/.keep").writeText("")
        File(targetSourcesDir, "placeholder.c").writeText(
            """
            |void ${target.lowercase()}_placeholder(void) {}
            |
            """.trimMargin()
        )

        File(rootDirFile, "Package.swift").writeText(
            """
            |// swift-tools-version: 5.9
            |
            |// Synthetic package to prebuild Datadog native dependencies for KMP.
            |
            |import PackageDescription
            |
            |let package = Package(
            |    name: "${DatadogSpmBuildPlugin.SYNTHETIC_PACKAGE_NAME}",
            |    platforms: [
            |        .iOS("${iosDeploymentTarget.get()}"),
            |        .tvOS("${tvosDeploymentTarget.get()}")
            |    ],
            |    products: [
            |        .library(name: "$target", type: .static, targets: ["$target"])
            |    ],
            |    dependencies: [
            |        .package(url: "${packageUrl.get()}", exact: "${sdkVersion.get()}")
            |    ],
            |    targets: [
            |        .target(
            |            name: "$target",
            |            dependencies: [
            |${productDependencies()}
            |            ]
            |        )
            |    ]
            |)
            |
            """.trimMargin()
        )
    }

    private fun productDependencies(): String {
        val ios = iosProducts.get()
        val tvos = tvosProducts.get()
        return (ios + tvos).distinct().joinToString(separator = ",\n") { product ->
            val condition = when (product) {
                in ios if product in tvos -> ""
                in ios -> ", condition: .when(platforms: [.iOS])"
                else -> ", condition: .when(platforms: [.tvOS])"
            }
            "                .product(name: \"$product\", package: \"${packageIdentity()}\"$condition)"
        }
    }

    // SPM identifies remote packages by the last path component of their URL
    private fun packageIdentity(): String = packageUrl.get().substringAfterLast('/').removeSuffix(".git")
}
