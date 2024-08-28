/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema.generator

import com.datadog.build.plugin.jsonschema.JsonSchemaReader
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.pathString

@Extensions(
    ExtendWith(MockitoExtension::class)
)
abstract class NativeModelsMappingFileGeneratorTest {

    @TempDir
    lateinit var tempDir: File

    @Mock
    lateinit var mockLogger: Logger

    abstract fun provideGenerator(): NativeModelsMappingFileGenerator

    abstract fun expectedFileName(): String

    @Test
    fun `M generate mapping W generate()`() {
        // Given
        val generator = provideGenerator()

        val rootTypeDefinition = JsonSchemaReader(mapOf("view-schema.json" to "ViewEvent"), mockLogger).let {
            val schemaFile = File(
                checkNotNull(it::class.java.getResource("/view-schema.json")).file
            )
            it.readSchema(schemaFile)
        }

        // When
        generator.generate(rootTypeDefinition)

        // Then
        val generatedFile = File(
            tempDir,
            Paths.get(
                "",
                *COMMON_MODELS_PACKAGE_NAME.split(".").toTypedArray(),
                "ViewEventMappingExt.kt"
            ).pathString
        )
        val generatedContent = generatedFile.readText()

        val expectedFile = File(
            checkNotNull(this::class.java.getResource("/${expectedFileName()}")).file
        )
        val expectedContent = expectedFile.readText()

        if (generatedContent != expectedContent) {
            val genLines = generatedContent.lines()
            val expLines = expectedContent.lines()
            for (i in 0 until minOf(genLines.size, expLines.size)) {
                if (genLines[i] != expLines[i]) {
                    System.err.println("--- GENERATED $generatedFile.kt \n")
                    throw AssertionError(
                        "File $generatedFile generated from \n$rootTypeDefinition didn't match expectation:\n" +
                            "First error on line ${i + 1}:\n" +
                            "<<<<<<< EXPECTED\n" +
                            expLines[i] +
                            "\n=======\n" +
                            genLines[i] +
                            "\n>>>>>>> GENERATED\n"
                    )
                }
            }
        }
    }

    companion object {
        const val COMMON_MODELS_PACKAGE_NAME = "com.datadog.kmp.rum.model"
    }
}
