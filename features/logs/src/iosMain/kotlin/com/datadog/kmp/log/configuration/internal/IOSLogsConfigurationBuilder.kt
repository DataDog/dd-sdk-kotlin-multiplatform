/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.configuration.internal

import cocoapods.DatadogObjc.DDLogsConfiguration
import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.log.model.LogEvent
import com.datadog.kmp.log.model.internal.toCommonModel

internal class IOSLogsConfigurationBuilder : PlatformLogsConfigurationBuilder<DDLogsConfiguration> {
    private val nativeLogsConfiguration = DDLogsConfiguration(customEndpoint = null)

    override fun setEventMapper(eventMapper: EventMapper<LogEvent>) {
        nativeLogsConfiguration.setEventMapper native@{ logEvent ->
            if (logEvent == null) return@native null

            val mapped = eventMapper.map(logEvent.toCommonModel()) ?: return@native null

            // TODO RUM-6088 status, logger name are mutable in Android SDK, but not mutable in iOS SDK
            logEvent.setMessage(mapped.message)
            logEvent.setTags(mapped.ddtags.split(","))

            logEvent.error()?.setMessage(mapped.error?.message)
            logEvent.error()?.setKind(mapped.error?.kind)
            logEvent.error()?.setStack(mapped.error?.stack)
            logEvent.error()?.setFingerprint(mapped.error?.fingerprint)
            mapped.error?.sourceType?.let {
                logEvent.error()?.setSourceType(it)
            }

            logEvent
        }
    }

    override fun build(): DDLogsConfiguration = nativeLogsConfiguration
}
