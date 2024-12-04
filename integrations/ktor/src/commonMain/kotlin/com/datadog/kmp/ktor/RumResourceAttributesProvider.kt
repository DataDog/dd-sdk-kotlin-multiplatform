/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.statement.HttpResponse

/**
 * Provider which listens for the Ktor [HttpRequestData] -> [HttpResponse] (or [Throwable]) chain and
 * offers a possibility to add custom attributes to the RUM Resource event.
 */
interface RumResourceAttributesProvider {

    /**
     * Offers a possibility to provide custom attributes at the request creation stage, which later will be attached
     * the RUM resource event associated with the request.
     * @param request the intercepted [HttpRequestData]
     */
    fun onRequest(request: HttpRequestData): Map<String, Any?>

    /**
     * Offers a possibility to create custom attributes at the response receive stage, which later will be attached to
     * the RUM resource event associated with the request.
     * @param response the [HttpResponse] response
     */
    fun onResponse(response: HttpResponse): Map<String, Any?>

    /**
     * Offers a possibility to create custom attributes at the error receive stage, which later will be attached to
     * the RUM resource event associated with the request.
     * @param request the intercepted [HttpRequestData]
     * @param throwable in case an error occurred during the [HttpRequest]
     */
    fun onError(request: HttpRequestData, throwable: Throwable): Map<String, Any?>
}

internal object DefaultRumResourceAttributesProvider : RumResourceAttributesProvider {
    override fun onRequest(request: HttpRequestData) = emptyMap<String, Any?>()
    override fun onResponse(response: HttpResponse) = emptyMap<String, Any?>()
    override fun onError(request: HttpRequestData, throwable: Throwable) = emptyMap<String, Any?>()
}
