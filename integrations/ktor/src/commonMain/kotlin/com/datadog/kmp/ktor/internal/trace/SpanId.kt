/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.trace

import com.datadog.kmp.ktor.HEX_RADIX

/**
 * Represents a Span ID (as a 64 bit id).
 * @property raw the raw bits of the id
 */
internal data class SpanId(
    val raw: ULong
) {

    /**
     * @return the hexadecimal representation of this id
     */
    fun toHexString(): String {
        return raw.toString(HEX_RADIX).lowercase()
    }
}
