/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.internal

import com.datadog.kmp.log.LogLevel

@Suppress("TooManyFunctions")
internal interface PlatformLogger {

    fun debug(message: String, throwable: Throwable?, attributes: Map<String, Any?>)

    fun info(message: String, throwable: Throwable?, attributes: Map<String, Any?>)

    fun warn(message: String, throwable: Throwable?, attributes: Map<String, Any?>)

    fun error(message: String, throwable: Throwable?, attributes: Map<String, Any?>)

    fun critical(message: String, throwable: Throwable?, attributes: Map<String, Any?>)

    fun log(priority: LogLevel, message: String, throwable: Throwable?, attributes: Map<String, Any?>)

    fun addAttribute(key: String, value: Any?)

    fun removeAttribute(key: String)

    fun addTag(key: String, value: String)

    fun addTag(tag: String)

    fun removeTag(tag: String)

    fun removeTagsWithKey(key: String)

    interface Builder {
        fun build(): PlatformLogger

        fun setService(service: String): Builder

        fun setRemoteLogThreshold(minLogThreshold: LogLevel): Builder

        fun setPrintLogsToConsole(enabled: Boolean): Builder

        fun setNetworkInfoEnabled(enabled: Boolean): Builder

        fun setName(name: String): Builder

        fun setBundleWithTraceEnabled(enabled: Boolean): Builder

        fun setBundleWithRumEnabled(enabled: Boolean): Builder

        fun setRemoteSampleRate(sampleRate: Float): Builder
    }
}

internal expect fun platformLoggerBuilder(): PlatformLogger.Builder
