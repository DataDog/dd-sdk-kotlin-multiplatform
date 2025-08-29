/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import com.benasher44.uuid.uuid4
import com.datadog.kmp.ktor.internal.trace.SpanId
import com.datadog.kmp.ktor.internal.trace.TraceId
import com.datadog.tools.random.nullable
import io.ktor.client.request.HttpRequestBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class TracingHeaderTypeTest {

    private var fakeRumSessionId = nullable(uuid4().toString())

    @Test
    fun `M inject Datadog headers W injectHeaders`() {
        // Given
        val expectedHeaders = mapOf(
            Fixture(TraceId(0u, 1337u), SpanId(42u), true, fakeRumSessionId) to buildMap {
                put("x-datadog-parent-id", "42")
                put("x-datadog-trace-id", "1337")
                put("x-datadog-tags", "_dd.p.tid=0")
                put("x-datadog-sampling-priority", "1")
                put("x-datadog-origin", "rum")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), true, fakeRumSessionId) to buildMap {
                put("x-datadog-parent-id", "42")
                put("x-datadog-trace-id", "1337")
                put("x-datadog-tags", "_dd.p.tid=499602d2")
                put("x-datadog-sampling-priority", "1")
                put("x-datadog-origin", "rum")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(
                TraceId(ULong.MAX_VALUE, ULong.MAX_VALUE),
                SpanId(ULong.MAX_VALUE),
                true,
                fakeRumSessionId
            ) to buildMap {
                put("x-datadog-parent-id", "18446744073709551615")
                put("x-datadog-trace-id", "18446744073709551615")
                put("x-datadog-tags", "_dd.p.tid=ffffffffffffffff")
                put("x-datadog-sampling-priority", "1")
                put("x-datadog-origin", "rum")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), false, fakeRumSessionId) to buildMap {
                put("x-datadog-sampling-priority", "0")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            }
        )

        // When + Then
        expectedHeaders.forEach {
            val fakeRequestBuilder = HttpRequestBuilder()
            TracingHeaderType.DATADOG.injectHeaders(
                fakeRequestBuilder,
                it.key.sampledIn,
                it.key.traceId,
                it.key.spanId,
                it.key.rumSessionId
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
            Fixture(TraceId(0u, 1337u), SpanId(42u), true, fakeRumSessionId) to buildMap {
                put("traceparent", "00-00000000000000000000000000000539-000000000000002a-01")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), true, fakeRumSessionId) to buildMap {
                put("traceparent", "00-00000000499602d20000000000000539-000000000000002a-01")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(
                TraceId(ULong.MAX_VALUE, ULong.MAX_VALUE),
                SpanId(ULong.MAX_VALUE),
                true,
                fakeRumSessionId
            ) to buildMap {
                put("traceparent", "00-ffffffffffffffffffffffffffffffff-ffffffffffffffff-01")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), false, fakeRumSessionId) to buildMap {
                put("traceparent", "00-00000000499602d20000000000000539-000000000000002a-00")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            }
        )

        // When + Then
        expectedHeaders.forEach {
            val fakeRequestBuilder = HttpRequestBuilder()
            TracingHeaderType.TRACECONTEXT.injectHeaders(
                fakeRequestBuilder,
                it.key.sampledIn,
                it.key.traceId,
                it.key.spanId,
                it.key.rumSessionId
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
            Fixture(TraceId(0u, 1337u), SpanId(42u), true, fakeRumSessionId) to buildMap {
                put("b3", "539-2a-1")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), true, fakeRumSessionId) to buildMap {
                put("b3", "499602d20000000000000539-2a-1")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(
                TraceId(ULong.MAX_VALUE, ULong.MAX_VALUE),
                SpanId(ULong.MAX_VALUE),
                true,
                fakeRumSessionId
            ) to buildMap {
                put("b3", "ffffffffffffffffffffffffffffffff-ffffffffffffffff-1")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), false, fakeRumSessionId) to buildMap {
                put("b3", "0")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            }
        )

        // When + Then
        expectedHeaders.forEach {
            val fakeRequestBuilder = HttpRequestBuilder()
            TracingHeaderType.B3.injectHeaders(
                fakeRequestBuilder,
                it.key.sampledIn,
                it.key.traceId,
                it.key.spanId,
                it.key.rumSessionId
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
            Fixture(TraceId(0u, 1337u), SpanId(42u), true, fakeRumSessionId) to buildMap {
                put("X-B3-TraceId", "539")
                put("X-B3-SpanId", "2a")
                put("X-B3-Sampled", "1")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), true, fakeRumSessionId) to buildMap {
                put("X-B3-TraceId", "499602d20000000000000539")
                put("X-B3-SpanId", "2a")
                put("X-B3-Sampled", "1")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(
                TraceId(ULong.MAX_VALUE, ULong.MAX_VALUE),
                SpanId(ULong.MAX_VALUE),
                true,
                fakeRumSessionId
            ) to buildMap {
                put("X-B3-TraceId", "ffffffffffffffffffffffffffffffff")
                put("X-B3-SpanId", "ffffffffffffffff")
                put("X-B3-Sampled", "1")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            },
            Fixture(TraceId(1234567890u, 1337u), SpanId(42u), false, fakeRumSessionId) to buildMap {
                put("X-B3-Sampled", "0")
                if (fakeRumSessionId != null) put("baggage", "session.id=$fakeRumSessionId")
            }
        )

        // When + Then
        expectedHeaders.forEach {
            val fakeRequestBuilder = HttpRequestBuilder()
            TracingHeaderType.B3MULTI.injectHeaders(
                fakeRequestBuilder,
                it.key.sampledIn,
                it.key.traceId,
                it.key.spanId,
                it.key.rumSessionId
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
        val sampledIn: Boolean,
        val rumSessionId: String? = null
    )

    // endregion
}
