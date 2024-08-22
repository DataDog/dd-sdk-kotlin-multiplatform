/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema.generator

import com.datadog.build.plugin.jsonschema.TypeDefinition
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

class MultiClassGenerator(
    val classGenerator: KotlinSpecGenerator<TypeDefinition.Class, TypeSpec.Builder>,
    packageName: String,
    knownTypes: MutableSet<KotlinTypeWrapper>
) : TypeSpecGenerator<TypeDefinition.OneOfClass>(
    packageName,
    knownTypes
) {

    //region TypeSpecGenerator

    override fun generate(
        definition: TypeDefinition.OneOfClass,
        rootTypeName: String
    ): TypeSpec.Builder {
        val typeBuilder = TypeSpec.classBuilder(definition.name)
            .addModifiers(KModifier.SEALED)

        if (definition.description.isNotBlank()) {
            val docBuilder = CodeBlock.builder()
            docBuilder.add(definition.description)
            docBuilder.add("\n")
            typeBuilder.addKdoc(docBuilder.build())
        }

        definition.options.forEach {
            when (it) {
                is TypeDefinition.Class -> {
                    val childType = it.copy(parentType = definition)
                    val wrapper = childType.withUniqueTypeName(rootTypeName)
                    typeBuilder.addType(
                        classGenerator.generate(childType, rootTypeName).build()
                    )
                    wrapper.written = true
                }
                else -> error(
                    "Can't have type $it as child of a `one_of` block"
                )
            }
        }

        return typeBuilder
    }

    // endregion
}
