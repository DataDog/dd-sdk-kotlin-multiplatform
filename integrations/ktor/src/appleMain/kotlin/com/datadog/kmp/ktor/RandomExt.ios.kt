/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.posix.arc4random_buf
import kotlin.random.Random

internal actual val RNG: Random = AppleSystemRandom

private object AppleSystemRandom : Random() {

    override fun nextBits(bitCount: Int): Int {
        if (bitCount == 0) return 0

        val value = memScoped {
            val bytes = allocArray<UByteVar>(Int.SIZE_BYTES)
            arc4random_buf(bytes, Int.SIZE_BYTES.convert())

            var result = 0
            repeat(Int.SIZE_BYTES) { index ->
                result = result or (bytes[index].toInt() shl (index * Byte.SIZE_BITS))
            }
            result
        }

        return value ushr (Int.SIZE_BITS - bitCount)
    }
}
