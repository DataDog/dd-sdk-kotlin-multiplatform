/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import cocoapods.DatadogObjc.DDDatadog
import cocoapods.DatadogObjc.DDSDKVerbosityLevelDebug
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual object Datadog {
    actual fun setVerbosity() {
        DDDatadog.setVerbosityLevel(DDSDKVerbosityLevelDebug)
    }
}
