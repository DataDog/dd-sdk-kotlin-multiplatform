/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import com.datadog.build.utils.execShell
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class CheckApiSurfaceTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @Input
    lateinit var sourceSetName: String

    @InputFile
    lateinit var surfaceFile: File

    init {
        group = "datadog"
    }

    // region Task

    @TaskAction
    fun applyTask() {
        val lines = execOperations.execShell(
            "git",
            "diff",
            "--color=never",
            "HEAD",
            "--",
            surfaceFile.absolutePath
        )

        val additions = lines.filter { it.matches(Regex("^\\+[^+].*$")) }
        val removals = lines.filter { it.matches(Regex("^-[^-].*$")) }

        if (additions.isNotEmpty() || removals.isNotEmpty()) {
            error(
                "Make sure you run the ${ApiSurfacePlugin.createGenerateApiSurfaceTaskName(sourceSetName)} task" +
                    " before you push your PR.\n" +
                    additions.joinToString("\n") + removals.joinToString("\n")
            )
        }
    }

    // endregion
}
