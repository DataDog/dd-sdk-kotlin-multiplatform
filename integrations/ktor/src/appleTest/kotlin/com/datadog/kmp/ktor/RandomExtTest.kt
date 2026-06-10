/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import com.datadog.tools.random.randomInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RandomExtTest {

    @Test
    fun `M fill lower bits first W nextBits`() {
        // Given
        val bitCount = randomInt(from = 8, until = 25)

        // When
        val value = RNG.nextBits(bitCount)

        // Then
        assertEquals(0, value ushr bitCount)
        assertNotEquals(0, value shl Int.SIZE_BITS - bitCount)
    }
}
