/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

import com.datadog.android.api.InternalLogger
import com.datadog.android.core.configuration.Configuration
import com.datadog.kmp.internal.BasicProxyAuthenticator
import java.net.InetSocketAddress
import java.net.Proxy

internal fun Configuration.Builder.setProxy(
    proxyConfiguration: ProxyConfiguration?
): Configuration.Builder {
    if (proxyConfiguration != null) {
        val proxyType = when (proxyConfiguration.type) {
            ProxyType.HTTP -> Proxy.Type.HTTP
            ProxyType.SOCKS -> Proxy.Type.SOCKS
        }
        val proxy = Proxy(proxyType, InetSocketAddress(proxyConfiguration.hostname, proxyConfiguration.port.toInt()))
        val username = proxyConfiguration.username
        val password = proxyConfiguration.password
        val authenticator = if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            BasicProxyAuthenticator(username, password)
        } else {
            InternalLogger.UNBOUND.log(
                level = InternalLogger.Level.DEBUG,
                target = InternalLogger.Target.USER,
                messageBuilder = { "Proxy credentials are not provided" }
            )
            null
        }
        setProxy(proxy, authenticator)
    }
    return this
}
