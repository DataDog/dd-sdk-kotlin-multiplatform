/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema

import com.google.gson.annotations.SerializedName

enum class JsonType {
    @SerializedName("null")
    NULL,

    @SerializedName("boolean")
    BOOLEAN,

    @SerializedName("object")
    OBJECT,

    @SerializedName("array")
    ARRAY,

    @SerializedName("number")
    NUMBER,

    @SerializedName("string")
    STRING,

    @SerializedName("integer")
    INTEGER;

    fun asJsonPrimitiveType(): JsonPrimitiveType? {
        return when (this) {
            BOOLEAN -> JsonPrimitiveType.BOOLEAN
            NUMBER -> JsonPrimitiveType.NUMBER
            STRING -> JsonPrimitiveType.STRING
            INTEGER -> JsonPrimitiveType.INTEGER
            ARRAY,
            OBJECT,
            NULL -> null
        }
    }
}
