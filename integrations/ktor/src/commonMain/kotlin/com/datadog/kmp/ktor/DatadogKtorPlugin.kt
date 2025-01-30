/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import com.datadog.kmp.ktor.internal.plugin.DatadogKtorPlugin
import com.datadog.kmp.ktor.internal.plugin.buildClientPlugin
import com.datadog.kmp.ktor.internal.sampling.DeterministicTraceSampler
import com.datadog.kmp.ktor.internal.trace.DefaultSpanIdGenerator
import com.datadog.kmp.ktor.internal.trace.DefaultTraceIdGenerator
import com.datadog.kmp.rum.RumMonitor
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.ClientPlugin

internal const val DEFAULT_TRACE_SAMPLE_RATE: Float = 20f

/**
 * Create a Datadog plugin for a Ktor [HttpClient].
 * @param tracedHosts the map of all the hosts to track, and the header types that you want
 * to use to handle distributed traces. For a default setup, we recommend using the DATADOG + TRACECONTEXT header types
 * for the hosts you own.
 * @param traceSamplingRate the sampling rate for the tracing (between 0 and 100)
 * @param rumResourceAttributesProvider which listens on the intercepted Ktor call chain
 * and offers the possibility to add custom attributes to the RUM resource events.
 */
fun datadogKtorPlugin(
    tracedHosts: Map<String, Set<TracingHeaderType>> = emptyMap(),
    traceSamplingRate: Float = DEFAULT_TRACE_SAMPLE_RATE,
    rumResourceAttributesProvider: RumResourceAttributesProvider = DefaultRumResourceAttributesProvider
): ClientPlugin<Unit> {
    return DatadogKtorPlugin(
        RumMonitor.get(),
        tracedHosts,
        DeterministicTraceSampler(traceSamplingRate),
        DefaultTraceIdGenerator(),
        DefaultSpanIdGenerator(),
        rumResourceAttributesProvider
    ).buildClientPlugin()
}
