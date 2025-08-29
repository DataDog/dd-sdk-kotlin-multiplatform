/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import com.datadog.kmp.ktor.internal.addToW3cBaggage
import com.datadog.kmp.ktor.internal.trace.SpanId
import com.datadog.kmp.ktor.internal.trace.TraceId
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers

/**
 * Defines the list of tracing header types that can be injected into http requests.
 */
enum class TracingHeaderType {
    /**
     * Datadog's [`x-datadog-*` header](https://docs.datadoghq.com/real_user_monitoring/connect_rum_and_traces/?tab=browserrum#how-are-rum-resources-linked-to-traces).
     */
    DATADOG,

    /**
     * Open Telemetry B3 [Single header](https://github.com/openzipkin/b3-propagation#single-header).
     */
    B3,

    /**
     * Open Telemetry B3 [Multiple headers](https://github.com/openzipkin/b3-propagation#multiple-headers).
     */
    B3MULTI,

    /**
     * W3C [Trace Context header](https://www.w3.org/TR/trace-context/#tracestate-header).
     */
    TRACECONTEXT;

    /**
     * Inject the tracing headers for the current method.
     * @param request the builder for the Ktor request to inject
     * @param sampledIn whether the trace is sampled in
     * @param traceId the trace id
     * @param spanId the span id
     * @param rumSessionId the RUM session id
     */
    internal fun injectHeaders(
        request: HttpRequestBuilder,
        sampledIn: Boolean,
        traceId: TraceId,
        spanId: SpanId,
        rumSessionId: String?
    ) {
        request.headers {
            when (this@TracingHeaderType) {
                DATADOG -> {
                    // remove pre-existing if any
                    remove(DATADOG_TRACE_ID_KEY)
                    remove(DATADOG_TAGS_KEY)
                    remove(DATADOG_SPAN_ID_KEY)
                    remove(DATADOG_SAMPLING_PRIORITY_KEY)
                    remove(DATADOG_ORIGIN_KEY)
                    if (sampledIn) {
                        val mostSignificantTraceId = traceId.high.toString(HEX_RADIX)
                        append(DATADOG_TRACE_ID_KEY, traceId.low.toString())
                        append(DATADOG_TAGS_KEY, "$DATADOG_MOST_SIGNIFICANT_TRACE_ID_TAG=$mostSignificantTraceId")
                        append(DATADOG_SPAN_ID_KEY, spanId.raw.toString())
                        append(DATADOG_SAMPLING_PRIORITY_KEY, DATADOG_KEEP_SAMPLING_DECISION)
                        append(DATADOG_ORIGIN_KEY, DATADOG_ORIGIN_RUM)
                    } else {
                        append(DATADOG_SAMPLING_PRIORITY_KEY, DATADOG_DROP_SAMPLING_DECISION)
                    }
                }

                B3 -> {
                    // remove pre-existing if any
                    remove(B3_HEADER_KEY)
                    if (sampledIn) {
                        append(B3_HEADER_KEY, "${traceId.toHexString()}-${spanId.toHexString()}-1")
                    } else {
                        append(B3_HEADER_KEY, B3_DROP_SAMPLING_DECISION)
                    }
                }

                B3MULTI -> {
                    // remove pre-existing if any
                    remove(B3M_TRACE_ID_KEY)
                    remove(B3M_SPAN_ID_KEY)
                    remove(B3M_SAMPLING_PRIORITY_KEY)
                    if (sampledIn) {
                        append(B3M_TRACE_ID_KEY, traceId.toHexString())
                        append(B3M_SPAN_ID_KEY, spanId.toHexString())
                        append(B3M_SAMPLING_PRIORITY_KEY, B3M_KEEP_SAMPLING_DECISION)
                    } else {
                        append(B3M_SAMPLING_PRIORITY_KEY, B3M_DROP_SAMPLING_DECISION)
                    }
                }

                TRACECONTEXT -> {
                    // remove pre-existing if any
                    remove(W3C_TRACEPARENT_KEY)
                    remove(W3C_TRACESTATE_KEY)

                    val paddedTraceId = traceId.toHexString().padStart(W3C_TRACE_ID_LENGTH, '0')
                    val paddedSpanId = spanId.toHexString().padStart(W3C_PARENT_ID_LENGTH, '0')
                    val decision = if (sampledIn) W3C_SAMPLE_PRIORITY_ACCEPT else W3C_SAMPLE_PRIORITY_DROP
                    append(W3C_TRACEPARENT_KEY, "00-$paddedTraceId-$paddedSpanId-$decision")
                }
            }

            if (rumSessionId != null) {
                addToW3cBaggage(DATADOG_RUM_SESSION_ID_TAG, rumSessionId)
            }
        }
    }
}
