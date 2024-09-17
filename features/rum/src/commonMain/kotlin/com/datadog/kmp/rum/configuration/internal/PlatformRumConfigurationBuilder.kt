/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.event.ViewEventMapper
import com.datadog.kmp.rum.model.ActionEvent
import com.datadog.kmp.rum.model.ErrorEvent
import com.datadog.kmp.rum.model.LongTaskEvent
import com.datadog.kmp.rum.model.ResourceEvent

@Suppress("TooManyFunctions")
internal interface PlatformRumConfigurationBuilder<out T : Any> {

    fun setSessionSampleRate(sampleRate: Float)

    fun setTelemetrySampleRate(sampleRate: Float)

    fun trackLongTasks(longTaskThresholdMs: Long)

    fun trackBackgroundEvents(enabled: Boolean)

    fun trackFrustrations(enabled: Boolean)

    fun setVitalsUpdateFrequency(frequency: VitalsUpdateFrequency)

    fun setSessionListener(sessionListener: RumSessionListener)

    fun setViewEventMapper(eventMapper: ViewEventMapper)

    fun setResourceEventMapper(eventMapper: EventMapper<ResourceEvent>)

    fun setActionEventMapper(eventMapper: EventMapper<ActionEvent>)

    fun setErrorEventMapper(eventMapper: EventMapper<ErrorEvent>)

    fun setLongTaskEventMapper(eventMapper: EventMapper<LongTaskEvent>)

    fun build(): T
}
