/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.trace

import com.datadog.kmp.ktor.HEX_RADIX

/**
 * Represents a Trace ID (as a 128 bit id).
 * @property high the high bits of the id
 * @property low the low bits of the id
 */
data class TraceId(
    val high: ULong,
    val low: ULong
) {

    /**
     * @return the hexadecimal representation of this id
     */
    fun toHexString(): String {
        if (high == 0L.toULong()) {
            return low.toString(HEX_RADIX).lowercase()
        } else {
            val lowPadded = low.toString(HEX_RADIX).padStart(LONG_HEX_SIZE, '0')
            return (high.toString(HEX_RADIX) + lowPadded).lowercase()
        }
    }

    companion object {
        internal const val LONG_HEX_SIZE = 16
    }
}
