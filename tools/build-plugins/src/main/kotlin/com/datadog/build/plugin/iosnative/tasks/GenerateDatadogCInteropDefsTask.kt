/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateDatadogCInteropDefsTask : DefaultTask() {

    @get:Input
    abstract val frameworkNames: ListProperty<String>

    @get:Input
    abstract val frameworkPreImportModules: MapProperty<String, List<String>>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()

        frameworkNames.get().forEach { frameworkName ->
            val defFile = outputDir.resolve("$frameworkName.def")
            val modules = frameworkPreImportModules.get()[frameworkName].orEmpty() + frameworkName
            defFile.writeText(
                """
                |language = Objective-C
                |modules = ${modules.joinToString(" ")}
                |linkerOpts = -framework $frameworkName
                |
                """.trimMargin()
            )
        }
    }
}
