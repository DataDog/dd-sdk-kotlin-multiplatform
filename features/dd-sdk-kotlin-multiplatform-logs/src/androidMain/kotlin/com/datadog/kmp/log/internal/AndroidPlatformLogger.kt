/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.internal

import android.util.Log
import com.datadog.kmp.log.LogLevel
import com.datadog.android.log.Logger as NativeLogger

internal class AndroidPlatformLogger(private val nativeLogger: NativeLogger) : PlatformLogger {

    override fun debug(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        nativeLogger.d(message, throwable, attributes)
    }

    override fun info(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        nativeLogger.i(message, throwable, attributes)
    }

    override fun warn(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        nativeLogger.w(message, throwable, attributes)
    }

    override fun error(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        nativeLogger.e(message, throwable, attributes)
    }

    override fun critical(message: String, throwable: Throwable?, attributes: Map<String, Any?>) {
        nativeLogger.wtf(message, throwable, attributes)
    }

    override fun addAttribute(key: String, value: Any?) {
        nativeLogger.addAttribute(key, value)
    }

    override fun removeAttribute(key: String) {
        nativeLogger.removeAttribute(key)
    }

    override fun addTag(key: String, value: String) {
        nativeLogger.addTag(key, value)
    }

    override fun addTag(tag: String) {
        nativeLogger.addTag(tag)
    }

    override fun removeTag(tag: String) {
        nativeLogger.removeTag(tag)
    }

    override fun removeTagsWithKey(key: String) {
        nativeLogger.removeTagsWithKey(key)
    }

    class Builder(private val nativeLoggerBuilder: NativeLogger.Builder) : PlatformLogger.Builder {

        override fun build(): PlatformLogger {
            return AndroidPlatformLogger(nativeLoggerBuilder.build())
        }

        override fun setService(service: String): PlatformLogger.Builder {
            nativeLoggerBuilder.setService(service)
            return this
        }

        override fun setRemoteLogThreshold(minLogThreshold: LogLevel): PlatformLogger.Builder {
            nativeLoggerBuilder.setRemoteLogThreshold(minLogThreshold.native)
            return this
        }

        override fun setPrintLogsToConsole(enabled: Boolean): PlatformLogger.Builder {
            nativeLoggerBuilder.setLogcatLogsEnabled(enabled)
            return this
        }

        override fun setNetworkInfoEnabled(enabled: Boolean): PlatformLogger.Builder {
            nativeLoggerBuilder.setNetworkInfoEnabled(enabled)
            return this
        }

        override fun setName(name: String): PlatformLogger.Builder {
            nativeLoggerBuilder.setName(name)
            return this
        }

        override fun setBundleWithTraceEnabled(enabled: Boolean): PlatformLogger.Builder {
            nativeLoggerBuilder.setBundleWithTraceEnabled(enabled)
            return this
        }

        override fun setBundleWithRumEnabled(enabled: Boolean): PlatformLogger.Builder {
            nativeLoggerBuilder.setBundleWithRumEnabled(enabled)
            return this
        }

        override fun setRemoteSampleRate(sampleRate: Float): PlatformLogger.Builder {
            nativeLoggerBuilder.setRemoteSampleRate(sampleRate)
            return this
        }
    }
}

internal actual fun platformLoggerBuilder(): PlatformLogger.Builder =
    AndroidPlatformLogger.Builder(NativeLogger.Builder())

private val LogLevel.native: Int
    get() = when (this) {
        LogLevel.DEBUG -> Log.DEBUG
        LogLevel.INFO -> Log.INFO
        LogLevel.WARN -> Log.WARN
        LogLevel.ERROR -> Log.ERROR
        LogLevel.CRITICAL -> Log.ASSERT
    }
