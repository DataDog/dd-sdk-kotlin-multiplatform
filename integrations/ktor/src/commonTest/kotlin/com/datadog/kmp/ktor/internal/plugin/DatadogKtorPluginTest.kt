/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.plugin

import com.datadog.kmp.ktor.RUM_RULE_PSR
import com.datadog.kmp.ktor.RUM_SPAN_ID
import com.datadog.kmp.ktor.RUM_TRACE_ID
import com.datadog.kmp.ktor.RumResourceAttributesProvider
import com.datadog.kmp.ktor.TracingHeaderType
import com.datadog.kmp.ktor.internal.trace.SpanId
import com.datadog.kmp.ktor.internal.trace.SpanIdGenerator
import com.datadog.kmp.ktor.internal.trace.TraceId
import com.datadog.kmp.ktor.internal.trace.TraceIdGenerator
import com.datadog.kmp.ktor.sampling.Sampler
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import com.datadog.tools.random.exhaustiveAttributes
import com.datadog.tools.random.nullable
import com.datadog.tools.random.randomElement
import com.datadog.tools.random.randomEnumValues
import com.datadog.tools.random.randomLong
import com.datadog.tools.random.randomULong
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matching
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ConnectTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DatadogKtorPluginTest {

    private val fakeHost = "datadoghq.com"
    private val fakeTracingHeaderTypes = randomEnumValues<TracingHeaderType>()
    private val mockRumMonitor = mock<RumMonitor>()
    private val fakeTracedHosts = mapOf(
        fakeHost to fakeTracingHeaderTypes
    )
    private val mockTraceSampler = mock<Sampler>()
    private val mockTraceIdGenerator = mock<TraceIdGenerator>()
    private val mockSpanIdGenerator = mock<SpanIdGenerator>()
    private val mockRumResourceAttributesProvider = mock<RumResourceAttributesProvider>()

    private val fakeSpanId = SpanId(randomULong())
    private val fakeTraceId = TraceId(high = randomULong(), low = randomULong())

    private val fakeRumRequestAttributes = exhaustiveAttributes()
    private val fakeRumResponseAttributes = exhaustiveAttributes()
    private val fakeRumErrorAttributes = exhaustiveAttributes()

    private var testedPlugin = DatadogKtorPlugin(
        rumMonitor = mockRumMonitor,
        tracedHosts = fakeTracedHosts,
        traceSampler = mockTraceSampler,
        traceIdGenerator = mockTraceIdGenerator,
        spanIdGenerator = mockSpanIdGenerator,
        rumResourceAttributesProvider = mockRumResourceAttributesProvider
    )

    private val mockRequestHandler = mock<MockRequestHandler>()

    // some classes of Ktor request-response chain cannot be mocked, because either they are final or some members
    // are final, so using mock engine instead
    private val fakeClient = HttpClient(
        MockEngine(
            MockEngineConfig().apply {
                addHandler(mockRequestHandler)
            }
        )
    ) {
        install(testedPlugin.buildClientPlugin())
    }

    @BeforeTest
    fun `set up`() {
        every { mockTraceSampler.sample() } returns true
        every { mockTraceSampler.sampleRate } returns 100f
        every { mockTraceIdGenerator.generateTraceId() } returns fakeTraceId
        every { mockSpanIdGenerator.generateSpanId() } returns fakeSpanId
        with(mockRumResourceAttributesProvider) {
            every { onRequest(any()) } returns fakeRumRequestAttributes
            every { onResponse(any()) } returns fakeRumResponseAttributes
            every { onError(any(), any()) } returns fakeRumErrorAttributes
        }
    }

    // region RUM monitor

    @Test
    fun `M start + stop resource tracking W request succeeded + sampled for tracing`() {
        // Given
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = HttpMethod.DefaultMethods.randomElement()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
                if (method in setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                    setBody(
                        listOf("body", TextContent("body", ContentType.Any))
                            .randomElement()
                    )
                }
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        everySuspend {
            mockRequestHandler.invoke(
                any(),
                any()
            )
        } calls { (scope: MockRequestHandleScope, _: HttpRequestData) ->
            scope.respond(
                content = "",
                status = fakeStatusCode,
                headers = Headers.build {
                    if (fakeContextLength != null) {
                        set(HttpHeaders.ContentLength, fakeContextLength.toString())
                    }
                }
            )
        }

        // When
        val response = runBlocking {
            fakeClient.request(request)
        }

        // Then
        verify {
            // TODO RUM-6456 verify request headers
            mockRumMonitor.startResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                statusCode = fakeStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = fakeContextLength,
                // seems capture doesn't work yet for verify blocks, so using matching instead
                // see https://github.com/lupuuss/Mokkery/issues/65
                attributes = matching {
                    assertContains(it, RUM_TRACE_ID)
                    assertContains(it, RUM_SPAN_ID)
                    assertContains(it, RUM_RULE_PSR)

                    checkNotNull(it[RUM_TRACE_ID])
                    checkNotNull(it[RUM_SPAN_ID])
                    checkNotNull(it[RUM_RULE_PSR])

                    assertEquals(
                        fakeRumResponseAttributes,
                        it.toMutableMap().apply {
                            remove(RUM_TRACE_ID)
                            remove(RUM_SPAN_ID)
                            remove(RUM_RULE_PSR)
                        }
                    )
                    true
                }
            )
        }

        verifyNoMoreCalls(mockRumMonitor)
    }

    // TODO RUM-7625 Redirect tracking is wrong: first redirect response will stop resource and it won't be any
    //  new `onRequest` call. See ticket for more details.
    @Test
    fun `M start + stop resource tracking W request succeeded + sampled for tracing + redirect`() {
        // Given
        val fakeUrl = "https://$fakeHost/track"
        // Only Get and Head are allowed for implicit redirect, see Ktor HttpRedirect.kt
        val fakeMethod = listOf(HttpMethod.Get, HttpMethod.Head)
            .randomElement()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
                if (method in setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                    setBody(
                        listOf("body", TextContent("body", ContentType.Any))
                            .randomElement()
                    )
                }
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeRedirectStatusCode = HttpStatusCode.redirectStatusCodes()
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        var redirected = false
        everySuspend {
            mockRequestHandler.invoke(
                any(),
                any()
            )
        } calls { (scope: MockRequestHandleScope, _: HttpRequestData) ->
            if (!redirected) {
                scope.respond(
                    content = "",
                    status = fakeRedirectStatusCode,
                    headers = Headers.build {
                        set(HttpHeaders.Location, "$fakeUrl/redirected")
                    }
                ).also {
                    redirected = true
                }
            } else {
                scope.respond(
                    content = "",
                    status = fakeStatusCode,
                    headers = Headers.build {
                        if (fakeContextLength != null) {
                            set(HttpHeaders.ContentLength, fakeContextLength.toString())
                        }
                    }
                )
            }
        }

        // When
        val response = runBlocking {
            fakeClient.request(request)
        }

        // Then
        verify(VerifyMode.exhaustiveOrder) {
            // TODO RUM-6456 verify request headers
            mockRumMonitor.startResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                statusCode = fakeRedirectStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = null,
                // seems capture doesn't work yet for verify blocks, so using matching instead
                // see https://github.com/lupuuss/Mokkery/issues/65
                attributes = matching {
                    assertContains(it, RUM_TRACE_ID)
                    assertContains(it, RUM_SPAN_ID)
                    assertContains(it, RUM_RULE_PSR)

                    checkNotNull(it[RUM_TRACE_ID])
                    checkNotNull(it[RUM_SPAN_ID])
                    checkNotNull(it[RUM_RULE_PSR])

                    assertEquals(
                        fakeRumResponseAttributes,
                        it.toMutableMap().apply {
                            remove(RUM_TRACE_ID)
                            remove(RUM_SPAN_ID)
                            remove(RUM_RULE_PSR)
                        }
                    )
                    true
                }
            )

            mockRumMonitor.stopResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                statusCode = fakeStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = fakeContextLength,
                // seems capture doesn't work yet for verify blocks, so using matching instead
                // see https://github.com/lupuuss/Mokkery/issues/65
                attributes = matching {
                    assertContains(it, RUM_TRACE_ID)
                    assertContains(it, RUM_SPAN_ID)
                    assertContains(it, RUM_RULE_PSR)

                    checkNotNull(it[RUM_TRACE_ID])
                    checkNotNull(it[RUM_SPAN_ID])
                    checkNotNull(it[RUM_RULE_PSR])

                    assertEquals(
                        fakeRumResponseAttributes,
                        it.toMutableMap().apply {
                            remove(RUM_TRACE_ID)
                            remove(RUM_SPAN_ID)
                            remove(RUM_RULE_PSR)
                        }
                    )
                    true
                }
            )
        }
    }

    @Test
    fun `M start + stop resource tracking W request succeeded + not sampled for tracing`() {
        // Given
        every { mockTraceSampler.sample() } returns false
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = HttpMethod.DefaultMethods.randomElement()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
                if (method in setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                    setBody(
                        listOf("body", TextContent("body", ContentType.Any))
                            .randomElement()
                    )
                }
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        everySuspend {
            mockRequestHandler.invoke(
                any(),
                any()
            )
        } calls { (scope: MockRequestHandleScope, _: HttpRequestData) ->
            scope.respond(
                content = "",
                status = fakeStatusCode,
                headers = Headers.build {
                    if (fakeContextLength != null) {
                        set(HttpHeaders.ContentLength, fakeContextLength.toString())
                    }
                }
            )
        }

        // When
        val response = runBlocking {
            fakeClient.request(request)
        }

        // Then
        verify {
            // TODO RUM-6456 verify request headers
            mockRumMonitor.startResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                statusCode = fakeStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = fakeContextLength,
                attributes = fakeRumResponseAttributes
            )
        }
    }

    // TODO RUM-7625 Redirect tracking is wrong: first redirect response will stop resource and it won't be any
    //  new `onRequest` call. See ticket for more details.
    @Test
    fun `M start + stop resource tracking W request succeeded + not sampled for tracing + redirect`() {
        // Given
        every { mockTraceSampler.sample() } returns false
        val fakeUrl = "https://$fakeHost/track"
        // Only Get and Head are allowed for implicit redirect, see Ktor HttpRedirect.kt
        val fakeMethod = listOf(HttpMethod.Get, HttpMethod.Head)
            .randomElement()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
                if (method in setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                    setBody(
                        listOf("body", TextContent("body", ContentType.Any))
                            .randomElement()
                    )
                }
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeRedirectStatusCode = HttpStatusCode.redirectStatusCodes()
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        var redirected = false
        everySuspend {
            mockRequestHandler.invoke(
                any(),
                any()
            )
        } calls { (scope: MockRequestHandleScope, _: HttpRequestData) ->
            if (!redirected) {
                scope.respond(
                    content = "",
                    status = fakeRedirectStatusCode,
                    headers = Headers.build {
                        set(HttpHeaders.Location, "$fakeUrl/redirected")
                    }
                ).also {
                    redirected = true
                }
            } else {
                scope.respond(
                    content = "",
                    status = fakeStatusCode,
                    headers = Headers.build {
                        if (fakeContextLength != null) {
                            set(HttpHeaders.ContentLength, fakeContextLength.toString())
                        }
                    }
                )
            }
        }

        // When
        val response = runBlocking {
            fakeClient.request(request)
        }

        // Then
        verify(VerifyMode.exhaustiveOrder) {
            // TODO RUM-6456 verify request headers
            mockRumMonitor.startResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                statusCode = fakeRedirectStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = null,
                attributes = fakeRumResponseAttributes
            )

            mockRumMonitor.stopResource(
                key = response.request.attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR],
                statusCode = fakeStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = fakeContextLength,
                attributes = fakeRumResponseAttributes
            )
        }
    }

    @Test
    fun `M start + stop resource tracking with error W request failed with exception`() {
        // Given
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = HttpMethod.DefaultMethods.randomElement()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
                if (method in setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                    setBody(
                        listOf("body", TextContent("body", ContentType.Any))
                            .randomElement()
                    )
                }
            }
        val fakeThrowable = ConnectTimeoutException(fakeUrl, timeout = randomLong())
        everySuspend {
            mockRequestHandler.invoke(
                any(),
                any()
            )
        } calls { (_: MockRequestHandleScope, _: HttpRequestData) ->
            throw fakeThrowable
        }

        // When
        try {
            runBlocking {
                fakeClient.request(request)
            }
        } catch (t: Throwable) {
            assertEquals(fakeThrowable, t)
        }

        // Then
        verify {
            // TODO RUM-6456 verify request headers
            mockRumMonitor.startResource(
                key = any(),
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            // TODO RUM-6456 verify request headers
            mockRumMonitor.stopResourceWithError(
                key = any(),
                statusCode = null,
                message = "Ktor request error $fakeMethod $fakeUrl",
                throwable = fakeThrowable,
                attributes = fakeRumErrorAttributes
            )
        }
    }

    // endregion

    // region Host matching

    @Test
    fun `M match host W traceHeaderTypesForHost + exact host`() {
        // When
        val traceHeaderTypes = testedPlugin.traceHeaderTypesForHost(fakeHost)

        // Then
        assertEquals(fakeTracingHeaderTypes, traceHeaderTypes)
    }

    @Test
    fun `M match host W traceHeaderTypesForHost + subdomain`() {
        // When
        val traceHeaderTypes = testedPlugin.traceHeaderTypesForHost("sub-a.sub-b.$fakeHost")

        // Then
        assertEquals(fakeTracingHeaderTypes, traceHeaderTypes)
    }

    @Test
    fun `M match host W traceHeaderTypesForHost + match any wildcard`() {
        // Given
        val fakeWildcardTracHeaderTypes = randomEnumValues<TracingHeaderType>()
        val plugin = DatadogKtorPlugin(
            rumMonitor = mockRumMonitor,
            tracedHosts = fakeTracedHosts + mapOf("*" to fakeWildcardTracHeaderTypes),
            traceSampler = mockTraceSampler,
            traceIdGenerator = mockTraceIdGenerator,
            spanIdGenerator = mockSpanIdGenerator,
            rumResourceAttributesProvider = mockRumResourceAttributesProvider
        )

        // When
        val traceHeaderTypes = plugin.traceHeaderTypesForHost("random-domain.com")

        // Then
        assertEquals(fakeWildcardTracHeaderTypes, traceHeaderTypes)
    }

    // endregion

    // region private

    // not the same as in production files
    private fun HttpMethod.asRumMethod() =
        RumResourceMethod.entries.firstOrNull { it.name == value } ?: RumResourceMethod.CONNECT

    // see Ktor HttpRedirect.kt
    private fun HttpStatusCode.Companion.redirectStatusCodes() = listOf(
        MovedPermanently,
        Found,
        SeeOther,
        TemporaryRedirect,
        PermanentRedirect
    )

    // endregion
}
