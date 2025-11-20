/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build.plugin.jsonschema.generator

import com.datadog.build.plugin.jsonschema.TypeDefinition
import com.squareup.kotlinpoet.ClassName

abstract class NativeModelsMappingFileGenerator(
    packageName: String,
    knownTypes: MutableSet<KotlinTypeWrapper>
) : KotlinSpecGenerator<TypeDefinition, Unit>(
    packageName,
    knownTypes
) {

    fun generate(rootTypeDefinition: TypeDefinition) = generate(rootTypeDefinition, "")

    // return full class name with parent classes names taken into account, without package name, e.g.: ViewEvent.Action
    protected fun TypeDefinition.Class.fullCommonClassName(rootClassName: String?): String {
        return if (rootClassName != null) {
            (withUniqueTypeName(rootClassName).typeName as ClassName).simpleNames.joinToString(".")
        } else {
            name
        }
    }

    // return full enum name with parent classes names taken into account, without package
    // name, e.g.: ViewEvent.SourceType
    protected fun TypeDefinition.Enum.fullCommonEnumName(rootClassName: String?): String {
        return if (rootClassName != null) {
            (asKotlinTypeName(rootClassName) as ClassName).simpleNames.joinToString(".")
        } else {
            name
        }
    }

    protected companion object {
        const val MODEL_CONVERSION_METHOD_NAME = "toCommonModel"
        const val ENUM_CONVERSION_METHOD_NAME = "toCommonEnum"
    }
}
