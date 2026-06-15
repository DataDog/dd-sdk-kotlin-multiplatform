/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateKlibCompilerMetadataTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val compiledKlibDirectory: DirectoryProperty

    @get:OutputFile
    abstract val metadataInfoFile: RegularFileProperty

    init {
        group = "datadog"
        description = "List KLIB compiler metadata properties"
    }

    @TaskAction
    fun applyTask() {
        val klibDirectory = compiledKlibDirectory.get().asFile
        val manifestFile = klibDirectory
            .walkTopDown()
            .firstOrNull { it.isFile && it.name == KLIB_MANIFEST_FILE_NAME }

        checkNotNull(manifestFile) {
            "Couldn't find any KLIB manifest to get compilation metadata, did a search in $klibDirectory"
        }

        val metadata = CompilerMetadata.parseKlibMetadata(manifestFile.readText())
        metadataInfoFile.get().asFile.writeMetadata(metadata.asText())
    }

    private fun File.writeMetadata(metadata: String) {
        parentFile.mkdirs()
        writeText(metadata)
    }

    private companion object {
        const val KLIB_MANIFEST_FILE_NAME = "manifest"
    }
}
