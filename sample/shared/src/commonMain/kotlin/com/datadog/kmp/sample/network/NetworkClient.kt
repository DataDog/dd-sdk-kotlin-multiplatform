/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.network

import com.datadog.kmp.ktor.TracingHeaderType
import com.datadog.kmp.ktor.datadogKtorPlugin
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

@Suppress("TooGenericExceptionCaught")
object NetworkClient {

    private val client = HttpClient {
        followRedirects = true
        install(
            datadogKtorPlugin(
                tracedHosts = mapOf(
                    "httpbin.org" to setOf(TracingHeaderType.DATADOG)
                ),
                traceSamplingRate = 100f
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
                val response = client.get(url)
                if (response.status.value >= HttpStatusCode.BadRequest.value) {
                    onFailure(response.status.value, null)
                } else {
                    val responseBody = response.bodyAsText()
                    onSuccess(responseBody)
                }
            } catch (e: Exception) {
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
                }
                if (response.status.value >= HttpStatusCode.BadRequest.value) {
                    onFailure(response.status.value, null)
                } else {
                    val responseBody = response.bodyAsText()
                    onSuccess(responseBody)
                }
            } catch (e: Exception) {
                onFailure(0, e)
            }
        }
    }
}
