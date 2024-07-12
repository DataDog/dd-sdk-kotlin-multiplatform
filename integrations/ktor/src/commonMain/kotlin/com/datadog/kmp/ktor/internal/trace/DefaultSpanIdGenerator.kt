/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.trace

import com.datadog.kmp.ktor.RNG

internal class DefaultSpanIdGenerator : SpanIdGenerator {

    override fun generateSpanId(): SpanId {
        return SpanId(RNG.nextLong(1, Long.MAX_VALUE).toULong())
    }
}
