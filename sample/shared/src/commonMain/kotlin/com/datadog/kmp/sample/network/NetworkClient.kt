/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.network

import com.datadog.kmp.ktor.HttpRequestSnapshot
import com.datadog.kmp.ktor.RumResourceAttributesProvider
import com.datadog.kmp.ktor.datadogKtorPlugin
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object NetworkClient {

    private const val CUSTOM_HEADER_NAME = "x-custom-header"
    private const val HTTPBIN_HOST = "httpbin.org"

    private val client = HttpClient {
        followRedirects = true
        install(
            datadogKtorPlugin(
                tracedHosts = listOf(HTTPBIN_HOST),
                traceSampleRate = 100f,
                rumResourceAttributesProvider = object : RumResourceAttributesProvider {
                    override fun onRequest(request: HttpRequestSnapshot) =
                        mapOf("custom-header-value" to request.headers[CUSTOM_HEADER_NAME])

                    override fun onResponse(response: HttpResponse) =
                        mapOf("http-protocol-version" to response.version.toString())

                    override fun onError(request: HttpRequestSnapshot, throwable: Throwable) =
                        mapOf("custom-header-value" to request.headers[CUSTOM_HEADER_NAME])
                }
            )
        )
    }

    suspend fun get(
        url: String,
        onSuccess: (String) -> Unit,
        onFailure: (Int, Throwable?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.get(url) {
                    headers[CUSTOM_HEADER_NAME] = "some-get-value"
                }
                if (response.status.value >= HttpStatusCode.BadRequest.value) {
                    onFailure(response.status.value, null)
                } else {
                    val responseBody = response.bodyAsText()
                    onSuccess(responseBody)
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                onFailure(0, e)
            }
        }
    }

    suspend fun post(
        url: String,
        payload: String,
        onSuccess: (String) -> Unit,
        onFailure: (Int, Throwable?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.post(url) {
                    setBody(payload)
                    headers[CUSTOM_HEADER_NAME] = "some-post-value"
                }
                if (response.status.value >= HttpStatusCode.BadRequest.value) {
                    onFailure(response.status.value, null)
                } else {
                    val responseBody = response.bodyAsText()
                    onSuccess(responseBody)
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                onFailure(0, e)
            }
        }
    }
}
