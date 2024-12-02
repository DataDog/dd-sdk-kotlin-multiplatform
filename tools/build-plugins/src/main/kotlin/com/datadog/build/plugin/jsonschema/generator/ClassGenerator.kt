/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema.generator

import com.datadog.build.plugin.jsonschema.JsonPrimitiveType
import com.datadog.build.plugin.jsonschema.JsonType
import com.datadog.build.plugin.jsonschema.TypeDefinition
import com.datadog.build.plugin.jsonschema.TypeProperty
import com.datadog.build.plugin.jsonschema.variableName
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

class ClassGenerator(
    packageName: String,
    knownTypes: MutableSet<KotlinTypeWrapper>
) : TypeSpecGenerator<TypeDefinition.Class>(
    packageName,
    knownTypes
) {

    // region TypeSpecGenerator

    override fun generate(
        definition: TypeDefinition.Class,
        rootTypeName: String
    ): TypeSpec.Builder {
        val typeBuilder = TypeSpec.classBuilder(definition.name)

        if (definition.parentType != null) {
            typeBuilder.superclass(definition.parentType.asKotlinTypeName(rootTypeName))
        }

        if (
            definition.properties.any { it.type !is TypeDefinition.Constant } ||
            definition.additionalProperties != null
        ) {
            typeBuilder.addModifiers(KModifier.DATA)
        }

        typeBuilder.addKdoc(generateKDoc(definition))

        definition.properties.forEach {
            typeBuilder.addProperty(generateProperty(it, rootTypeName))
        }
        if (definition.additionalProperties != null) {
            typeBuilder.addProperty(
                generateAdditionalProperties(
                    definition.additionalProperties,
                    definition.readOnlyAdditionalProperties,
                    rootTypeName
                )
            )
        }

        if (
            definition.properties.any { it.type !is TypeDefinition.Constant } ||
            definition.additionalProperties != null
        ) {
            typeBuilder.primaryConstructor(generateConstructor(definition, rootTypeName))
        }

        return typeBuilder
    }

    // endregion

    // region Internal

    private fun generateKDoc(definition: TypeDefinition.Class): CodeBlock {
        val docBuilder = CodeBlock.builder()

        if (definition.description.isNotBlank()) {
            docBuilder.add(definition.description)
            docBuilder.add("\n")
        }

        definition.properties.forEach { p ->
            if (p.type !is TypeDefinition.Constant && p.type.description.isNotBlank()) {
                docBuilder.add("@param ${p.name.variableName()} ${p.type.description}\n")
            }
        }

        if (
            definition.additionalProperties != null &&
            definition.additionalProperties.description.isNotBlank()
        ) {
            docBuilder.add(
                "@param ${Identifier.PARAM_ADDITIONAL_PROPS} ${definition.additionalProperties.description}\n"
            )
        }
        return docBuilder.build()
    }

    private fun generateConstructor(
        definition: TypeDefinition.Class,
        rootTypeName: String
    ): FunSpec {
        val constructorBuilder = FunSpec.constructorBuilder()

        definition.properties.forEach { p ->
            if (p.type !is TypeDefinition.Constant) {
                val propertyName = p.name.variableName()
                val isNullable = (p.optional || p.type is TypeDefinition.Null)
                val notNullableType = p.type.asKotlinTypeName(rootTypeName)
                val propertyType = notNullableType.copy(nullable = isNullable)
                constructorBuilder.addParameter(
                    ParameterSpec.builder(propertyName, propertyType)
                        .withDefaultValue(p, rootTypeName)
                        .build()
                )
            }
        }

        if (definition.additionalProperties != null) {
            val mapType = definition.additionalProperties.additionalPropertyType(
                definition.readOnlyAdditionalProperties,
                rootTypeName
            )
            constructorBuilder.addParameter(
                ParameterSpec.builder(Identifier.PARAM_ADDITIONAL_PROPS, mapType)
                    .defaultValue(if (definition.readOnlyAdditionalProperties) "mapOf()" else "mutableMapOf()")
                    .build()
            )
        }

        return constructorBuilder.build()
    }

    private fun generateProperty(property: TypeProperty, rootTypeName: String): PropertySpec {
        val propertyName = property.name.variableName()
        val propertyType = property.type
        val isNullable = (property.optional || propertyType is TypeDefinition.Null) &&
            (propertyType !is TypeDefinition.Constant)
        val notNullableType = propertyType.asKotlinTypeName(rootTypeName)
        val type = notNullableType.copy(nullable = isNullable)
        val initializer = if (propertyType is TypeDefinition.Constant) {
            getKotlinValue(propertyType.value, propertyType.type)
        } else {
            propertyName
        }

        return PropertySpec.builder(propertyName, type)
            .mutable(!property.readOnly)
            .initializer(initializer)
            .build()
    }

    private fun generateAdditionalProperties(
        additionalPropertyType: TypeDefinition,
        readOnly: Boolean,
        rootTypeName: String
    ): PropertySpec {
        val type = additionalPropertyType.additionalPropertyType(readOnly, rootTypeName)

        return PropertySpec.builder(Identifier.PARAM_ADDITIONAL_PROPS, type)
            .mutable(false)
            .initializer(Identifier.PARAM_ADDITIONAL_PROPS)
            .build()
    }

    private fun getKotlinValue(
        value: Any?,
        type: Any?
    ): String {
        return when {
            value is JsonPrimitiveType -> {
                error(
                    "Unable to get Kotlin Value from $value with type $type"
                )
            }

            value is String -> "\"$value\""
            value is Double &&
                (type == JsonType.INTEGER || type == JsonPrimitiveType.INTEGER) -> {
                "${value.toLong()}L"
            }

            value is Double -> {
                "$value"
            }

            value is Boolean -> {
                "$value"
            }

            else -> error("Unable to get Kotlin Value from $value with type $type")
        }
    }

    // endregion

    // region Internal Extensions

    private fun ParameterSpec.Builder.withDefaultValue(
        p: TypeProperty,
        rootTypeName: String
    ): ParameterSpec.Builder {
        val defaultValue = p.defaultValue
        if (defaultValue != null) {
            when (p.type) {
                is TypeDefinition.Primitive -> defaultValue(
                    getKotlinValue(
                        defaultValue,
                        p.type.type
                    )
                )

                is TypeDefinition.Enum -> defaultValue(
                    "%T.%L",
                    p.type.asKotlinTypeName(rootTypeName),
                    p.type.enumConstantName(
                        if (defaultValue is Number) {
                            defaultValue.toInt().toString()
                        } else {
                            defaultValue.toString()
                        }
                    )
                )

                else -> throw IllegalArgumentException(
                    "Unable to generate default value for class: ${p.type}. " +
                        "This feature is not supported yet"
                )
            }
        } else if (p.optional || p.type is TypeDefinition.Null) {
            defaultValue("null")
        }
        return this
    }

    private fun TypeDefinition.additionalPropertyType(readOnly: Boolean, rootTypeName: String): TypeName {
        val valueType = if (this is TypeDefinition.Primitive) {
            this.asKotlinTypeName(rootTypeName)
        } else {
            ANY.copy(nullable = true)
        }
        val mapType = if (readOnly) MAP else MUTABLE_MAP
        return mapType.parameterizedBy(STRING, valueType)
    }

    // endregion
}
