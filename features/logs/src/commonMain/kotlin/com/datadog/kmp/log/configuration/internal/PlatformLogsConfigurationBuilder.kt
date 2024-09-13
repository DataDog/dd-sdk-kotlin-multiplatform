/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.configuration.internal

import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.log.model.LogEvent

internal interface PlatformLogsConfigurationBuilder<out T : Any> {
    fun setEventMapper(eventMapper: EventMapper<LogEvent>)

    fun build(): T
}
