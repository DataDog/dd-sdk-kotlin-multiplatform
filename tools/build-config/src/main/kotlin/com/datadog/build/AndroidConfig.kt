/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build

import com.datadog.build.utils.Version

object AndroidConfig {
    const val MIN_SDK = 21
    const val COMPILE_SDK = 34
    const val BUILD_TOOLS_VERSION = "34.0.0"

    val VERSION = Version(0, 0, 1, Version.Type.Dev)
}