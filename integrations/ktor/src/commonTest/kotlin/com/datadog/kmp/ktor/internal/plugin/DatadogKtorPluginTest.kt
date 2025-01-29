/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.plugin

import com.datadog.kmp.ktor.HEX_RADIX
import com.datadog.kmp.ktor.RUM_RULE_PSR
import com.datadog.kmp.ktor.RUM_SPAN_ID
import com.datadog.kmp.ktor.RUM_TRACE_ID
import com.datadog.kmp.ktor.RumResourceAttributesProvider
import com.datadog.kmp.ktor.TracingHeaderType
import com.datadog.kmp.ktor.internal.sampling.Sampler
import com.datadog.kmp.ktor.internal.trace.SpanId
import com.datadog.kmp.ktor.internal.trace.SpanIdGenerator
import com.datadog.kmp.ktor.internal.trace.TraceId
import com.datadog.kmp.ktor.internal.trace.TraceIdGenerator
import com.datadog.kmp.ktor.test.HeadersAssert.Companion.assertThat
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import com.datadog.tools.random.exhaustiveAttributes
import com.datadog.tools.random.nullable
import com.datadog.tools.random.randomBoolean
import com.datadog.tools.random.randomElement
import com.datadog.tools.random.randomEnumValues
import com.datadog.tools.random.randomFloat
import com.datadog.tools.random.randomLong
import com.datadog.tools.random.randomULong
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsBy
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.collections.isIn
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
import io.ktor.client.request.HttpRequest
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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DatadogKtorPluginTest {

    private val fakeHost = "datadoghq.com"
    private val fakeTracingHeaderTypes = randomEnumValues<TracingHeaderType>()
    private val mockRumMonitor = mock<RumMonitor>()
    private val fakeTracedHosts = mapOf(fakeHost to fakeTracingHeaderTypes)
    private val mockTraceSampler = mock<Sampler<TraceId>>()
    private val mockTraceIdGenerator = mock<TraceIdGenerator>()
    private val mockSpanIdGenerator = mock<SpanIdGenerator>()
    private val mockRumResourceAttributesProvider = mock<RumResourceAttributesProvider>()

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
    private val mockEngine = MockEngine(
        MockEngineConfig().apply {
            addHandler(mockRequestHandler)
        }
    )

    // some classes of Ktor request-response chain cannot be mocked, because either they are final or some members
    // are final, so using mock engine instead
    private val fakeClient = HttpClient(mockEngine) {
        install(testedPlugin.buildClientPlugin())
    }

    @BeforeTest
    fun `set up`() {
        every { mockTraceSampler.sample(any()) } returns true
        every { mockTraceSampler.sampleRate } returns 100f
        every { mockTraceIdGenerator.generateTraceId() } returnsBy {
            TraceId(
                high = randomULong(),
                low = randomULong()
            )
        }
        every { mockSpanIdGenerator.generateSpanId() } returnsBy { SpanId(randomULong()) }
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
                setRandomBody()
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        everySuspend {
            mockRequestHandler.invoke(any(), any())
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
        val capturedRequestHeaders = mockEngine.requestHistory.map { it.headers }
        verify(VerifyMode.exhaustiveOrder) {
            mockRumMonitor.startResource(
                key = response.request.requestId,
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = response.request.requestId,
                statusCode = fakeStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = fakeContextLength,
                // seems capture doesn't work yet for verify blocks, so using matching instead
                // see https://github.com/lupuuss/Mokkery/issues/65
                attributes = matching {
                    assertContains(it, RUM_TRACE_ID)
                    assertContains(it, RUM_SPAN_ID)
                    assertContains(it, RUM_RULE_PSR)

                    val traceId = checkNotNull(it[RUM_TRACE_ID] as? String)
                    val spanId = checkNotNull(it[RUM_SPAN_ID] as? String)
                    val rulePsr = checkNotNull(it[RUM_RULE_PSR] as? Float)

                    assertEquals(1f, rulePsr)
                    assertThat(capturedRequestHeaders.first())
                        .apply {
                            fakeTracingHeaderTypes.forEach {
                                hasTraceId(expectedTraceId(traceId, it), it)
                                hasSpanId(expectedSpanId(spanId, it), it)
                                hasSamplingDecision(1, it)
                            }
                        }

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

    @Test
    fun `M start + stop resource tracking W request succeeded + sampled for tracing + redirect`() {
        // Given
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = randomRedirectMethod()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeRedirectStatusCode = HttpStatusCode.redirectStatusCodes()
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        everySuspend {
            mockRequestHandler.invoke(any(), any())
        } calls { (scope: MockRequestHandleScope, request: HttpRequestData) ->
            if (!request.url.encodedPath.endsWith("/redirected")) {
                scope.respond(
                    content = "",
                    status = fakeRedirectStatusCode,
                    headers = Headers.build {
                        set(HttpHeaders.Location, "$fakeUrl/redirected")
                    }
                )
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
        runBlocking {
            fakeClient.request(request)
        }

        // Then
        val recordedRequests = mockEngine.requestHistory
        val recordedResponses = mockEngine.responseHistory
        assertEquals(2, recordedRequests.size)
        assertEquals(2, recordedResponses.size)
        val firstRequestId = recordedRequests[0].requestId
        val redirectRequestId = recordedRequests[1].requestId
        assertNotEquals(firstRequestId, redirectRequestId)
        val capturedRequestHeaders = recordedRequests.map { it.headers }
        assertEquals(recordedRequests[0].traceId, recordedRequests[1].traceId)
        assertNotEquals(recordedRequests[0].spanId, recordedRequests[1].spanId)

        verify(VerifyMode.exhaustiveOrder) {
            mockRumMonitor.startResource(
                key = firstRequestId,
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = firstRequestId,
                statusCode = fakeRedirectStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = null,
                // seems capture doesn't work yet for verify blocks, so using matching instead
                // see https://github.com/lupuuss/Mokkery/issues/65
                attributes = matching {
                    assertContains(it, RUM_TRACE_ID)
                    assertContains(it, RUM_SPAN_ID)
                    assertContains(it, RUM_RULE_PSR)

                    val traceId = checkNotNull(it[RUM_TRACE_ID] as? String)
                    val spanId = checkNotNull(it[RUM_SPAN_ID] as? String)
                    val rulePsr = checkNotNull(it[RUM_RULE_PSR])

                    assertEquals(1f, rulePsr)
                    assertThat(capturedRequestHeaders.first())
                        .apply {
                            fakeTracingHeaderTypes.forEach {
                                hasTraceId(expectedTraceId(traceId, it), it)
                                hasSpanId(expectedSpanId(spanId, it), it)
                                hasSamplingDecision(1, it)
                            }
                        }

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

            mockRumMonitor.startResource(
                key = redirectRequestId,
                method = fakeMethod.asRumMethod(),
                url = "$fakeUrl/redirected",
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = redirectRequestId,
                statusCode = fakeStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = fakeContextLength,
                // seems capture doesn't work yet for verify blocks, so using matching instead
                // see https://github.com/lupuuss/Mokkery/issues/65
                attributes = matching {
                    assertContains(it, RUM_TRACE_ID)
                    assertContains(it, RUM_SPAN_ID)
                    assertContains(it, RUM_RULE_PSR)

                    val traceId = checkNotNull(it[RUM_TRACE_ID] as? String)
                    val spanId = checkNotNull(it[RUM_SPAN_ID] as? String)
                    val rulePsr = checkNotNull(it[RUM_RULE_PSR])

                    assertEquals(1f, rulePsr)
                    assertThat(capturedRequestHeaders[1])
                        .apply {
                            fakeTracingHeaderTypes.forEach {
                                hasTraceId(expectedTraceId(traceId, it), it)
                                hasSpanId(expectedSpanId(spanId, it), it)
                                hasSamplingDecision(1, it)
                            }
                        }

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

    @Test
    fun `M start + stop resource tracking W request succeeded + not sampled for tracing`() {
        // Given
        every { mockTraceSampler.sample(any()) } returns false
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = HttpMethod.DefaultMethods.randomElement()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
                setRandomBody()
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        everySuspend {
            mockRequestHandler.invoke(any(), any())
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
            mockRumMonitor.startResource(
                key = response.request.requestId,
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = response.request.requestId,
                statusCode = fakeStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = fakeContextLength,
                attributes = fakeRumResponseAttributes
            )
        }

        verifyNoMoreCalls(mockRumMonitor)

        assertThat(mockEngine.requestHistory.first().headers)
            .apply {
                fakeTracingHeaderTypes.forEach {
                    hasSamplingDecision(0, it)
                }
            }
    }

    @Test
    fun `M start + stop resource tracking W request succeeded + not sampled for tracing + redirect`() {
        // Given
        every { mockTraceSampler.sample(any()) } returns false
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = randomRedirectMethod()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeRedirectStatusCode = HttpStatusCode.redirectStatusCodes()
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        everySuspend {
            mockRequestHandler.invoke(any(), any())
        } calls { (scope: MockRequestHandleScope, request: HttpRequestData) ->
            if (!request.url.encodedPath.endsWith("/redirected")) {
                scope.respond(
                    content = "",
                    status = fakeRedirectStatusCode,
                    headers = Headers.build {
                        set(HttpHeaders.Location, "$fakeUrl/redirected")
                    }
                )
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
        runBlocking {
            fakeClient.request(request)
        }

        // Then
        val recordedRequests = mockEngine.requestHistory
        val recordedResponses = mockEngine.responseHistory
        assertEquals(2, recordedRequests.size)
        assertEquals(2, recordedResponses.size)
        val firstRequestId = recordedRequests[0].requestId
        val redirectRequestId = recordedRequests[1].requestId
        assertNotEquals(firstRequestId, redirectRequestId)
        val capturedRequestHeaders = recordedRequests.map { it.headers }

        verify(VerifyMode.exhaustiveOrder) {
            mockRumMonitor.startResource(
                key = firstRequestId,
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = firstRequestId,
                statusCode = fakeRedirectStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = null,
                attributes = fakeRumResponseAttributes
            )

            mockRumMonitor.startResource(
                key = redirectRequestId,
                method = fakeMethod.asRumMethod(),
                url = "$fakeUrl/redirected",
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = redirectRequestId,
                statusCode = fakeStatusCode.value,
                kind = RumResourceKind.NATIVE,
                size = fakeContextLength,
                attributes = fakeRumResponseAttributes
            )
        }

        verifyNoMoreCalls(mockRumMonitor)

        capturedRequestHeaders.forEach {
            assertThat(it)
                .apply {
                    fakeTracingHeaderTypes.forEach {
                        hasSamplingDecision(0, it)
                    }
                }
        }
    }

    @Test
    fun `M carry sampling decision W request is made`() {
        // Given
        every { mockTraceSampler.sample(any()) } returnsBy { randomBoolean() }
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = randomRedirectMethod()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeRedirectStatusCode = HttpStatusCode.redirectStatusCodes()
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        everySuspend {
            mockRequestHandler.invoke(any(), any())
        } calls { (scope: MockRequestHandleScope, request: HttpRequestData) ->
            if (!request.url.encodedPath.endsWith("/redirected")) {
                scope.respond(
                    content = "",
                    status = fakeRedirectStatusCode,
                    headers = Headers.build {
                        set(HttpHeaders.Location, "$fakeUrl/redirected")
                    }
                )
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
        runBlocking {
            fakeClient.request(request)
        }

        // Then
        val requests = mockEngine.requestHistory
        assertEquals(
            requests.first().attributes[DatadogKtorPlugin.DD_IS_SAMPLED_ATTR],
            requests[1].attributes[DatadogKtorPlugin.DD_IS_SAMPLED_ATTR]
        )
    }

    @Test
    fun `M compute rule_psr W request is made`() {
        // Given
        val fakeSampleRate = randomFloat(from = 40f, until = 100f)
        every { mockTraceSampler.sampleRate } returns fakeSampleRate
        every { mockTraceSampler.sample(any()) } returnsBy { randomFloat(0f, 100f) < fakeSampleRate }
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = HttpMethod.DefaultMethods.randomElement()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
                setRandomBody()
            }

        val fakeStatusCode = HttpStatusCode.allStatusCodes
            .filter { it.value !in 300..399 }
            .randomElement()
        val fakeContextLength = nullable(randomLong())
        everySuspend {
            mockRequestHandler.invoke(any(), any())
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
        repeat(10) {
            runBlocking {
                fakeClient.request(request)
            }
        }

        // Then
        val capturedRulePsrValues = mutableListOf<Float>()
        verify(VerifyMode.exactly(10)) {
            mockRumMonitor.stopResource(
                key = isIn(mockEngine.requestHistory.map { it.requestId }),
                statusCode = fakeStatusCode.value,
                size = fakeContextLength,
                kind = RumResourceKind.NATIVE,
                attributes = matching {
                    val rulePsr = it[RUM_RULE_PSR] as? Float
                    if (rulePsr != null) {
                        capturedRulePsrValues += rulePsr
                    }

                    true
                }
            )
        }

        assertTrue(capturedRulePsrValues.isNotEmpty())
        capturedRulePsrValues.forEach { assertIsWithin(it, 0f, 1f) }
        assertTrue(capturedRulePsrValues.all { it == fakeSampleRate / 100f })
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
                setRandomBody()
            }
        val fakeThrowable = ConnectTimeoutException(fakeUrl, timeout = randomLong())
        var capturedRequestId: String? = null
        everySuspend {
            mockRequestHandler.invoke(any(), any())
        } calls { (_: MockRequestHandleScope, request: HttpRequestData) ->
            capturedRequestId = request.requestId
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
        verify(VerifyMode.exhaustiveOrder) {
            mockRumMonitor.startResource(
                key = checkNotNull(capturedRequestId),
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResourceWithError(
                key = checkNotNull(capturedRequestId),
                statusCode = null,
                message = "Ktor request error $fakeMethod $fakeUrl",
                throwable = fakeThrowable,
                attributes = fakeRumErrorAttributes
            )
        }

        verifyNoMoreCalls(mockRumMonitor)
    }

    @Test
    fun `M start + stop resource tracking with error W request failed with exception on redirect`() {
        // Given
        val fakeUrl = "https://$fakeHost/track"
        val fakeMethod = randomRedirectMethod()
        val request = HttpRequestBuilder()
            .apply {
                url(fakeUrl)
                headers["fake-header-name"] = "fake-header-value"
                method = fakeMethod
            }

        val fakeRedirectStatusCode = HttpStatusCode.redirectStatusCodes()
            .randomElement()
        val fakeThrowable = ConnectTimeoutException(fakeUrl, timeout = randomLong())
        var capturedRedirectRequestId: String? = null
        everySuspend {
            mockRequestHandler.invoke(any(), any())
        } calls { (scope: MockRequestHandleScope, request: HttpRequestData) ->
            if (!request.url.encodedPath.endsWith("/redirected")) {
                scope.respond(
                    content = "",
                    status = fakeRedirectStatusCode,
                    headers = Headers.build {
                        set(HttpHeaders.Location, "$fakeUrl/redirected")
                    }
                )
            } else {
                capturedRedirectRequestId = request.requestId
                throw fakeThrowable
            }
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
        val firstRequestId = mockEngine.requestHistory.first().requestId
        assertNotEquals(capturedRedirectRequestId, firstRequestId)

        verify {
            mockRumMonitor.startResource(
                key = firstRequestId,
                method = fakeMethod.asRumMethod(),
                url = fakeUrl,
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResource(
                key = firstRequestId,
                statusCode = fakeRedirectStatusCode.value,
                size = null,
                kind = RumResourceKind.NATIVE,
                attributes = any()
            )

            mockRumMonitor.startResource(
                key = checkNotNull(capturedRedirectRequestId),
                method = fakeMethod.asRumMethod(),
                url = "$fakeUrl/redirected",
                attributes = fakeRumRequestAttributes
            )

            mockRumMonitor.stopResourceWithError(
                key = checkNotNull(capturedRedirectRequestId),
                statusCode = null,
                message = "Ktor request error $fakeMethod $fakeUrl/redirected",
                throwable = fakeThrowable,
                attributes = fakeRumErrorAttributes
            )
        }

        verifyNoMoreCalls(mockRumMonitor)
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

    private fun HttpRequestBuilder.setRandomBody() {
        if (method in setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
            setBody(
                listOf("body", TextContent("body", ContentType.Any))
                    .randomElement()
            )
        }
    }

    private val HttpRequestData.requestId: String
        get() = attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR]

    private val HttpRequestData.traceId: TraceId
        get() = attributes[DatadogKtorPlugin.DD_TRACE_ID_ATTR]

    private val HttpRequestData.spanId: SpanId
        get() = attributes[DatadogKtorPlugin.DD_SPAN_ID_ATTR]

    private val HttpRequest.requestId: String
        get() = attributes[DatadogKtorPlugin.DD_REQUEST_ID_ATTR]

    private fun expectedSpanId(spanIdDec: String, headerType: TracingHeaderType): String {
        return when (headerType) {
            TracingHeaderType.DATADOG -> spanIdDec
            else -> spanIdDec.toULong().toString(HEX_RADIX).lowercase().let {
                if (headerType == TracingHeaderType.TRACECONTEXT) {
                    it.padStart(16, '0')
                } else {
                    it
                }
            }
        }
    }

    private fun expectedTraceId(traceIdHex: String, headerType: TracingHeaderType): String {
        return when (headerType) {
            TracingHeaderType.TRACECONTEXT -> traceIdHex.padStart(32, '0')
            else -> traceIdHex
        }
    }

    private fun randomRedirectMethod(): HttpMethod {
        // Only Get and Head are allowed for implicit redirect, see Ktor HttpRedirect.kt
        return listOf(HttpMethod.Get, HttpMethod.Head)
            .randomElement()
    }

    private fun assertIsWithin(actual: Float, from: Float, until: Float) {
        assertTrue(actual >= from, "Expected $actual to be greater or equal $from, but it wasn't.")
        assertTrue(actual <= until, "Expected $actual to be less or equal $until, but it wasn't.")
    }

    // endregion
}
