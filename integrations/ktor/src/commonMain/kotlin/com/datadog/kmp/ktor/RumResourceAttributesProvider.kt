/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.clone
import io.ktor.util.Attributes
import io.ktor.util.putAll

/**
 * Provider which listens for the Ktor [HttpRequestSnapshot] -> [HttpResponse] (or [Throwable]) chain and
 * offers a possibility to add custom attributes to the RUM Resource event.
 */
interface RumResourceAttributesProvider {

    /**
     * Offers a possibility to provide custom attributes at the request creation stage, which later will be attached
     * the RUM resource event associated with the request.
     * @param request the intercepted [HttpRequestSnapshot]
     */
    fun onRequest(request: HttpRequestSnapshot): Map<String, Any?>

    /**
     * Offers a possibility to create custom attributes at the response receive stage, which later will be attached to
     * the RUM resource event associated with the request.
     * @param response the [HttpResponse] response
     */
    fun onResponse(response: HttpResponse): Map<String, Any?>

    /**
     * Offers a possibility to create custom attributes at the error receive stage, which later will be attached to
     * the RUM resource event associated with the request.
     * @param request the intercepted [HttpRequestSnapshot]
     * @param throwable in case an error occurred during the [HttpRequest]
     */
    fun onError(request: HttpRequestSnapshot, throwable: Throwable): Map<String, Any?>
}

/**
 * Represents an immutable snapshot of the request to be executed.
 *
 * @param url the [Url] to be called.
 * @param method the [HttpMethod] to be used.
 * @param headers the [Headers] to be used.
 * @param body the body of the request (if any). Note: it is a raw body, before any content transformation is applied.
 * @param attributes the [Attributes] of the request.
 */
class HttpRequestSnapshot internal constructor(
    val url: Url,
    val method: HttpMethod,
    val headers: Headers,
    val body: Any,
    val attributes: Attributes
) {
    internal companion object {
        fun takeFrom(builder: HttpRequestBuilder) = HttpRequestSnapshot(
            // build() mutates the builder, so using clone to have a separate copy
            url = builder.url.clone().build(),
            method = builder.method,
            headers = builder.headers.build(),
            body = builder.body,
            attributes = Attributes().apply { putAll(builder.attributes) }
        )
    }
}

internal object DefaultRumResourceAttributesProvider : RumResourceAttributesProvider {
    override fun onRequest(request: HttpRequestSnapshot) = emptyMap<String, Any?>()
    override fun onResponse(response: HttpResponse) = emptyMap<String, Any?>()
    override fun onError(request: HttpRequestSnapshot, throwable: Throwable) = emptyMap<String, Any?>()
}
