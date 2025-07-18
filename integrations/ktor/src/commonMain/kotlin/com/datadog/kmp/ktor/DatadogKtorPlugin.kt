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
 * @param traceSampleRate the sample rate for the tracing (between 0 and 100).
 * @param traceContextInjection the trace context injection behavior for this interceptor in the intercepted
 * requests. By default this is set to [TraceContextInjection.All] meaning that all the trace context will be
 * propagated in the intercepted requests no matter if the span created around the request is sampled or not. In case
 * of [TraceContextInjection.Sampled] only the sampled request will propagate the trace context.
 * @param rumResourceAttributesProvider which listens on the intercepted Ktor call chain
 * and offers the possibility to add custom attributes to the RUM resource events.
 */
fun datadogKtorPlugin(
    tracedHosts: Map<String, Set<TracingHeaderType>> = emptyMap(),
    traceSampleRate: Float = DEFAULT_TRACE_SAMPLE_RATE,
    traceContextInjection: TraceContextInjection = TraceContextInjection.All,
    rumResourceAttributesProvider: RumResourceAttributesProvider = DefaultRumResourceAttributesProvider
): ClientPlugin<Unit> {
    return DatadogKtorPlugin(
        RumMonitor.get(),
        tracedHosts,
        DeterministicTraceSampler(traceSampleRate),
        traceContextInjection,
        DefaultTraceIdGenerator(),
        DefaultSpanIdGenerator(),
        rumResourceAttributesProvider
    ).buildClientPlugin()
}

/**
 * Create a Datadog plugin for a Ktor [HttpClient].
 * @param tracedHosts the list of all the hosts to track. For a default setup, DATADOG + TRACECONTEXT header types
 * will be used.
 * @param traceSampleRate the sample rate for the tracing (between 0 and 100).
 * @param traceContextInjection the trace context injection behavior for this interceptor in the intercepted
 * requests. By default this is set to [TraceContextInjection.All] meaning that all the trace context will be
 * propagated in the intercepted requests no matter if the span created around the request is sampled or not. In case
 * of [TraceContextInjection.Sampled] only the sampled request will propagate the trace context.
 * @param rumResourceAttributesProvider which listens on the intercepted Ktor call chain
 * and offers the possibility to add custom attributes to the RUM resource events.
 */
fun datadogKtorPlugin(
    tracedHosts: List<String> = emptyList(),
    traceSampleRate: Float = DEFAULT_TRACE_SAMPLE_RATE,
    traceContextInjection: TraceContextInjection = TraceContextInjection.All,
    rumResourceAttributesProvider: RumResourceAttributesProvider = DefaultRumResourceAttributesProvider
): ClientPlugin<Unit> {
    return datadogKtorPlugin(
        tracedHosts = tracedHosts.associateWith { setOf(TracingHeaderType.DATADOG, TracingHeaderType.TRACECONTEXT) },
        traceSampleRate = traceSampleRate,
        traceContextInjection = traceContextInjection,
        rumResourceAttributesProvider = rumResourceAttributesProvider
    )
}
