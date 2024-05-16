/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

import cocoapods.DatadogObjc.DDLogs
import cocoapods.DatadogObjc.DDLogsConfiguration

/**
 * An entry point to Datadog Logs feature.
 */
actual object Logs {

    /**
     * Enables a Logs feature.
     */
    actual fun enable() {
        DDLogs.enableWith(
            DDLogsConfiguration(customEndpoint = null)
        )
    }

    /**
     * Add a custom attribute to all future logs sent by loggers.
     *
     * Values can be nested up to 10 levels deep. Keys using more than 10 levels will be sanitized by SDK.
     *
     * @param key the key for this attribute
     * @param value the attribute value
     */
    actual fun addAttribute(key: String, value: Any?) = DDLogs.addAttributeForKey(key, value)

    /**
     * Remove a custom attribute from all future logs sent by loggers.
     *
     * Previous logs won't lose the attribute value associated with this key if they were created
     * prior to this call.
     *
     * @param key the key of the attribute to remove
     */
    actual fun removeAttribute(key: String) = DDLogs.removeAttributeForKey(key)
}