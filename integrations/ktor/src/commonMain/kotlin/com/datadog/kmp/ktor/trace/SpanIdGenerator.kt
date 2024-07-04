/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.trace

/**
 * A generator to create [SpanId].
 */
fun interface SpanIdGenerator {
    /**
     * Generates a new unique [SpanId].
     */
    fun generateSpanId(): SpanId
}
