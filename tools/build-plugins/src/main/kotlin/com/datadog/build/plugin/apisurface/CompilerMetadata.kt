/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.apisurface

import java.util.Properties

internal object CompilerMetadata {

    fun parseJvmMetadata(javapOutput: List<String>): JvmMetadata {
        val kotlinAbiVersion = javapOutput
            .map { it.trim() }
            .first { it.startsWith("mv=[") }
            .removePrefix("mv=[")
            .removeSuffix("]")
            .replace(",", ".")

        val jvmBytecodeVersion = javapOutput
            .map { it.trim() }
            .first { it.startsWith("major version: ") }
            .removePrefix("major version: ")
            .toInt()
            .toJvmVersion()

        return JvmMetadata(
            kotlinAbiVersion = kotlinAbiVersion,
            jvmBytecodeVersion = jvmBytecodeVersion
        )
    }

    fun parseKlibMetadata(manifestText: String): KlibMetadata {
        val manifest = Properties()
        manifestText.byteInputStream().use { manifest.load(it) }

        return KlibMetadata(
            kotlinKlibAbiVersion = checkNotNull(manifest.getProperty("abi_version")) {
                "Couldn't find abi_version in the KLIB manifest."
            },
            kotlinMetadataVersion = checkNotNull(manifest.getProperty("metadata_version")) {
                "Couldn't find metadata_version in the KLIB manifest."
            },
            kotlinCompilerVersion = checkNotNull(manifest.getProperty("compiler_version")) {
                "Couldn't find compiler_version in the KLIB manifest."
            }
        )
    }

    private fun Int.toJvmVersion(): Int {
        // Java 25
        require(this <= 69) { "Unsupported JVM major version value: $this" }
        // At least between Java 25 and Java 5 this formula is true.
        return this - 44
    }
}

internal data class JvmMetadata(
    val kotlinAbiVersion: String,
    val jvmBytecodeVersion: Int
) {
    fun asText(): String = "kotlin_abi_version=$kotlinAbiVersion\n" +
        "jvm_bytecode_version=$jvmBytecodeVersion\n"
}

internal data class KlibMetadata(
    val kotlinKlibAbiVersion: String,
    val kotlinMetadataVersion: String,
    val kotlinCompilerVersion: String
) {
    fun asText(): String = "kotlin_klib_abi_version=$kotlinKlibAbiVersion\n" +
        "kotlin_metadata_version=$kotlinMetadataVersion\n" +
        "kotlin_compiler_version=$kotlinCompilerVersion\n"
}
