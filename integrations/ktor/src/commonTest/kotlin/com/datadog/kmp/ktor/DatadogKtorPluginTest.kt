/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import kotlin.test.Test
import kotlin.test.assertEquals

class DatadogKtorPluginTest {

    @Test
    fun `M sample all traces by default W datadogKtorPlugin`() {
        // Then
        assertEquals(100f, DEFAULT_TRACE_SAMPLE_RATE)
    }
}
