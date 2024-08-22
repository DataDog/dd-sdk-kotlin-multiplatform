/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema.generator

import com.datadog.build.plugin.jsonschema.JsonType
import com.datadog.build.plugin.jsonschema.TypeDefinition
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

class EnumClassGenerator(
    packageName: String,
    knownTypes: MutableSet<KotlinTypeWrapper>
) : TypeSpecGenerator<TypeDefinition.Enum>(
    packageName,
    knownTypes
) {

    // region TypeSpecGenerator

    override fun generate(
        definition: TypeDefinition.Enum,
        rootTypeName: String
    ): TypeSpec.Builder {
        val enumBuilder = TypeSpec.enumBuilder(definition.name)

        val jsonValueType = definition.jsonValueType()
        val typeName = jsonValueType.asTypeName().copy(nullable = definition.allowsNull())

        enumBuilder.addKdoc(generateKDoc(definition))

        enumBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(
                    Identifier.PARAM_JSON_VALUE,
                    typeName
                )
                .build()
        )

        enumBuilder.addProperty(
            PropertySpec.builder(Identifier.PARAM_JSON_VALUE, typeName, KModifier.PRIVATE)
                .initializer(Identifier.PARAM_JSON_VALUE)
                .build()
        )

        val parameterFormat = if (definition.type == JsonType.NUMBER) "%L" else "%S"
        definition.values.forEach { value ->
            val enumValue = if (value == null) {
                TypeSpec.anonymousClassBuilder()
                    .addSuperclassConstructorParameter("null")
                    .build()
            } else {
                TypeSpec.anonymousClassBuilder()
                    .addSuperclassConstructorParameter(parameterFormat, value)
                    .build()
            }
            enumBuilder.addEnumConstant(definition.enumConstantName(value), enumValue)
        }

        return enumBuilder
    }

    // endregion

    // region Internal

    private fun generateKDoc(definition: TypeDefinition.Enum): CodeBlock {
        val docBuilder = CodeBlock.builder()

        if (definition.description.isNotBlank()) {
            docBuilder.add(definition.description)
            docBuilder.add("\n")
        }
        return docBuilder.build()
    }

    // endregion
}
