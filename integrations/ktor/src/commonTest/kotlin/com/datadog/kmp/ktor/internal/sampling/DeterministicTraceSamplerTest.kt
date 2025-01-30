/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.sampling

import com.datadog.kmp.ktor.internal.trace.TraceId
import com.datadog.tools.random.randomFloat
import com.datadog.tools.random.randomInt
import com.datadog.tools.random.randomULong
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeterministicTraceSamplerTest {

    private lateinit var fakeTraceIds: List<TraceId>

    @BeforeTest
    fun `set up`() {
        val listSize = randomInt(from = 256, until = 1024)
        fakeTraceIds = Array(listSize) { TraceId(randomULong(), randomULong()) }.toList()
    }

    @Test
    fun `M return consistent results W sample + hardcodedFixtures`() {
        hardcodedFixtures().forEach {
            // Given
            val testedSampler = DeterministicTraceSampler(it.samplingRate)

            // When
            val sampled = testedSampler.sample(TraceId(randomULong(), it.traceIdLow))

            // Then
            assertEquals(it.samplingDecision, sampled)
        }
    }

    @Test
    fun `the sampler will sample the values based on the fixed sample rate`() {
        repeat(128) {
            // Given
            val fakeSampleRate = randomFloat(0f, 100f)
            val testedSampler = DeterministicTraceSampler(fakeSampleRate)
            var sampledIn = 0

            // When
            fakeTraceIds.forEach {
                if (testedSampler.sample(it)) {
                    sampledIn++
                }
            }

            // Then
            val offset = 2.5f * fakeTraceIds.size
            assertEquals(fakeTraceIds.size * fakeSampleRate / 100f, sampledIn.toFloat(), offset)
        }
    }

    @Test
    fun `when sample rate is 0 all values will be dropped`() {
        // Given
        val testedSampler = DeterministicTraceSampler(0f)
        var sampledIn = 0

        // When
        fakeTraceIds.forEach {
            if (testedSampler.sample(it)) {
                sampledIn++
            }
        }

        // Then
        assertEquals(0, sampledIn)
    }

    @Test
    fun `when sample rate is 100 all values will pass`() {
        // Given
        val testedSampler = DeterministicTraceSampler(100f)
        var sampledIn = 0

        // When
        fakeTraceIds.forEach {
            if (testedSampler.sample(it)) {
                sampledIn++
            }
        }

        // Then
        assertEquals(fakeTraceIds.size, sampledIn)
    }

    @Test
    fun `when sample rate is below 0 it is normalized to 0`() {
        // Given
        val fakeSampleRate = randomFloat(until = 0f)
        val testedSampler = DeterministicTraceSampler(fakeSampleRate)

        // When
        val effectiveSampleRate = testedSampler.sampleRate

        // Then
        assertEquals(0f, effectiveSampleRate)
    }

    @Test
    fun `when sample rate is above 100 it is normalized to 100`() {
        // Given
        val fakeSampleRate = randomFloat(from = 100.01f)
        val testedSampler = DeterministicTraceSampler(fakeSampleRate)

        // When
        val effectiveSampleRate = testedSampler.sampleRate

        // Then
        assertEquals(100f, effectiveSampleRate)
    }

    data class Fixture(
        val traceIdLow: ULong,
        val samplingRate: Float,
        val samplingDecision: Boolean
    )

    companion object {

        fun hardcodedFixtures(): List<Fixture> {
            return listOf(
                Fixture(4815162342u, 55.9f, false),
                Fixture(4815162342u, 56.0f, true),
                Fixture(1415926535897932384u, 90.5f, false),
                Fixture(1415926535897932384u, 90.6f, true),
                Fixture(718281828459045235u, 7.4f, false),
                Fixture(718281828459045235u, 7.5f, true),
                Fixture(41421356237309504u, 32.1f, false),
                Fixture(41421356237309504u, 32.2f, true),
                Fixture(6180339887498948482u, 68.2f, false),
                Fixture(6180339887498948482u, 68.3f, true)
            )
        }
    }
}
