/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema

import java.io.File

data class JsonDefinitionReference(
    val typeName: String,
    val definition: JsonDefinition,
    val fromFile: File
)
