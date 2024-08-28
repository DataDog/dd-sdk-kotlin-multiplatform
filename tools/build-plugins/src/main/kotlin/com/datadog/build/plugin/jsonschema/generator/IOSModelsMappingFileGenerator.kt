/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema.generator

import com.datadog.build.plugin.jsonschema.JsonPrimitiveType
import com.datadog.build.plugin.jsonschema.TypeDefinition
import com.datadog.build.plugin.jsonschema.TypeProperty
import com.datadog.build.utils.ddCapitalize
import com.datadog.build.utils.toCamelCase
import com.datadog.build.utils.toCamelCaseAsVar
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import org.gradle.api.logging.Logger
import java.io.File
import java.util.Locale

internal class IOSModelsMappingFileGenerator(
    private val outputDir: File,
    commonModelsPackageName: String,
    private val iosModelsPackageName: String,
    private val iosModelsClassNamePrefix: String,
    private val typeNameRemapping: Map<String, String>,
    private val logger: Logger
) : NativeModelsMappingFileGenerator(commonModelsPackageName, mutableSetOf()) {

    override fun generate(definition: TypeDefinition, rootTypeName: String) {
        logger.info("Generating iOS mapping functions for type $definition with package name $packageName")

        if (definition is TypeDefinition.Class) {
            val fileBuilder = FileSpec.builder(packageName, "${definition.name}MappingExt")
            fileBuilder.addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
                    .addMember("%S", "ktlint")
                    .build()
            )
            fileBuilder.addImport("platform.Foundation", "NSNumber")
            emitClassConversionMethod(fileBuilder, null, null, definition, mutableSetOf())
            fileBuilder
                .build()
                .writeTo(outputDir)
        } else {
            error("Top-level type definition is not a class")
        }
    }

    private fun emitClassConversionMethod(
        fileSpecBuilder: FileSpec.Builder,
        commonRootClassName: String?,
        parentIOSTypeDefinition: IOSTypeDefinition?,
        typeDefinition: TypeDefinition.Class,
        processed: MutableSet<String>
    ) {
        val fullCommonClassName = typeDefinition.fullCommonClassName(commonRootClassName)

        val iosTypeDefinition = IOSTypeDefinition.ofClass(typeDefinition, parentIOSTypeDefinition, typeNameRemapping)
        val iosClassName = iosTypeDefinition.className

        if (processed.contains(iosClassName)) return
        processed += iosClassName

        val returnClass = ClassName.bestGuess("$packageName.$fullCommonClassName")
        val funSpecBuilder = FunSpec.builder(MODEL_CONVERSION_METHOD_NAME)
        funSpecBuilder.addModifiers(KModifier.INTERNAL, KModifier.INLINE)
            .receiver(ClassName(iosModelsPackageName, "$iosModelsClassNamePrefix$iosClassName"))
            .returns(returnClass)
            .addCode(
                CodeBlock.builder()
                    .add("return %T(", returnClass)
                    .apply {
                        add("\n")
                        indent()
                        emitClassPropertiesConversion(
                            typeDefinition,
                            iosTypeDefinition,
                            codeBlockBuilder = this,
                            funSpecBuilder,
                            commonRootClassName
                        )
                        if (typeDefinition.additionalProperties != null) {
                            // there is a type erasure for interop classes generated by commonizer, but we are
                            // sure that keys in additional properties are of type String and not null
                            addStatement(
                                "additionalProperties = ${
                                    typeDefinition.name.toCamelCase().lowercaseFirstChar()
                                }Info().mapKeys { it.key as String }"
                            )
                        }
                        unindent()
                    }
                    .add(")")
                    .build()
            )

        fileSpecBuilder.addFunction(funSpecBuilder.build())

        typeDefinition.properties.forEach {
            when (it.type) {
                is TypeDefinition.Class -> {
                    if (it.type.properties.isNotEmpty() || !it.type.isInlined) {
                        emitClassConversionMethod(
                            fileSpecBuilder,
                            commonRootClassName ?: typeDefinition.name,
                            iosTypeDefinition,
                            it.type,
                            processed
                        )
                    }
                }

                is TypeDefinition.Enum -> emitEnumConversionMethod(
                    fileSpecBuilder,
                    commonRootClassName ?: typeDefinition.name,
                    iosTypeDefinition,
                    it.type,
                    processed
                )

                is TypeDefinition.Array -> {
                    when (it.type.items) {
                        is TypeDefinition.Class -> emitClassConversionMethod(
                            fileSpecBuilder,
                            commonRootClassName ?: typeDefinition.name,
                            IOSTypeDefinition.ofArray(it.type, iosTypeDefinition),
                            it.type.items,
                            processed
                        )

                        is TypeDefinition.Enum -> emitEnumConversionMethod(
                            fileSpecBuilder,
                            commonRootClassName ?: typeDefinition.name,
                            iosTypeDefinition,
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

    private fun emitEnumConversionMethod(
        fileSpecBuilder: FileSpec.Builder,
        commonRootClassName: String?,
        parentIOSTypeDefinition: IOSTypeDefinition?,
        typeDefinition: TypeDefinition.Enum,
        processed: MutableSet<String>
    ) {
        val fullCommonEnumName = typeDefinition.fullCommonEnumName(commonRootClassName)

        val iosTypeDefinition = IOSTypeDefinition.ofEnum(typeDefinition, parentIOSTypeDefinition, typeNameRemapping)
        val iosClassName = iosTypeDefinition.className

        if (processed.contains(iosClassName)) return
        processed += iosClassName

        val returnClass = ClassName.bestGuess("$packageName.$fullCommonEnumName")
        val receiver = ClassName(iosModelsPackageName, "$iosModelsClassNamePrefix$iosClassName")
        fileSpecBuilder.addFunction(
            FunSpec.builder("${iosClassName.lowercaseFirstChar()}${ENUM_CONVERSION_METHOD_NAME.ddCapitalize()}")
                .addModifiers(KModifier.INTERNAL, KModifier.INLINE)
                .addParameter("enumValue", receiver)
                .returns(returnClass)
                .addCode(
                    CodeBlock.builder()
                        .beginControlFlow("return when(enumValue)")
                        .apply {
                            typeDefinition.values.forEach {
                                if (it == null) {
                                    addStatement("null -> null")
                                } else {
                                    addStatement(
                                        "%M -> %T.%N",
                                        MemberName(
                                            iosModelsPackageName,
                                            "$iosModelsClassNamePrefix${iosClassName}${
                                                it.asIOSEnumName(
                                                    iosTypeDefinition
                                                )
                                            }"
                                        ),
                                        returnClass,
                                        typeDefinition.enumConstantName(it)
                                    )
                                }
                            }
                            // will never hit this, because we auto-generate code, which means all the possible
                            // branches are covered. This is just needed because in ObjC API all enums are extending
                            // Int type
                            addStatement("else -> throw IllegalArgumentException(\"Unknown value ${"$"}enumValue\")")
                        }
                        .endControlFlow()
                        .build()
                )
                .build()
        )
    }

    private fun emitClassPropertiesConversion(
        typeDefinition: TypeDefinition.Class,
        iosTypeDefinition: IOSTypeDefinition,
        codeBlockBuilder: CodeBlock.Builder,
        funSpecBuilder: FunSpec.Builder,
        commonRootClassName: String?
    ) {
        typeDefinition.properties.forEach {
            val separator = if (it.optional) "?." else "."
            if (it.type is TypeDefinition.Primitive) {
                emitPrimitiveTypePropertyValueConversion(codeBlockBuilder, funSpecBuilder, it)
            } else if (it.type is TypeDefinition.Class) {
                val propertyCommonClassName = it.type.commonClassName(commonRootClassName)
                emitClassTypePropertyValueConversion(
                    codeBlockBuilder = codeBlockBuilder,
                    hostFunSpecBuilder = funSpecBuilder,
                    propertyCommonClassName,
                    property = it
                )
            } else if (it.type is TypeDefinition.Enum) {
                val enumTypeName = IOSTypeDefinition.ofEnum(it.type, iosTypeDefinition, typeNameRemapping).className
                codeBlockBuilder.addStatement(
                    "%N = ${enumTypeName.lowercaseFirstChar()}${ENUM_CONVERSION_METHOD_NAME.ddCapitalize()}(%N()),",
                    it.asPropertyName,
                    it.asPropertyName
                )
            } else if (it.type is TypeDefinition.Array) {
                if (it.type.items is TypeDefinition.Class) {
                    val classTypeName = IOSTypeDefinition.ofClass(
                        it.type.items,
                        IOSTypeDefinition.ofArray(it.type, iosTypeDefinition),
                        typeNameRemapping
                    ).className
                    codeBlockBuilder.addStatement(
                        "%N = %N()${separator}map { (it as " +
                            "$iosModelsClassNamePrefix$classTypeName).$MODEL_CONVERSION_METHOD_NAME() },",
                        it.asPropertyName,
                        it.asPropertyName
                    )
                } else if (it.type.items is TypeDefinition.Enum) {
                    val enumClassName =
                        IOSTypeDefinition.ofEnum(it.type.items, iosTypeDefinition, typeNameRemapping).className
                    codeBlockBuilder.addStatement(
                        "%N = %N()${separator}map { " +
                            "${enumClassName.lowercaseFirstChar()}${ENUM_CONVERSION_METHOD_NAME.ddCapitalize()}" +
                            "(it as $iosModelsClassNamePrefix$enumClassName) },",
                        it.asPropertyName,
                        it.asPropertyName
                    )
                } else if (it.type.items is TypeDefinition.Primitive) {
                    // special case for oneOf case in RUM. We don't have TypeDefinition.OneOfClass incoming
                    // due to simplification.
                    if (iosTypeDefinition.name == "Action" && it.name == "id") {
                        codeBlockBuilder.addStatement(
                            "%N = %N().stringsArray()?.map { it as String }.orEmpty(),",
                            it.asPropertyName,
                            it.asPropertyName
                        )
                    } else {
                        codeBlockBuilder.addStatement("%N = %N(),", it.asPropertyName, it.asPropertyName)
                    }
                }
            }
        }
    }

    private fun emitPrimitiveTypePropertyValueConversion(
        codeBlockBuilder: CodeBlock.Builder,
        hostFunSpecBuilder: FunSpec.Builder,
        property: TypeProperty
    ) {
        val separator = if (property.optional) "?." else "."
        if (property.type is TypeDefinition.Primitive) {
            when (property.type.type) {
                JsonPrimitiveType.STRING -> codeBlockBuilder.addStatement(
                    "%N = %N(),",
                    property.asPropertyName,
                    property.asPropertyName
                )

                JsonPrimitiveType.BOOLEAN -> codeBlockBuilder.addStatement(
                    "%N = %N()${separator}boolValue,",
                    property.asPropertyName,
                    property.asPropertyName
                )

                JsonPrimitiveType.DOUBLE -> codeBlockBuilder.addStatement(
                    "%N = %N()${separator}doubleValue,",
                    property.asPropertyName,
                    property.asPropertyName
                )

                JsonPrimitiveType.INTEGER -> codeBlockBuilder.addStatement(
                    "%N = %N()${separator}longValue,",
                    property.asPropertyName,
                    property.asPropertyName
                )

                JsonPrimitiveType.NUMBER -> {
                    // NSNumber can be casted to Kotlin Number
                    hostFunSpecBuilder.addSuppressAnnotation("CAST_NEVER_SUCCEEDS")
                    val castOperator = if (property.optional) "as?" else "as"
                    codeBlockBuilder.addStatement(
                        "%N = %N() $castOperator Number,",
                        property.asPropertyName,
                        property.asPropertyName
                    )
                }
            }
        }
    }

    private fun emitClassTypePropertyValueConversion(
        codeBlockBuilder: CodeBlock.Builder,
        hostFunSpecBuilder: FunSpec.Builder,
        className: ClassName?,
        property: TypeProperty
    ) {
        check(property.type is TypeDefinition.Class)

        if (property.type.isInlined) {
            val valueConversion = when ((property.type.additionalProperties as TypeDefinition.Primitive).type) {
                JsonPrimitiveType.STRING -> "it.value as String"
                JsonPrimitiveType.DOUBLE -> "(it.value as NSNumber).doubleValue"
                JsonPrimitiveType.INTEGER -> "(it.value as NSNumber).longValue"
                JsonPrimitiveType.NUMBER -> {
                    // NSNumber can be casted to Kotlin Number
                    hostFunSpecBuilder.addSuppressAnnotation("CAST_NEVER_SUCCEEDS")
                    "it.value as Number"
                }

                JsonPrimitiveType.BOOLEAN -> "(it.value as NSNumber).boolValue"
            }
            // iOS model generator doesn't generate a dedicated class if object has only additional properties
            // with a type specified and no explicit properties
            codeBlockBuilder.addStatement(
                "%N = %N()?.let { %T(additionalProperties = it.mapKeys " +
                    "{ it.key as String }.mapValues { $valueConversion }) },",
                property.asPropertyName,
                property.asPropertyName,
                className
            )
        } else {
            val separator = if (property.optional) "?." else "."
            codeBlockBuilder.addStatement(
                "%N = %N()${separator}$MODEL_CONVERSION_METHOD_NAME(),",
                property.asPropertyName,
                property.asPropertyName
            )
        }
    }

    // needed because the approach used for the model naming is different between Android and iOS
    private class IOSTypeDefinition private constructor(
        private val parent: IOSTypeDefinition?,
        val originalTypeDefinition: TypeDefinition,
        val name: String?
    ) {
        val className: String
            get() = if (parent != null) parent.className + name.orEmpty() else name.orEmpty()

        companion object {
            fun ofArray(
                arrayTypeDefinition: TypeDefinition.Array,
                parent: IOSTypeDefinition?
            ): IOSTypeDefinition =
                IOSTypeDefinition(parent, arrayTypeDefinition, null)

            fun ofEnum(
                enumTypeDefinition: TypeDefinition.Enum,
                parent: IOSTypeDefinition?,
                typeNameRemapping: Map<String, String>
            ): IOSTypeDefinition {
                return definitionFromClassOrEnum(enumTypeDefinition, parent, typeNameRemapping)
            }

            fun ofClass(
                classTypeDefinition: TypeDefinition.Class,
                parent: IOSTypeDefinition?,
                typeNameRemapping: Map<String, String>
            ): IOSTypeDefinition {
                return definitionFromClassOrEnum(classTypeDefinition, parent, typeNameRemapping)
            }

            private fun definitionFromClassOrEnum(
                typeDefinition: TypeDefinition,
                parent: IOSTypeDefinition?,
                typeNameRemapping: Map<String, String>
            ): IOSTypeDefinition {
                val name = when (typeDefinition) {
                    is TypeDefinition.Class -> typeDefinition.originalName
                    is TypeDefinition.Enum -> typeDefinition.originalName
                    else -> error("Unexpected type definition = $typeDefinition")
                }

                return name
                    .let { if (it.length <= 3) it.uppercase(Locale.US) else it }
                    .let {
                        if (it == "Type" && parent != null) {
                            "${parent.name}Type"
                        } else {
                            it
                        }
                    }
                    .let {
                        typeNameRemapping.getOrDefault(it, it)
                    }
                    .let {
                        if (parent?.originalTypeDefinition is TypeDefinition.Array) "${it}s" else it
                    }
                    .let {
                        if (parent?.parent?.name == "Action" && it == "Id") "RUMActionID" else it
                    }
                    .let {
                        if (parent?.className == "ErrorEventError" && it == "Meta") "MetaInfo" else it
                    }
                    .let {
                        IOSTypeDefinition(parent, typeDefinition, it)
                    }
            }
        }
    }

    private fun TypeDefinition.Class.commonClassName(commonRootClassName: String?): ClassName {
        return if (commonRootClassName != null) {
            withUniqueTypeName(commonRootClassName).typeName as ClassName
        } else {
            ClassName.bestGuess(
                "$packageName.$name"
            )
        }
    }

    // TODO RUM-5993 iOS model generator doesn't generate a dedicated class if object has only additional properties
    // with a type specified and no explicit properties. This behavior should be changed.
    private inline val TypeDefinition.Class.isInlined
        get() = properties.isEmpty() && additionalProperties is TypeDefinition.Primitive

    private fun FunSpec.Builder.addSuppressAnnotation(ruleName: String) {
        val alreadyExists = annotations.any {
            it.typeName.toString() == "kotlin.Suppress" && it.members.any {
                it.toString().contains(ruleName)
            }
        }
        if (alreadyExists) return

        addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", ruleName).build())
    }

    private val TypeProperty.asPropertyName
        get() = name.trimStart { it == '_' }.toCamelCaseAsVar()

    private fun String.asIOSEnumName(parentDefinition: IOSTypeDefinition): String {
        return replace(Regex("[-+ ]"), "_")
            .toCamelCase()
            .run {
                if (first().isDigit()) "${parentDefinition.name}$this" else this
            }
            .run {
                if (parentDefinition.name == "RUMMethod") lowercase(Locale.US).ddCapitalize() else this
            }
    }

    private fun String.lowercaseFirstChar() = replaceFirstChar { it.lowercase(Locale.US) }
}
