/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.sampling

import com.datadog.kmp.ktor.internal.trace.TraceId

internal interface Sampler<T : Any> {
    // strictly speaking if there is some variable sample rate, result of sampleRate call may be different from the
    // value used for the sample() decision, so not the best API. But since it is internal and we have only single
    // implementation so far, it is fine.
    val sampleRate: Float
    fun sample(item: T): Boolean
}

/**
 * [Sampler] with the given sample rate using a deterministic algorithm for a stable
 * sampling decision across sources.
 */
internal open class DeterministicTraceSampler(
    sampleRate: Float
) : Sampler<TraceId> {

    override fun sample(item: TraceId): Boolean {
        return when {
            sampleRate >= SAMPLE_ALL_RATE -> true
            sampleRate <= 0f -> false
            else -> {
                val hash = item.low * SAMPLER_HASHER
                val threshold = (MAX_ID.toDouble() * sampleRate / SAMPLE_ALL_RATE).toULong()
                hash < threshold
            }
        }
    }

    override val sampleRate: Float = sampleRate.coerceAtLeast(0f)
        .coerceAtMost(SAMPLE_ALL_RATE)

    private companion object {
        const val SAMPLE_ALL_RATE = 100f

        // Good number for Knuth hashing (large, prime, fit in 64 bit long)
        private const val SAMPLER_HASHER: ULong = 1111111111111111111u

        private const val MAX_ID: ULong = 0xFFFFFFFFFFFFFFFFUL
    }
}
