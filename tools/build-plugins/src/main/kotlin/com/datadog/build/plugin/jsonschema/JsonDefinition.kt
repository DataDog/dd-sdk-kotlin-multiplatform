/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema

import com.google.gson.annotations.SerializedName

data class JsonDefinition(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: JsonType?,
    @SerializedName("enum") val enum: List<String?>?,
    @SerializedName("const") val constant: Any?,
    @SerializedName("\$ref") val ref: String?,
    @SerializedName("\$id") val id: String?,
    @SerializedName("required") val required: List<String>?,
    @SerializedName("uniqueItems") val uniqueItems: Boolean?,
    @SerializedName("items") val items: JsonDefinition?,
    @SerializedName("allOf") val allOf: List<JsonDefinition>?,
    @SerializedName("oneOf") val oneOf: List<JsonDefinition>?,
    @SerializedName("anyOf") val anyOf: List<JsonDefinition>?,
    @SerializedName("properties") val properties: Map<String, JsonDefinition>?,
    @SerializedName("definitions") val definitions: Map<String, JsonDefinition>?,
    @SerializedName("readOnly") val readOnly: Boolean?,
    @SerializedName("additionalProperties") val additionalProperties: Any?,
    @SerializedName("default") val default: Any?
) {

    companion object {
        val EMPTY = JsonDefinition(
            title = null,
            description = null,
            type = null,
            enum = null,
            constant = null,
            ref = null,
            id = null,
            required = null,
            uniqueItems = null,
            items = null,
            allOf = null,
            oneOf = null,
            anyOf = null,
            properties = null,
            definitions = null,
            readOnly = null,
            additionalProperties = null,
            default = null
        )

        val ANY = JsonDefinition(
            title = null,
            description = null,
            type = JsonType.OBJECT,
            enum = null,
            constant = null,
            ref = null,
            id = null,
            required = null,
            uniqueItems = null,
            items = null,
            allOf = null,
            oneOf = null,
            anyOf = null,
            properties = null,
            definitions = null,
            readOnly = null,
            additionalProperties = null,
            default = null
        )
    }
}
