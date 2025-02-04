/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.plugin

import io.ktor.client.plugins.api.OnRequestContext
import io.ktor.client.plugins.api.OnResponseContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.content.OutgoingContent

internal interface KtorPlugin {

    val pluginName: String

    // user-initiated request: doesn't include HTTP client-initiated requests (e.g. redirect) and context here is
    // not yet transformed
    fun onRequest(
        onRequestContext: OnRequestContext,
        request: HttpRequestBuilder,
        content: Any
    )

    // will be called on any request (both user-initiated and ktor-initiated)
    fun onSend(
        request: HttpRequestBuilder,
        content: OutgoingContent
    )

    // will be called on any response (both user-initiated and ktor-initiated)
    fun onResponse(
        onResponseContext: OnResponseContext,
        response: HttpResponse
    )

    fun onError(
        request: HttpRequestBuilder,
        throwable: Throwable
    )
}
