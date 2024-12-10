/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.ktor.internal.plugin

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin

internal fun KtorPlugin.buildClientPlugin(): ClientPlugin<Unit> {
    val plugin = this

    // Ktor pipeline is Before -> State -> Transform -> Render -> Send. See [HttpRequestPipeline.Phases].
    return createClientPlugin(pluginName) {
        // executed at the Send pipeline stage
        on(Send) {
            try {
                proceed(it)
            } catch (@Suppress("TooGenericExceptionCaught") t: Throwable) {
                plugin.onError(it, t)
                throw t
            }
        }

        // executed at the State pipeline stage. Request body is not yet transformed to OutgoingContent instance
        // (it is done at the Render stage)
        //
        // Maybe we should call this in on(Send) instead? There request is finalized and unlikely be modified after.
        // The only problem is that in on(Send) request payload will be already serialized, which makes it more
        // difficult to capture attributes from it, and there won't be any access to the original payload.
        onRequest { request, content ->
            plugin.onRequest(onRequestContext = this, request = request, content = content)
        }

        onResponse { response ->
            plugin.onResponse(onResponseContext = this, response = response)
        }
    }
}
