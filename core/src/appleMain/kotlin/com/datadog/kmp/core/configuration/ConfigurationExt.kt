/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.core.configuration

import cocoapods.DatadogObjc.DDConfiguration
import cocoapods.DatadogObjc.DDCoreLoggerLevelDebug
import cocoapods.DatadogObjc.DDInternalLogger

internal fun DDConfiguration.setProxy(proxyConfiguration: ProxyConfiguration?) {
    if (proxyConfiguration == null) return

    setProxyConfiguration(proxyConfiguration.toConfigurationMap().mapKeys { it.key })
}

private fun ProxyConfiguration.toConfigurationMap(): Map<String, Any> {
    val proxyConfigMap = mutableMapOf<String, Any>()
    val username = username
    val password = password
    // not using property references from CoreFoundation, because, by some reason, KMP didn't generate
    // bindings for __CFString/CFString
    if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
        proxyConfigMap["kCFProxyUsernameKey"] = username
        proxyConfigMap["kCFProxyPasswordKey"] = password
    } else {
        DDInternalLogger.consolePrint("Proxy credentials are not provided", DDCoreLoggerLevelDebug)
    }

    when (type) {
        ProxyType.HTTP -> {
            // Datadog intakes are only HTTPS, so configuration proxy only for such kind of traffic
            proxyConfigMap["HTTPSEnable"] = true
            proxyConfigMap["HTTPSProxy"] = hostname
            proxyConfigMap["HTTPSPort"] = port
        }

        ProxyType.SOCKS -> {
            proxyConfigMap["SOCKSEnable"] = true
            proxyConfigMap["SOCKSProxy"] = hostname
            proxyConfigMap["SOCKSPort"] = port
        }
    }
    return proxyConfigMap
}
