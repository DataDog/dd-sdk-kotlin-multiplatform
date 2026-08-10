/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CompilerMetadataTest {

    @Test
    fun `M parse JVM compiler metadata W javap output`() {
        // Given
        val javapOutput = listOf(
            "Classfile /tmp/com/datadog/kmp/Datadog.class",
            "  major version: 61",
            "  RuntimeVisibleAnnotations:",
            "    kotlin.Metadata(",
            "      mv=[2,2,0]"
        )

        // When
        val metadata = CompilerMetadata.parseJvmMetadata(javapOutput)

        // Then
        assertEquals("2.2.0", metadata.kotlinAbiVersion)
        assertEquals(17, metadata.jvmBytecodeVersion)
        assertEquals(
            "kotlin_abi_version=2.2.0\n" +
                "jvm_bytecode_version=17\n",
            metadata.asText()
        )
    }

    @Test
    fun `M parse KLIB compiler metadata W manifest`() {
        // Given
        val manifest = """
            |abi_version=2.2.0
            |builtins_platform=NATIVE
            |compiler_version=2.2.21
            |metadata_version=1.4.1
            |native_targets=ios_simulator_arm64
        """.trimMargin()

        // When
        val metadata = CompilerMetadata.parseKlibMetadata(manifest)

        // Then
        assertEquals("2.2.0", metadata.kotlinKlibAbiVersion)
        assertEquals("1.4.1", metadata.kotlinMetadataVersion)
        assertEquals("2.2.21", metadata.kotlinCompilerVersion)
        assertEquals(
            "kotlin_klib_abi_version=2.2.0\n" +
                "kotlin_metadata_version=1.4.1\n" +
                "kotlin_compiler_version=2.2.21\n",
            metadata.asText()
        )
    }
}
