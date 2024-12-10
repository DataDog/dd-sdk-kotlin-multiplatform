/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

import cocoapods.DatadogObjc.DDLogger
import cocoapods.DatadogObjc.DDLoggerConfiguration
import cocoapods.DatadogObjc.DDLogs
import cocoapods.DatadogObjc.DDLogsConfiguration
import com.datadog.kmp.internal.INCLUDE_BINARY_IMAGES
import com.datadog.kmp.internal.InternalProxy
import com.datadog.kmp.internal.LOG_ERROR_IS_CRASH
import com.datadog.kmp.internal.addDatadogUnhandledExceptionHook
import com.datadog.kmp.internal.createNSErrorFromThrowable
import com.datadog.kmp.log.configuration.LogsConfiguration
import com.datadog.kmp.log.internal.default

/**
 * An entry point to Datadog Logs feature.
 */
actual object Logs {

    private const val ALL_IN_SAMPLE_RATE = 100f

    /**
     * Enables a Logs feature.
     *
     * @param logsConfiguration Configuration to use for the feature.
     */
    actual fun enable(logsConfiguration: LogsConfiguration) {
        DDLogs.enableWith(logsConfiguration.nativeConfiguration as DDLogsConfiguration)

        if (InternalProxy.isCrashReportingEnabled) {
            addDatadogUnhandledExceptionHook {
                val loggerConfiguration = DDLoggerConfiguration.default().apply {
                    setNetworkInfoEnabled(true)
                    setRemoteSampleRate(ALL_IN_SAMPLE_RATE)
                }
                // TODO RUM-5178 No ObjC API to write crash log directly, without any logger
                val crashLogger = DDLogger.createWith(loggerConfiguration)

                crashLogger.critical(
                    "Caught unhandled Kotlin exception",
                    createNSErrorFromThrowable(it),
                    mutableMapOf<Any?, Any?>().apply {
                        this += INCLUDE_BINARY_IMAGES to true
                        this += LOG_ERROR_IS_CRASH to true
                    }
                )
            }
        }
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
