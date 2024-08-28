/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

abstract class GenerateJsonSchemaExtension @Inject constructor(
    private val objectFactory: ObjectFactory
) {
    internal val jsonSchemas = mutableMapOf<String, JsonSchema>()

    fun schema(name: String, configure: JsonSchema.() -> Unit) {
        val schema = objectFactory.newInstance<JsonSchema>()
        schema.configure()
        jsonSchemas += name to schema
    }
}

abstract class JsonSchema @Inject constructor(
    private val objectFactory: ObjectFactory
) {
    abstract val location: Property<SchemaLocation>

    /**
     * The package name where to generate the models based on the schema files.
     * (E.g.: `com.example.model`).
     */
    abstract val targetPackageName: Property<String>

    /**
     * The list of schema files to be ignored.
     */
    abstract val ignoredFiles: ListProperty<String>

    /**
     * The mapping of the schema file to the generated model name. Mostly used for merged
     * schemas.
     */
    abstract val inputNameMapping: MapProperty<String, String>

    internal val androidModelsMappingGeneration = objectFactory.newInstance<AndroidModelsMappingGeneration>()
    internal val iosModelsMappingGeneration = objectFactory.newInstance<IOSModelsMappingGeneration>()

    fun androidModelsMappingGeneration(configure: AndroidModelsMappingGeneration.() -> Unit) {
        androidModelsMappingGeneration.configure()
    }

    fun iosModelsMappingGeneration(configure: IOSModelsMappingGeneration.() -> Unit) {
        iosModelsMappingGeneration.configure()
    }
}

interface AndroidModelsMappingGeneration {
    val enabled: Property<Boolean>
    val androidModelsPackageName: Property<String>
}

interface IOSModelsMappingGeneration {
    val enabled: Property<Boolean>
    val iosModelsPackageName: Property<String>
    val iosModelsClassNamePrefix: Property<String>
    val typeNameRemapping: MapProperty<String, String>
}

sealed class SchemaLocation {
    data class Git(
        internal val repo: String,
        internal val subFolder: String = "",
        internal val ref: String = "master",
        internal val excludedPrefixes: List<String> = emptyList(),
        internal val destinationFolder: String = ""
    ) : SchemaLocation()

    data class Local(
        internal val path: String
    ) : SchemaLocation()
}
