/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

@file:OptIn(ExperimentalForeignApi::class)

package com.datadog.kmp

import cocoapods.DatadogObjc.DDBatchProcessingLevelHigh
import cocoapods.DatadogObjc.DDBatchProcessingLevelLow
import cocoapods.DatadogObjc.DDBatchProcessingLevelMedium
import cocoapods.DatadogObjc.DDBatchSizeLarge
import cocoapods.DatadogObjc.DDBatchSizeMedium
import cocoapods.DatadogObjc.DDBatchSizeSmall
import cocoapods.DatadogObjc.DDConfiguration
import cocoapods.DatadogObjc.DDSDKVerbosityLevelCritical
import cocoapods.DatadogObjc.DDSDKVerbosityLevelDebug
import cocoapods.DatadogObjc.DDSDKVerbosityLevelError
import cocoapods.DatadogObjc.DDSDKVerbosityLevelNone
import cocoapods.DatadogObjc.DDSDKVerbosityLevelWarn
import cocoapods.DatadogObjc.DDSite
import cocoapods.DatadogObjc.DDTrackingConsent
import cocoapods.DatadogObjc.DDUploadFrequency
import cocoapods.DatadogObjc.DDUploadFrequencyAverage
import cocoapods.DatadogObjc.DDUploadFrequencyFrequent
import cocoapods.DatadogObjc.DDUploadFrequencyRare
import com.datadog.kmp.core.configuration.BatchProcessingLevel
import com.datadog.kmp.core.configuration.BatchSize
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.core.configuration.UploadFrequency
import com.datadog.kmp.privacy.TrackingConsent
import kotlinx.cinterop.ExperimentalForeignApi
import cocoapods.DatadogObjc.DDDatadog as DatadogIOS

actual object Datadog {
    actual var verbosity: LogLevel?
        get() = DatadogIOS.verbosityLevel().toLogLevel
        set(value) = DatadogIOS.setVerbosityLevel(value.native)

    actual fun initialize(
        // unused
        context: Any?,
        configuration: Configuration,
        trackingConsent: TrackingConsent
    ) {
        DatadogIOS.initializeWithConfiguration(configuration.native, trackingConsent.native)
    }
}

private val LogLevel?.native: Long
    get() = when (this) {
        LogLevel.DEBUG -> DDSDKVerbosityLevelDebug
        LogLevel.WARN -> DDSDKVerbosityLevelWarn
        LogLevel.ERROR -> DDSDKVerbosityLevelError
        LogLevel.CRITICAL -> DDSDKVerbosityLevelCritical
        null -> DDSDKVerbosityLevelNone
    }

private val Long.toLogLevel: LogLevel?
    get() = when (this) {
        DDSDKVerbosityLevelDebug -> LogLevel.DEBUG
        DDSDKVerbosityLevelWarn -> LogLevel.WARN
        DDSDKVerbosityLevelError -> LogLevel.ERROR
        DDSDKVerbosityLevelCritical -> LogLevel.CRITICAL
        else -> null
    }

private val Configuration.native: DDConfiguration
    get() {
        val nativeConfig = DDConfiguration(
            clientToken = clientToken,
            env = env
        )
        nativeConfig.setSite(coreConfig.site.native)
        nativeConfig.setService(service)
        nativeConfig.setUploadFrequency(coreConfig.uploadFrequency.native)
        nativeConfig.setBatchProcessingLevel(coreConfig.batchProcessingLevel.native)
        nativeConfig.setBatchSize(coreConfig.batchSize.native)
        return nativeConfig
    }

private val TrackingConsent.native: DDTrackingConsent
    get() = when (this) {
        TrackingConsent.GRANTED -> DDTrackingConsent.granted()
        TrackingConsent.PENDING -> DDTrackingConsent.pending()
        TrackingConsent.NOT_GRANTED -> DDTrackingConsent.notGranted()
    }

private val UploadFrequency.native: DDUploadFrequency
    get() = when (this) {
        UploadFrequency.FREQUENT -> DDUploadFrequencyFrequent
        UploadFrequency.AVERAGE -> DDUploadFrequencyAverage
        UploadFrequency.RARE -> DDUploadFrequencyRare
    }

private val BatchProcessingLevel.native: DDUploadFrequency
    get() = when (this) {
        BatchProcessingLevel.HIGH -> DDBatchProcessingLevelHigh
        BatchProcessingLevel.MEDIUM -> DDBatchProcessingLevelMedium
        BatchProcessingLevel.LOW -> DDBatchProcessingLevelLow
    }

private val BatchSize.native: DDUploadFrequency
    get() = when (this) {
        BatchSize.LARGE -> DDBatchSizeLarge
        BatchSize.MEDIUM -> DDBatchSizeMedium
        BatchSize.SMALL -> DDBatchSizeSmall
    }

private val DatadogSite.native: DDSite
    get() = when (this) {
        DatadogSite.US1 -> DDSite.us1()
        DatadogSite.US1_FED -> DDSite.us1_fed()
        DatadogSite.US3 -> DDSite.us3()
        DatadogSite.US5 -> DDSite.us5()
        DatadogSite.EU1 -> DDSite.eu1()
        DatadogSite.AP1 -> DDSite.ap1()
    }
