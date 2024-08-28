/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema.generator

import com.datadog.build.plugin.jsonschema.TypeDefinition
import com.datadog.build.plugin.jsonschema.TypeProperty
import com.datadog.build.utils.toCamelCaseAsVar
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import org.gradle.api.logging.Logger
import java.io.File

internal class AndroidModelsMappingFileGenerator(
    private val outputDir: File,
    commonModelsPackageName: String,
    private val androidModelsPackageName: String,
    private val logger: Logger
) : NativeModelsMappingFileGenerator(commonModelsPackageName, mutableSetOf()) {

    override fun generate(definition: TypeDefinition, rootTypeName: String) {
        logger.info("Generating Android mapping functions for type $definition with package name $packageName")

        if (definition is TypeDefinition.Class) {
            val fileBuilder = FileSpec.builder(packageName, "${definition.name}MappingExt")
            fileBuilder.addAnnotation(
                // we are making generated functions inline in order not to have performance benefit, but
                // to reduce DEX method count
                AnnotationSpec.builder(Suppress::class)
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
                    .addMember("%S", "NOTHING_TO_INLINE")
                    .addMember("%S", "ktlint")
                    .build()
            )
            emitClassConversionMethod(fileBuilder, null, definition, mutableSetOf())
            fileBuilder.build().writeTo(outputDir)
        } else {
            error("Top-level type definition is not a class")
        }
    }

    private fun emitClassConversionMethod(
        fileSpecBuilder: FileSpec.Builder,
        rootClassName: String?,
        typeDefinition: TypeDefinition.Class,
        processed: MutableSet<String>
    ) {
        val fullCommonClassName = typeDefinition.fullCommonClassName(rootClassName)

        if (processed.contains(fullCommonClassName)) return
        processed += fullCommonClassName

        val returnClass = ClassName.bestGuess("$packageName.$fullCommonClassName")
        fileSpecBuilder.addFunction(
            FunSpec.builder(MODEL_CONVERSION_METHOD_NAME)
                .addModifiers(KModifier.INTERNAL, KModifier.INLINE)
                .receiver(ClassName.bestGuess("$androidModelsPackageName.$fullCommonClassName"))
                .returns(returnClass)
                .addCode(
                    CodeBlock.builder()
                        .add("return %T(", returnClass)
                        .apply {
                            add("\n")
                            indent()
                            emitClassPropertiesConversion(typeDefinition, this)
                            if (typeDefinition.additionalProperties != null) {
                                addStatement("additionalProperties = additionalProperties")
                            }
                            unindent()
                        }
                        .add(")")
                        .build()
                )
                .build()
        )

        typeDefinition.properties.forEach {
            when (it.type) {
                is TypeDefinition.Class -> emitClassConversionMethod(
                    fileSpecBuilder,
                    rootClassName ?: typeDefinition.name,
                    it.type,
                    processed
                )

                is TypeDefinition.Enum -> emitEnumConversionMethod(
                    fileSpecBuilder,
                    rootClassName ?: typeDefinition.name,
                    it.type,
                    processed
                )

                is TypeDefinition.Array -> {
                    when (it.type.items) {
                        is TypeDefinition.Class -> emitClassConversionMethod(
                            fileSpecBuilder,
                            rootClassName ?: typeDefinition.name,
                            it.type.items,
                            processed
                        )

                        is TypeDefinition.Enum -> emitEnumConversionMethod(
                            fileSpecBuilder,
                            rootClassName ?: typeDefinition.name,
                            it.type.items,
                            processed
                        )

                        is TypeDefinition.Primitive, is TypeDefinition.Constant -> {
                            // nothing to generate, skip it
                        }

                        else -> {
                            error("Unsupported type ${it.type.items} inside array")
                        }
                    }
                }

                is TypeDefinition.Primitive, is TypeDefinition.Constant -> {
                    // nothing to generate, skip it
                }

                else -> {
                    error("Unsupported property type ${it.type}")
                }
            }
        }
    }

    private fun emitClassPropertiesConversion(
        typeDefinition: TypeDefinition.Class,
        codeBlockBuilder: CodeBlock.Builder
    ) {
        typeDefinition.properties.forEach {
            val separator = if (it.optional) "?." else "."
            if (it.type is TypeDefinition.Primitive) {
                codeBlockBuilder.addStatement("%N = %N,", it.asPropertyName, it.asPropertyName)
            } else if (it.type is TypeDefinition.Class) {
                codeBlockBuilder.addStatement(
                    "%N = %N${separator}$MODEL_CONVERSION_METHOD_NAME(),",
                    it.asPropertyName,
                    it.asPropertyName
                )
            } else if (it.type is TypeDefinition.Enum) {
                codeBlockBuilder.addStatement(
                    "%N = %N${separator}$ENUM_CONVERSION_METHOD_NAME(),",
                    it.asPropertyName,
                    it.asPropertyName
                )
            } else if (it.type is TypeDefinition.Array) {
                if (it.type.items is TypeDefinition.Class) {
                    codeBlockBuilder.addStatement(
                        "%N = %N${separator}map { it.$MODEL_CONVERSION_METHOD_NAME() },",
                        it.asPropertyName,
                        it.asPropertyName
                    )
                } else if (it.type.items is TypeDefinition.Enum) {
                    codeBlockBuilder.addStatement(
                        "%N = %N${separator}map { it.$ENUM_CONVERSION_METHOD_NAME() },",
                        it.asPropertyName,
                        it.asPropertyName
                    )
                } else if (it.type.items is TypeDefinition.Primitive) {
                    codeBlockBuilder.addStatement("%N = %N,", it.asPropertyName, it.asPropertyName)
                }
            }
        }
    }

    private fun emitEnumConversionMethod(
        fileSpecBuilder: FileSpec.Builder,
        rootClassName: String?,
        typeDefinition: TypeDefinition.Enum,
        processed: MutableSet<String>
    ) {
        val fullCommonEnumName = typeDefinition.fullCommonEnumName(rootClassName)

        if (processed.contains(fullCommonEnumName)) return
        processed += fullCommonEnumName

        val returnClass = ClassName.bestGuess("$packageName.$fullCommonEnumName")
        val receiver = ClassName.bestGuess("$androidModelsPackageName.$fullCommonEnumName")
        fileSpecBuilder.addFunction(
            FunSpec.builder(ENUM_CONVERSION_METHOD_NAME)
                .addModifiers(KModifier.INTERNAL, KModifier.INLINE)
                .receiver(receiver)
                .returns(returnClass)
                .addCode(
                    CodeBlock.builder()
                        .beginControlFlow("return when(this)")
                        .apply {
                            typeDefinition.values.forEach {
                                if (it == null) {
                                    addStatement("null -> null")
                                } else {
                                    addStatement(
                                        "%T.%N -> %T.%N",
                                        receiver,
                                        typeDefinition.enumConstantName(it),
                                        returnClass,
                                        typeDefinition.enumConstantName(it)
                                    )
                                }
                            }
                        }
                        .endControlFlow()
                        .build()
                )
                .build()
        )
    }

    private val TypeProperty.asPropertyName
        get() = name.trimStart { it == '_' }.toCamelCaseAsVar()
}
