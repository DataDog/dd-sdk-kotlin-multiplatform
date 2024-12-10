/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.configuration.internal

import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.log.model.LogEvent
import com.datadog.kmp.log.model.toCommonModel
import com.datadog.android.log.LogsConfiguration as NativeLogsConfiguration

internal class AndroidLogsConfigurationBuilder(
    private val nativeBuilder: NativeLogsConfiguration.Builder = NativeLogsConfiguration.Builder()
) : PlatformLogsConfigurationBuilder<NativeLogsConfiguration> {

    override fun setEventMapper(eventMapper: EventMapper<LogEvent>) {
        nativeBuilder.setEventMapper native@{ logEvent ->
            val mapped = eventMapper.map(logEvent.toCommonModel()) ?: return@native null

            logEvent.message = mapped.message
            logEvent.ddtags = mapped.ddtags

            logEvent.error?.message = mapped.error?.message
            logEvent.error?.kind = mapped.error?.kind
            logEvent.error?.stack = mapped.error?.stack
            logEvent.error?.fingerprint = mapped.error?.fingerprint
            logEvent.error?.sourceType = mapped.error?.sourceType

            // root and usr additional properties are exposed as a reference, so no need to copy things back

            logEvent
        }
    }

    override fun build(): NativeLogsConfiguration = nativeBuilder.build()
}
