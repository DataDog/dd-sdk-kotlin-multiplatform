/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.transdeps

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault
open class TransitiveDependenciesTask : DefaultTask() {

    @get:Input
    var humanReadableSize: Boolean = true

    @get:Input
    var sortByName: Boolean = true

    @get: OutputFiles
    var outputFiles: List<File> = mutableListOf()

    init {
        group = "datadog"
        description = "Generate the list of transitive dependencies of the library"
        outputs.upToDateWhen { false }
    }

    // region Task

    @TaskAction
    fun applyTask() {
        SUPPORTED_CONFIGURATIONS.forEach { (configurationName, target) ->
            val configuration = project.configurations.getByName(configurationName)
            val outputFile = File(project.projectDir, "$target-transitiveDependencies")
            outputFile.writeText("Dependencies List\n\n")
            listConfigurationDependencies(outputFile, configuration)
            outputFiles += outputFile
        }
    }

    // endregion

    // region Internal

    private fun listConfigurationDependencies(file: File, configuration: Configuration) {
        check(configuration.isCanBeResolved) { "$configuration cannot be resolved" }

        val sortedArtifacts = if (sortByName) {
            configuration.files {
                // ProjectDependency (i.e. local modules) don't have a file associated
                it !is ProjectDependency
            }.sortedBy { it.absolutePath }
        } else {
            configuration.sortedBy { -it.length() }
        }

        var sum = 0L
        sortedArtifacts.forEach {
            sum += it.length()
            file.appendText(getDependencyFileDescription(it))
        }

        file.appendText("\n${TOTAL.padEnd(PADDING)}:${size(sum)}\n\n")
    }

    private fun getDependencyFileDescription(it: File): String {
        val hash = it.parentFile
        val version = hash.parentFile
        val artifact = version.parentFile
        val group = artifact.parentFile

        val title = "${group.name}:${artifact.name}:${version.name}"

        return "${title.padEnd(PADDING)}:${size(it.length())}\n"
    }

    private fun size(size: Long): String {
        if (humanReadableSize) {
            val rawSize = when {
                size >= 2 * MB -> "${size / MB} Mb"
                size >= 2 * KB -> "${size / KB} Kb"
                else -> "$size b "
            }
            return rawSize.padStart(8)
        } else {
            return "$size b ".padStart(16)
        }
    }

    // endregion

    companion object {
        private const val PADDING = 64
        private const val KB = 1024
        private const val MB = 1024 * 1024

        private val SUPPORTED_CONFIGURATIONS = mapOf(
            "androidReleaseCompileClasspath" to "android"
        )
        private const val TOTAL = "Total transitive dependencies size"
    }
}
