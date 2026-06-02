/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.iosnative.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class CheckNoDatadogPodsOutsideRootTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val buildFiles: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val datadogPodRegex = Regex("""pod\("Datadog[A-Za-z0-9]*"\)""")
        val offenders = buildFiles.files.filter { file ->
            datadogPodRegex.containsMatchIn(file.readText())
        }
        if (offenders.isNotEmpty()) {
            val root = project.rootDir
            val formatted = offenders.joinToString(separator = "\n") { it.relativeTo(root).path }
            throw GradleException(
                "Datadog pod declarations must live only in root synthetic pods setup.\n" +
                    "Found in:\n$formatted"
            )
        }
    }
}
