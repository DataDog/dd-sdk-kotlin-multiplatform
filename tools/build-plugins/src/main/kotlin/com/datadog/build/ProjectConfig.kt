/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.build

import com.datadog.build.utils.Version

object ProjectConfig {
    object Android {
        const val MIN_SDK = 23
        const val COMPILE_SDK = 36
        const val BUILD_TOOLS_VERSION = "36.0.0"
    }

    const val GROUP_ID = "com.datadoghq"

    val VERSION = Version(1, 4, 0, Version.Type.Dev)
}
