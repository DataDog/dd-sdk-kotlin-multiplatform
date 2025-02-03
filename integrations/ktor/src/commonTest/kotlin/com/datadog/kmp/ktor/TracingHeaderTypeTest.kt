/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import com.datadog.kmp.ktor.internal.trace.SpanId
import com.datadog.kmp.ktor.internal.trace.TraceId
import io.ktor.client.request.HttpRequestBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class TracingHeaderTypeTest {

    @Test
    fun `M inject Datadog headers W injectHeaders`() {
        // Given
        val expectedHeaders = mapOf(
            Fixture(TraceId(0u, 1337u), SpanId(42u), true) to mapOf(
                "x-datadog-parent-id" to "42",
                "x-datadog-trace-id" to "1337",
                "x-datadog-tags" to "_dd.p.tid=0",
                "x-datadog-sampling-priority" to "1",
                "x-datadog-origin" to "rum"
            ),
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), true) to mapOf(
                "x-datadog-parent-id" to "42",
                "x-datadog-trace-id" to "1337",
                "x-datadog-tags" to "_dd.p.tid=499602d2",
                "x-datadog-sampling-priority" to "1",
                "x-datadog-origin" to "rum"
            ),
            Fixture(TraceId(ULong.MAX_VALUE, ULong.MAX_VALUE), SpanId(ULong.MAX_VALUE), true) to mapOf(
                "x-datadog-parent-id" to "18446744073709551615",
                "x-datadog-trace-id" to "18446744073709551615",
                "x-datadog-tags" to "_dd.p.tid=ffffffffffffffff",
                "x-datadog-sampling-priority" to "1",
                "x-datadog-origin" to "rum"
            ),
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), false) to mapOf(
                "x-datadog-sampling-priority" to "0"
            )
        )

        // When + Then
        expectedHeaders.forEach {
            val fakeRequestBuilder = HttpRequestBuilder()
            TracingHeaderType.DATADOG.injectHeaders(
                fakeRequestBuilder,
                it.key.sampledIn,
                it.key.traceId,
                it.key.spanId
            )
            val headers = fakeRequestBuilder.headers.entries()
                .associate {
                    assertEquals(1, it.value.size)
                    it.key to it.value.first()
                }
            assertEquals(it.value, headers)
        }
    }

    @Test
    fun `M inject TraceContext headers W injectHeaders`() {
        // Given
        val expectedHeaders = mapOf(
            Fixture(TraceId(0u, 1337u), SpanId(42u), true) to mapOf(
                "traceparent" to "00-00000000000000000000000000000539-000000000000002a-01"
            ),
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), true) to mapOf(
                "traceparent" to "00-00000000499602d20000000000000539-000000000000002a-01"
            ),
            Fixture(TraceId(ULong.MAX_VALUE, ULong.MAX_VALUE), SpanId(ULong.MAX_VALUE), true) to mapOf(
                "traceparent" to "00-ffffffffffffffffffffffffffffffff-ffffffffffffffff-01"
            ),
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), false) to mapOf(
                "traceparent" to "00-00000000499602d20000000000000539-000000000000002a-00"
            )
        )

        // When + Then
        expectedHeaders.forEach {
            val fakeRequestBuilder = HttpRequestBuilder()
            TracingHeaderType.TRACECONTEXT.injectHeaders(
                fakeRequestBuilder,
                it.key.sampledIn,
                it.key.traceId,
                it.key.spanId
            )
            val headers = fakeRequestBuilder.headers.entries()
                .associate {
                    assertEquals(1, it.value.size)
                    it.key to it.value.first()
                }
            assertEquals(it.value, headers)
        }
    }

    @Test
    fun `M inject B3 headers W injectHeaders`() {
        // Given
        val expectedHeaders = mapOf(
            Fixture(TraceId(0u, 1337u), SpanId(42u), true) to mapOf(
                "b3" to "539-2a-1"
            ),
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), true) to mapOf(
                "b3" to "499602d20000000000000539-2a-1"
            ),
            Fixture(TraceId(ULong.MAX_VALUE, ULong.MAX_VALUE), SpanId(ULong.MAX_VALUE), true) to mapOf(
                "b3" to "ffffffffffffffffffffffffffffffff-ffffffffffffffff-1"
            ),
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), false) to mapOf(
                "b3" to "0"
            )
        )

        // When + Then
        expectedHeaders.forEach {
            val fakeRequestBuilder = HttpRequestBuilder()
            TracingHeaderType.B3.injectHeaders(
                fakeRequestBuilder,
                it.key.sampledIn,
                it.key.traceId,
                it.key.spanId
            )
            val headers = fakeRequestBuilder.headers.entries()
                .associate {
                    assertEquals(1, it.value.size)
                    it.key to it.value.first()
                }
            assertEquals(it.value, headers)
        }
    }

    @Test
    fun `M inject B3Multi headers W injectHeaders`() {
        // Given
        val expectedHeaders = mapOf(
            Fixture(TraceId(0u, 1337u), SpanId(42u), true) to mapOf(
                "X-B3-TraceId" to "539",
                "X-B3-SpanId" to "2a",
                "X-B3-Sampled" to "1"
            ),
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), true) to mapOf(
                "X-B3-TraceId" to "499602d20000000000000539",
                "X-B3-SpanId" to "2a",
                "X-B3-Sampled" to "1"
            ),
            Fixture(TraceId(ULong.MAX_VALUE, ULong.MAX_VALUE), SpanId(ULong.MAX_VALUE), true) to mapOf(
                "X-B3-TraceId" to "ffffffffffffffffffffffffffffffff",
                "X-B3-SpanId" to "ffffffffffffffff",
                "X-B3-Sampled" to "1"
            ),
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), false) to mapOf(
                "X-B3-Sampled" to "0"
            )
        )

        // When + Then
        expectedHeaders.forEach {
            val fakeRequestBuilder = HttpRequestBuilder()
            TracingHeaderType.B3MULTI.injectHeaders(
                fakeRequestBuilder,
                it.key.sampledIn,
                it.key.traceId,
                it.key.spanId
            )
            val headers = fakeRequestBuilder.headers.entries()
                .associate {
                    assertEquals(1, it.value.size)
                    it.key to it.value.first()
                }
            assertEquals(it.value, headers)
        }
    }

    // region private

    private data class Fixture(
        val traceId: TraceId,
        val spanId: SpanId,
        val sampledIn: Boolean
    )

    // endregion
}
