/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.internal

import com.datadog.android.api.InternalLogger
import com.datadog.android.core.InternalSdkCore
import okhttp3.Authenticator
import okhttp3.Challenge
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection
import com.datadog.android.Datadog as NativeDatadog

internal class BasicProxyAuthenticator(
    private val username: String,
    private val password: String,
    private val internalLoggerProvider: () -> InternalLogger = {
        (NativeDatadog.getInstance() as? InternalSdkCore)?.internalLogger ?: InternalLogger.UNBOUND
    }
) : Authenticator {

    @Suppress("ReturnCount")
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != HttpURLConnection.HTTP_PROXY_AUTH) {
            internalLoggerProvider().log(
                InternalLogger.Level.DEBUG,
                InternalLogger.Target.USER,
                {
                    "Proxy authenticator is invoked with unsupported code ${response.code}," +
                        " expected ${HttpURLConnection.HTTP_PROXY_AUTH}. Ignoring authentication request."
                }
            )
            return null
        }

        for (challenge in response.challenges()) {
            if (!isSupportedChallenge(challenge)) {
                continue
            }

            val credential = Credentials.basic(username, password, challenge.charset)
            return response.request.newBuilder()
                .header("Proxy-Authorization", credential)
                .build()
        }

        internalLoggerProvider().log(
            InternalLogger.Level.DEBUG,
            InternalLogger.Target.USER,
            { "No supported proxy authentication schemes found: ${response.challenges().map { it.scheme }}" }
        )

        return null
    }

    private fun isSupportedChallenge(challenge: Challenge) =
        listOf("Basic", "OkHttp-Preemptive").any { it.equals(challenge.scheme, ignoreCase = true) }
}
