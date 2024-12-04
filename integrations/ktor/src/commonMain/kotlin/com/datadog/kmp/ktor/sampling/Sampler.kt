/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.sampling

import com.datadog.kmp.ktor.RNG

internal interface Sampler {
    // strictly speaking if there is some variable sample rate, result of sampleRate call may be different from the
    // value used for the sample() decision, so not the best API. But since it is internal and we have only single
    // implementation so far, it is fine.
    val sampleRate: Float
    fun sample(): Boolean
}

internal class FixedRateSampler(override val sampleRate: Float) : Sampler {
    override fun sample(): Boolean {
        return RNG.nextDouble(MIN_SAMPLE_RATE, MAX_SAMPLE_RATE).toFloat() < sampleRate
    }

    companion object {
        const val MIN_SAMPLE_RATE: Double = 0.0
        const val MAX_SAMPLE_RATE: Double = 100.0
    }
}
