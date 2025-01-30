/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.plugin

import com.benasher44.uuid.uuid4
import com.datadog.kmp.ktor.HttpRequestSnapshot
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
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import io.ktor.client.plugins.api.OnRequestContext
import io.ktor.client.plugins.api.OnResponseContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentLength
import io.ktor.util.AttributeKey

internal class DatadogKtorPlugin(
    private val rumMonitor: RumMonitor,
    private val tracedHosts: Map<String, Set<TracingHeaderType>>,
    private val traceSampler: Sampler<TraceId>,
    private val traceIdGenerator: TraceIdGenerator,
    private val spanIdGenerator: SpanIdGenerator,
    private val rumResourceAttributesProvider: RumResourceAttributesProvider
) : KtorPlugin {

    override val pluginName: String = PLUGIN_NAME

    override fun onRequest(onRequestContext: OnRequestContext, request: HttpRequestBuilder, content: Any) {
        request.attributes.put(DD_ORIGINAL_BODY_ATTR, content)
    }

    override fun onSend(request: HttpRequestBuilder, content: OutgoingContent) {
        val traceId = request.attributes.getOrNull(DD_TRACE_ID_ATTR)
            ?: traceIdGenerator.generateTraceId()
        val isSampledIn = request.attributes.getOrNull(DD_IS_SAMPLED_ATTR) ?: traceSampler.sample(traceId)
        val traceHeaderTypes = traceHeaderTypesForHost(request.url.host)
        val spanId = spanIdGenerator.generateSpanId()

        if (isSampledIn && !traceHeaderTypes.isNullOrEmpty()) {
            traceHeaderTypes.forEach { headerType ->
                headerType.injectHeaders(request, true, traceId, spanId)
            }
            request.attributes.put(DD_TRACE_ID_ATTR, traceId)
            request.attributes.put(DD_SPAN_ID_ATTR, spanId)
            request.attributes.put(DD_RULE_PSR_ATTR, traceSampler.sampleRate / ALL_IN_SAMPLE_RATE)
        } else {
            TracingHeaderType.entries.forEach { headerType ->
                headerType.injectHeaders(request, false, traceId, spanId)
            }
        }

        val requestId = uuid4().toString()
        request.attributes.put(DD_REQUEST_ID_ATTR, requestId)
        request.attributes.put(DD_IS_SAMPLED_ATTR, isSampledIn)
        rumMonitor.startResource(
            key = requestId,
            method = request.method.asRumMethod(),
            url = request.url.buildString(),
            attributes = rumResourceAttributesProvider.onRequest(HttpRequestSnapshot.takeFrom(request))
        )
    }

    override fun onResponse(onResponseContext: OnResponseContext, response: HttpResponse) {
        val requestAttributes = response.request.attributes
        val requestId = requestAttributes.getOrNull(DD_REQUEST_ID_ATTR)
        if (requestId != null) {
            val tracingAttributes = mutableMapOf<String, Any?>().apply {
                val traceId = requestAttributes.getOrNull(DD_TRACE_ID_ATTR)
                val spanId = requestAttributes.getOrNull(DD_SPAN_ID_ATTR)
                val rulePsr = requestAttributes.getOrNull(DD_RULE_PSR_ATTR)
                if (traceId != null && spanId != null && rulePsr != null) {
                    put(RUM_TRACE_ID, traceId.toHexString())
                    put(RUM_SPAN_ID, spanId.raw.toString())
                    put(RUM_RULE_PSR, rulePsr)
                }
            }
            rumMonitor.stopResource(
                key = requestId,
                statusCode = response.status.value,
                size = response.contentLength(), // TODO RUM-6382 Report content size if header is missing
                kind = RumResourceKind.NATIVE,
                attributes = rumResourceAttributesProvider.onResponse(response) + tracingAttributes
            )
        } else {
            // TODO RUM-5254 handle missing request id case
        }
    }

    override fun onError(request: HttpRequestBuilder, throwable: Throwable) {
        val requestId = request.attributes.getOrNull(DD_REQUEST_ID_ATTR)
        if (requestId != null) {
            val method = request.method
            val url = request.url.toString()
            rumMonitor.stopResourceWithError(
                key = requestId,
                statusCode = null,
                message = "Ktor request error $method $url",
                throwable = throwable,
                attributes = rumResourceAttributesProvider.onError(
                    HttpRequestSnapshot.takeFrom(request),
                    throwable
                )
            )
        } else {
            // TODO RUM-5254 handle missing request id case
        }
    }

    internal fun traceHeaderTypesForHost(host: String): Set<TracingHeaderType>? {
        return tracedHosts.getOrElse(host) {
            tracedHosts.entries.firstOrNull { host.endsWith(".${it.key}") }?.value ?: tracedHosts["*"]
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

    companion object {

        private const val ALL_IN_SAMPLE_RATE = 100f

        internal const val PLUGIN_NAME = "Datadog"
        internal const val DD_REQUEST_ID = "X-Datadog-Request-ID"
        internal const val DD_IS_SAMPLED = "X-Datadog-Sampled"
        internal const val DD_ORIGINAL_BODY = "X-Datadog-Original-Body"
        internal val DD_REQUEST_ID_ATTR = AttributeKey<String>(DD_REQUEST_ID)
        internal val DD_TRACE_ID_ATTR = AttributeKey<TraceId>(RUM_TRACE_ID)
        internal val DD_SPAN_ID_ATTR = AttributeKey<SpanId>(RUM_SPAN_ID)
        internal val DD_RULE_PSR_ATTR = AttributeKey<Float>(RUM_RULE_PSR)
        internal val DD_IS_SAMPLED_ATTR = AttributeKey<Boolean>(DD_IS_SAMPLED)
        internal val DD_ORIGINAL_BODY_ATTR = AttributeKey<Any>(DD_ORIGINAL_BODY)
    }
}
