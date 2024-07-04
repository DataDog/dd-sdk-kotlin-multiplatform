/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import com.benasher44.uuid.uuid4
import com.datadog.kmp.ktor.trace.DefaultSpanIdGenerator
import com.datadog.kmp.ktor.trace.DefaultTraceIdGenerator
import com.datadog.kmp.ktor.trace.SpanIdGenerator
import com.datadog.kmp.ktor.trace.TraceIdGenerator
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.util.AttributeKey

internal const val PLUGIN_NAME = "Datadog"
internal const val DD_REQUEST_ID = "X-Datadog-Request-ID"
internal val DD_REQUEST_ID_ATTR = AttributeKey<String>(DD_REQUEST_ID)
internal const val DEFAULT_TRACE_SAMPLE_RATE: Float = 20f
internal const val MIN_SAMPLE_RATE: Double = 0.0
internal const val MAX_SAMPLE_RATE: Double = 100.0

/**
 * Create a Datadog plugin for a Ktor [HttpClient].
 * @param tracedHosts the list of all the hosts to track, and the header types that you want
 * to use to handle distributed traces
 * @param traceSamplingRate the sampling rate for the tracing (between 0 and 100)
 * @param traceIdGenerator the generator for trace ids
 * @param spanIdGenerator the generator for span ids
 */
fun datadogKtorPlugin(
    tracedHosts: Map<String, Set<TracingHeaderType>> = emptyMap(),
    traceSamplingRate: Float = DEFAULT_TRACE_SAMPLE_RATE,
    traceIdGenerator: TraceIdGenerator = DefaultTraceIdGenerator(),
    spanIdGenerator: SpanIdGenerator = DefaultSpanIdGenerator()
): ClientPlugin<Unit> {
    return createClientPlugin(PLUGIN_NAME) {
        // TODO RUM-5228 report request timings (DNS, SSL, …)
        // TODO RUM-5229 report request exceptions

        onRequest { request, _ ->

            val isSampledIn = RNG.nextDouble(MIN_SAMPLE_RATE, MAX_SAMPLE_RATE).toFloat() < traceSamplingRate
            val traceHeaderTypes = tracedHosts[request.url.host]
            val attributes = mutableMapOf<String, Any?>()

            val traceId = traceIdGenerator.generateTraceId()
            val spanId = spanIdGenerator.generateSpanId()

            if (isSampledIn && !traceHeaderTypes.isNullOrEmpty()) {
                traceHeaderTypes.forEach { headerType ->
                    headerType.injectHeaders(request, true, traceId, spanId)
                }
                attributes[RUM_TRACE_ID] = traceId
                attributes[RUM_SPAN_ID] = spanId
                attributes[RUM_RULE_PSR] = traceSamplingRate
            } else {
                TracingHeaderType.entries.forEach { headerType ->
                    headerType.injectHeaders(request, false, traceId, spanId)
                }
            }

            val requestId = uuid4().toString()
            request.attributes.put(DD_REQUEST_ID_ATTR, requestId)
            RumMonitor.get().startResource(
                key = requestId,
                method = request.method.asRumMethod(),
                url = request.url.buildString(),
                attributes = emptyMap()
            )
        }

        onResponse { response ->
            val requestId = response.request.attributes.getOrNull(DD_REQUEST_ID_ATTR)
            if (requestId != null) {
                RumMonitor.get().stopResource(
                    key = requestId,
                    statusCode = response.status.value,
                    size = null, // TODO RUM-5233 report request size
                    kind = RumResourceKind.NATIVE,
                    attributes = emptyMap()
                )
            } else {
                // TODO RUM-5254 handle missing request id case
            }
        }
    }
}

private fun HttpMethod.asRumMethod(): RumResourceMethod {
    return when (this) {
        HttpMethod.Post -> RumResourceMethod.POST
        HttpMethod.Get -> RumResourceMethod.GET
        HttpMethod.Head -> RumResourceMethod.HEAD
        HttpMethod.Put -> RumResourceMethod.PUT
        HttpMethod.Delete -> RumResourceMethod.DELETE
        HttpMethod.Patch -> RumResourceMethod.PATCH
        HttpMethod.Options -> RumResourceMethod.OPTIONS

        else -> {
            // TODO log unknown HTTP method
            RumResourceMethod.CONNECT
        }
    }
}
