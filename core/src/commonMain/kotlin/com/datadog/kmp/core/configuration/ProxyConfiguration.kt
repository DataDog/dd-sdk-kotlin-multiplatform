/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

/**
 * Proxy configuration to use.
 *
 * @param type the proxy type.
 * @param hostname the proxy host name.
 * @param port the proxy port.
 */
class ProxyConfiguration(internal val type: ProxyType, internal val hostname: String, internal val port: UInt) {
    internal var username: String? = null
    internal var password: String? = null

    /**
     * Adds Basic authentication scheme to the proxy connection request.
     *
     * @param username the user name.
     * @param password the password.
     */
    fun withBasicAuthentication(username: String, password: String): ProxyConfiguration {
        this.username = username
        this.password = password
        return this
    }

    /** @inheritDoc */
    override fun toString(): String {
        var description = "${ProxyConfiguration::class.simpleName}: type=$type, hostname=$hostname, port=$port"

        if (username != null) {
            description += ", username=$username"
        }
        if (password != null) {
            description += ", password=***"
        }
        return description
    }
}

/**
 * Represents a proxy type.
 */
enum class ProxyType {
    /**
     * HTTP proxy type.
     */
    HTTP,

    /**
     * SOCKS proxy type.
     */
    SOCKS
}
