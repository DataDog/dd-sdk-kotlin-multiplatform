/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.test

import com.datadog.kmp.ktor.HEX_RADIX
import com.datadog.kmp.ktor.TracingHeaderType
import io.ktor.http.Headers
import kotlin.test.assertEquals

class HeadersAssert private constructor(private val actual: Headers) {

    fun hasTraceId(traceId: String, format: TracingHeaderType): HeadersAssert {
        traceContextAssert(format)
            .assertTraceId(actual, traceId)
        return this
    }

    fun hasSpanId(spanId: String, format: TracingHeaderType): HeadersAssert {
        traceContextAssert(format)
            .assertSpanId(actual, spanId)
        return this
    }

    fun hasSamplingDecision(samplingDecision: Int, format: TracingHeaderType): HeadersAssert {
        traceContextAssert(format)
            .assertSamplingDecision(actual, samplingDecision)
        return this
    }

    private fun traceContextAssert(format: TracingHeaderType): TraceContextAssert<Headers> = when (format) {
        TracingHeaderType.DATADOG -> DatadogTraceContextAssert
        TracingHeaderType.TRACECONTEXT -> TraceContextContextAssert
        TracingHeaderType.B3 -> B3TraceContextAssert
        TracingHeaderType.B3MULTI -> B3MultiContextAssert
    }

    companion object {
        fun assertThat(headers: Headers): HeadersAssert = HeadersAssert(headers)
    }
}

private interface TraceContextAssert<T> {
    fun assertTraceId(carrier: T, traceIdHex: String)
    fun assertSpanId(carrier: T, spanIdHex: String)
    fun assertSamplingDecision(carrier: T, samplingDecision: Int)
}

private object DatadogTraceContextAssert : TraceContextAssert<Headers> {

    override fun assertTraceId(carrier: Headers, traceIdHex: String) {
        val actualLeastSignificantTraceId = carrier["x-datadog-trace-id"]
            ?.toULong()
            ?.toString(HEX_RADIX)
            ?.padStart(16, '0')
            .orEmpty()
        val actualMostSignificantTraceId = carrier["x-datadog-tags"]
            ?.split(",")
            ?.associate { it.split("=").let { it[0] to it[1] } }
            ?.get("_dd.p.tid")
            .orEmpty()
        val actualTraceId = actualMostSignificantTraceId + actualLeastSignificantTraceId
        assertEquals(
            traceIdHex,
            actualTraceId,
            "Expected Datadog traceId to be $traceIdHex, but was $actualTraceId.\n${carrier.toLinesString()}"
        )
    }

    override fun assertSpanId(carrier: Headers, spanIdHex: String) {
        val actualSpanId = carrier["x-datadog-parent-id"]
        assertEquals(
            spanIdHex,
            actualSpanId,
            "Expected Datadog spanId to be $spanIdHex, but was $actualSpanId.\n${carrier.toLinesString()}"
        )
    }

    override fun assertSamplingDecision(carrier: Headers, samplingDecision: Int) {
        val actualSamplingDecision = carrier["x-datadog-sampling-priority"]
            ?.toIntOrNull()
        assertEquals(
            samplingDecision,
            actualSamplingDecision,
            "Expected Datadog samplingDecision to be $samplingDecision," +
                " but was $actualSamplingDecision.\n${carrier.toLinesString()}"
        )
    }
}

private object TraceContextContextAssert : TraceContextAssert<Headers> {

    private data class TraceContext(val traceId: String, val spanId: String, val samplingDecision: Int)

    override fun assertTraceId(carrier: Headers, traceIdHex: String) {
        val actualTraceId = extractTraceContext(carrier)?.traceId
        assertEquals(
            traceIdHex,
            actualTraceId,
            "Expected TraceContext traceId to be $traceIdHex," +
                " but was $actualTraceId.\n${carrier.toLinesString()}"
        )
    }

    override fun assertSpanId(carrier: Headers, spanIdHex: String) {
        val actualSpanId = extractTraceContext(carrier)?.spanId
        assertEquals(
            spanIdHex,
            actualSpanId,
            "Expected TraceContext spanId to be $spanIdHex, but was $actualSpanId.\n${carrier.toLinesString()}"
        )
    }

    override fun assertSamplingDecision(carrier: Headers, samplingDecision: Int) {
        val actualSamplingDecision = extractTraceContext(carrier)?.samplingDecision
        assertEquals(
            samplingDecision,
            actualSamplingDecision,
            "Expected TraceContext samplingDecision to be $samplingDecision," +
                " but was $actualSamplingDecision.\n${carrier.toLinesString()}"
        )
    }

    private fun extractTraceContext(headers: Headers): TraceContext? {
        val values = headers["traceparent"]
            ?.split("-") ?: return null
        return TraceContext(values[1], values[2], values[3].toInt())
    }
}

private object B3TraceContextAssert : TraceContextAssert<Headers> {

    private data class TraceContext(val traceId: String, val spanId: String, val samplingDecision: Int)

    override fun assertTraceId(carrier: Headers, traceIdHex: String) {
        val actualTraceId = extractTraceContext(carrier)?.traceId
        assertEquals(
            traceIdHex,
            actualTraceId,
            "Expected B3 traceId to be $traceIdHex, but was $actualTraceId.\n${carrier.toLinesString()}"
        )
    }

    override fun assertSpanId(carrier: Headers, spanIdHex: String) {
        val actualSpanId = extractTraceContext(carrier)?.spanId
        assertEquals(
            spanIdHex,
            actualSpanId,
            "Expected B3 spanId to be $spanIdHex, but was $actualSpanId.\n${carrier.toLinesString()}"
        )
    }

    override fun assertSamplingDecision(carrier: Headers, samplingDecision: Int) {
        val actualSamplingDecision = extractTraceContext(carrier)?.samplingDecision
        assertEquals(
            samplingDecision,
            actualSamplingDecision,
            "Expected B3 samplingDecision to be $samplingDecision," +
                " but was $actualSamplingDecision.\n${carrier.toLinesString()}"
        )
    }

    private fun extractTraceContext(headers: Headers): TraceContext? {
        val values = headers["b3"]
            ?.split("-") ?: return null
        return if (values.size > 1) {
            TraceContext(values[0], values[1], values[2].toInt())
        } else {
            TraceContext("", "", values[0].toInt())
        }
    }
}

private object B3MultiContextAssert : TraceContextAssert<Headers> {

    override fun assertTraceId(carrier: Headers, traceIdHex: String) {
        val actualTraceId = carrier["X-B3-TraceId"]
        assertEquals(
            traceIdHex,
            actualTraceId,
            "Expected B3Multi traceId to be $traceIdHex, but was $actualTraceId.\n${carrier.toLinesString()}"
        )
    }

    override fun assertSpanId(carrier: Headers, spanIdHex: String) {
        val actualSpanId = carrier["X-B3-SpanId"]
        assertEquals(
            spanIdHex,
            actualSpanId,
            "Expected B3Multi spanId to be $spanIdHex, but was $actualSpanId.\n${carrier.toLinesString()}"
        )
    }

    override fun assertSamplingDecision(carrier: Headers, samplingDecision: Int) {
        val actualSamplingDecision = carrier["X-B3-Sampled"]?.toIntOrNull()
        assertEquals(
            samplingDecision,
            actualSamplingDecision,
            "Expected B3Multi samplingDecision to be $samplingDecision," +
                " but was $actualSamplingDecision.\n${carrier.toLinesString()}"
        )
    }
}

private fun Headers.toLinesString() = "Headers\n${entries().joinToString("\n")}"
