/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor

import com.benasher44.uuid.uuid4
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.util.AttributeKey

internal const val PLUGIN_NAME = "Datadog"
internal const val DD_REQUEST_ID = "X-Datadog-Request-ID"
internal val DD_REQUEST_ID_ATTR = AttributeKey<String>(DD_REQUEST_ID)

fun datadogKtorPlugin(): ClientPlugin<Unit> {
    return createClientPlugin(PLUGIN_NAME) {
        // TODO RUM-5228 report request timings (DNS, SSL, …)
        // TODO RUM-5229 report request exceptions

        onRequest { request, _ ->

            val requestId = uuid4().toString()
            request.attributes.put(DD_REQUEST_ID_ATTR, requestId)
            RumMonitor.get().startResource(
                key = requestId,
                method = request.method.asRumMethod(),
                url = request.url.buildString(),
                attributes = emptyMap()
            )
        }

        onResponse { response ->
            val requestId = response.request.attributes.getOrNull(DD_REQUEST_ID_ATTR)
            if (requestId != null) {
                RumMonitor.get().stopResource(
                    key = requestId,
                    statusCode = response.status.value,
                    size = null, // TODO RUM-5233 report request size
                    kind = RumResourceKind.NATIVE,
                    attributes = emptyMap()
                )
            } else {
                // TODO RUM-5254 handle missing request id case
            }
        }
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
