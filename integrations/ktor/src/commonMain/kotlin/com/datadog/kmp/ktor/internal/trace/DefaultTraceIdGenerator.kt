/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.trace

import com.datadog.kmp.ktor.RNG

internal class DefaultTraceIdGenerator : TraceIdGenerator {

    override fun generateTraceId(): TraceId {
        var idHi: ULong
        var idLo: ULong
        do {
            // 32-bit unix seconds + 32 bits of zero in decimal
            idHi = (epochSeconds() shl CLOCK_SHIFT).toULong()
            idLo = RNG.nextLong(1, Long.MAX_VALUE).toULong()
        } while (idHi == INVALID_ID && idLo == INVALID_ID)
        return TraceId(idHi, idLo)
    }

    companion object {
        internal const val INVALID_ID: ULong = 0u

        private const val CLOCK_SHIFT = 32
    }
}

internal expect fun epochSeconds(): Long
