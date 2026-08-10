/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import com.datadog.build.utils.execShell
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class CheckCompilerMetadataTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:Input
    abstract var generationTaskName: String

    @get:InputFile
    abstract val metadataInfoFile: RegularFileProperty

    init {
        group = "datadog"
        description = "Check the compiler metadata of the library"
    }

    @TaskAction
    fun applyTask() {
        val lines = execOperations.execShell(
            "git",
            "diff",
            "--color=never",
            "HEAD",
            "--",
            metadataInfoFile.get().asFile.absolutePath
        )

        val additions = lines.filter { it.matches(Regex("^\\+[^+].*$")) }
        val removals = lines.filter { it.matches(Regex("^-[^-].*$")) }

        if (additions.isNotEmpty() || removals.isNotEmpty()) {
            error(
                "Make sure you run the $generationTaskName task before you push your PR.\n" +
                    additions.joinToString("\n") +
                    "\n" +
                    removals.joinToString("\n")
            )
        }
    }
}
