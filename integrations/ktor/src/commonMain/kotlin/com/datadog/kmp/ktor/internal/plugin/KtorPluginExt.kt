/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.plugin

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin

internal fun KtorPlugin.buildClientPlugin(): ClientPlugin<Unit> {
    val plugin = this

    // Ktor pipeline is Before -> State -> Transform -> Render -> Send. See [HttpRequestPipeline.Phases].
    return createClientPlugin(pluginName) {
        onRequest { request, content ->
            plugin.onRequest(this, request, content)
        }

        on(Send) {
            try {
                proceed(it)
            } catch (@Suppress("TooGenericExceptionCaught") t: Throwable) {
                plugin.onError(it, t)
                throw t
            }
        }

        on(SendingRequest) { request, content ->
            plugin.onSend(request = request, content = content)
        }

        onResponse { response ->
            plugin.onResponse(onResponseContext = this, response = response)
        }
    }
}
