/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

internal object InternalAttributes {
    private const val DD_SOURCE_KEY = "_dd.source"
    private const val DD_SDK_VERSION_KEY = "_dd.sdk_version"

    val SOURCE_ATTRIBUTE = DD_SOURCE_KEY to "kotlin-multiplatform"
    val SDK_VERSION_ATTRIBUTE = DD_SDK_VERSION_KEY to LibraryConfig.SDK_VERSION
}
