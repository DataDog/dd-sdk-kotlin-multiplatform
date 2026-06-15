/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import com.datadog.build.utils.execShell
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class GenerateJvmCompilerMetadataTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:OutputFile
    abstract val metadataInfoFile: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val compiledClassesDirectory: DirectoryProperty

    init {
        group = "datadog"
        description = "List JVM compiler metadata properties"
    }

    @TaskAction
    fun applyTask() {
        val classesDir = compiledClassesDirectory.get().asFile
        val classFile = classesDir
            .walkTopDown()
            .firstOrNull {
                val relativePath = it.relativeTo(classesDir).path
                it.extension == "class" &&
                    !it.name.contains("$") &&
                    it.path.contains("datadog") &&
                    !relativePath.contains("test", ignoreCase = true)
            }

        checkNotNull(classFile) {
            "Couldn't find any class file to get compilation metadata, did a search in $classesDir"
        }

        val metadata = CompilerMetadata.parseJvmMetadata(
            execOperations.execShell("javap", "-v", classFile.absolutePath)
        )

        metadataInfoFile.get().asFile.writeMetadata(metadata.asText())
    }

    private fun File.writeMetadata(metadata: String) {
        parentFile.mkdirs()
        writeText(metadata)
    }
}
