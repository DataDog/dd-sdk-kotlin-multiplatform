/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateApiSurfaceTask : DefaultTask() {

    @get:InputFiles
    lateinit var sourceFiles: FileCollection

    @get: OutputFile
    lateinit var surfaceFile: File

    private lateinit var visitor: KotlinFileVisitor

    init {
        group = "datadog"
    }

    // region Task

    @TaskAction
    fun applyTask() {
        visitor = KotlinFileVisitor()
        visitFileCollectionRecursively(sourceFiles)

        val apiSurface = visitor.description.toString()
        if (apiSurface.isNotEmpty()) {
            surfaceFile.printWriter().use {
                it.print(apiSurface)
            }
        } else {
            if (surfaceFile.parentFile.exists()) {
                surfaceFile.parentFile.deleteRecursively()
            }
        }
    }

    // endregion

    private fun visitDirectoryRecursively(file: File) {
        when {
            !file.exists() -> logger.info("File $file doesn't exist, ignoring")
            file.isDirectory ->
                file.listFiles().orEmpty()
                    .sortedBy { it.absolutePath }
                    .forEach { visitDirectoryRecursively(it) }
            file.isFile -> visitFile(file)
            else -> logger.error("${file.path} is neither file nor directory")
        }
    }

    private fun visitFileCollectionRecursively(fileCollection: FileCollection) {
        for (file in fileCollection.files) {
            when {
                !file.exists() -> logger.info("File $file doesn't exist, ignoring")
                file.isDirectory ->
                    file.listFiles().orEmpty()
                        .sortedBy { it.absolutePath }
                        .forEach { visitDirectoryRecursively(it) }

                file.isFile -> visitFile(file)
                else -> logger.error("${file.path} is neither file nor directory")
            }
        }
    }

    private fun visitFile(file: File) {
        if (file.canRead()) {
            if (file.extension == EXT_KT) {
                visitor.visitFile(file)
            }
        } else {
            logger.error("${file.path} is not readable")
        }
    }

    private companion object {
        const val EXT_KT = "kt"
    }
}
