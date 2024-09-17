/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.internal

import cocoapods.DatadogObjc.DDLogLevel
import cocoapods.DatadogObjc.DDLogLevelCritical
import cocoapods.DatadogObjc.DDLogLevelDebug
import cocoapods.DatadogObjc.DDLogLevelError
import cocoapods.DatadogObjc.DDLogLevelInfo
import cocoapods.DatadogObjc.DDLogLevelWarn
import cocoapods.DatadogObjc.DDLogger
import cocoapods.DatadogObjc.DDLoggerConfiguration
import com.datadog.kmp.internal.createNSErrorFromThrowable
import com.datadog.kmp.internal.eraseKeyType
import com.datadog.kmp.internal.withIncludeBinaryImages
import com.datadog.kmp.log.LogLevel
import platform.Foundation.NSError

// open for mocking
internal open class IOSPlatformLogger : PlatformLogger {

    private val nativeLogger: DDLogger

    // only for tests, default constructor should exist for mocking
    @Suppress("unused")
    constructor() : this(nativeLogger = DDLogger.createWith(DDLoggerConfiguration.default()))

    constructor(nativeLogger: DDLogger) {
        this.nativeLogger = nativeLogger
    }

    // region PlatformLogger

    override fun debug(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        if (throwable != null) {
            nativeLogger.debug(
                message,
                createNSErrorFromThrowable(throwable),
                eraseKeyType(withIncludeBinaryImages(attributes))
            )
        } else {
            nativeLogger.debug(message, eraseKeyType(attributes))
        }
    }

    override fun info(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        if (throwable != null) {
            nativeLogger.info(
                message,
                createNSErrorFromThrowable(throwable),
                eraseKeyType(withIncludeBinaryImages(attributes))
            )
        } else {
            nativeLogger.info(message, eraseKeyType(attributes))
        }
    }

    override fun warn(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        if (throwable != null) {
            nativeLogger.warn(
                message,
                createNSErrorFromThrowable(throwable),
                eraseKeyType(withIncludeBinaryImages(attributes))
            )
        } else {
            nativeLogger.warn(message, eraseKeyType(attributes))
        }
    }

    override fun error(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        if (throwable != null) {
            nativeLogger.error(
                message,
                createNSErrorFromThrowable(throwable),
                eraseKeyType(withIncludeBinaryImages(attributes))
            )
        } else {
            nativeLogger.error(message, eraseKeyType(attributes))
        }
    }

    override fun critical(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        if (throwable != null) {
            nativeLogger.critical(
                message,
                createNSErrorFromThrowable(throwable),
                eraseKeyType(withIncludeBinaryImages(attributes))
            )
        } else {
            nativeLogger.critical(message, eraseKeyType(attributes))
        }
    }

    override fun log(priority: LogLevel, message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        // no log method in ObjC API, so this
        when (priority) {
            LogLevel.DEBUG -> debug(message, throwable, attributes)
            LogLevel.INFO -> info(message, throwable, attributes)
            LogLevel.WARN -> warn(message, throwable, attributes)
            LogLevel.ERROR -> error(message, throwable, attributes)
            LogLevel.CRITICAL -> critical(message, throwable, attributes)
        }
    }

    override fun addAttribute(key: String, value: Any?) {
        nativeLogger.addAttributeForKey(key, value)
    }

    override fun removeAttribute(key: String) {
        nativeLogger.removeAttributeForKey(key)
    }

    override fun addTag(key: String, value: String) {
        nativeLogger.addTagWithKey(key, value)
    }

    override fun addTag(tag: String) {
        nativeLogger.addWithTag(tag)
    }

    override fun removeTag(tag: String) {
        nativeLogger.removeWithTag(tag)
    }

    override fun removeTagsWithKey(key: String) {
        nativeLogger.removeTagWithKey(key)
    }

    // endregion

    // region iOS-specific methods

    // open for mocking
    open fun debug(message: String, error: NSError, attributes: Map<String, Any?>) {
        nativeLogger.debug(message, error, eraseKeyType(withIncludeBinaryImages(attributes)))
    }

    open fun info(message: String, error: NSError, attributes: Map<String, Any?>) {
        nativeLogger.info(message, error, eraseKeyType(withIncludeBinaryImages(attributes)))
    }

    open fun warn(message: String, error: NSError, attributes: Map<String, Any?>) {
        nativeLogger.warn(message, error, eraseKeyType(withIncludeBinaryImages(attributes)))
    }

    open fun error(message: String, error: NSError, attributes: Map<String, Any?>) {
        nativeLogger.error(message, error, eraseKeyType(withIncludeBinaryImages(attributes)))
    }

    open fun critical(message: String, error: NSError, attributes: Map<String, Any?>) {
        nativeLogger.critical(message, error, eraseKeyType(withIncludeBinaryImages(attributes)))
    }

    // endregion

    class Builder(private val loggerConfiguration: DDLoggerConfiguration) : PlatformLogger.Builder {

        override fun build(): PlatformLogger {
            return IOSPlatformLogger(DDLogger.createWith(loggerConfiguration))
        }

        override fun setService(service: String): PlatformLogger.Builder {
            loggerConfiguration.setService(service)
            return this
        }

        override fun setRemoteLogThreshold(minLogThreshold: LogLevel): PlatformLogger.Builder {
            loggerConfiguration.setRemoteLogThreshold(minLogThreshold.native)
            return this
        }

        override fun setPrintLogsToConsole(enabled: Boolean): PlatformLogger.Builder {
            loggerConfiguration.setPrintLogsToConsole(enabled)
            return this
        }

        override fun setNetworkInfoEnabled(enabled: Boolean): PlatformLogger.Builder {
            loggerConfiguration.setNetworkInfoEnabled(enabled)
            return this
        }

        override fun setName(name: String): PlatformLogger.Builder {
            loggerConfiguration.setName(name)
            return this
        }

        override fun setBundleWithTraceEnabled(enabled: Boolean): PlatformLogger.Builder {
            loggerConfiguration.setBundleWithTraceEnabled(enabled)
            return this
        }

        override fun setBundleWithRumEnabled(enabled: Boolean): PlatformLogger.Builder {
            loggerConfiguration.setBundleWithRumEnabled(enabled)
            return this
        }

        override fun setRemoteSampleRate(sampleRate: Float): PlatformLogger.Builder {
            loggerConfiguration.setRemoteSampleRate(sampleRate)
            return this
        }
    }
}

internal actual fun platformLoggerBuilder(): PlatformLogger.Builder =
    IOSPlatformLogger.Builder(DDLoggerConfiguration.default())

// region support methods

internal fun DDLoggerConfiguration.Companion.default(): DDLoggerConfiguration {
    // even though ObjC API has default values defined, KMP binding is not picking them;
    // keep it aligned with the signature in DatadogObjc
    return DDLoggerConfiguration(
        service = null,
        name = null,
        networkInfoEnabled = false,
        bundleWithRumEnabled = true,
        bundleWithTraceEnabled = true,
        remoteSampleRate = 100.toFloat(),
        remoteLogThreshold = LogLevel.DEBUG.native,
        printLogsToConsole = false
    )
}

private val LogLevel.native: DDLogLevel
    get() = when (this) {
        LogLevel.DEBUG -> DDLogLevelDebug
        LogLevel.INFO -> DDLogLevelInfo
        LogLevel.WARN -> DDLogLevelWarn
        LogLevel.ERROR -> DDLogLevelError
        LogLevel.CRITICAL -> DDLogLevelCritical
    }

// endregion
