/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

import com.datadog.kmp.Datadog

/**
 * Internal API, can be changed or removed at any point.
 */
object InternalProxy {

    /**
     * Indicates if crash reporting is enabled or not.
     */
    val isCrashReportingEnabled: Boolean
        get() { return Datadog.isCrashReportingEnabled }
}
