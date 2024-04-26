/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import android.content.Context
import com.datadog.kmp.core.configuration.BatchProcessingLevel
import com.datadog.kmp.core.configuration.BatchSize
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.core.configuration.UploadFrequency
import com.datadog.kmp.privacy.TrackingConsent
import android.util.Log as AndroidLog
import com.datadog.android.Datadog as DatadogAndroid
import com.datadog.android.DatadogSite as DatadogSiteAndroid
import com.datadog.android.core.configuration.BatchProcessingLevel as BatchProcessingLevelAndroid
import com.datadog.android.core.configuration.BatchSize as BatchSizeAndroid
import com.datadog.android.core.configuration.Configuration as ConfigurationAndroid
import com.datadog.android.core.configuration.UploadFrequency as UploadFrequencyAndroid
import com.datadog.android.privacy.TrackingConsent as TrackingConsentAndroid

actual object Datadog {

    actual var verbosity: LogLevel?
        get() = DatadogAndroid.getVerbosity().toLogLevel
        set(value) = DatadogAndroid.setVerbosity(value.native)

    actual fun initialize(
        context: Any?,
        configuration: Configuration,
        trackingConsent: TrackingConsent
    ) {
        requireNotNull(context)
        DatadogAndroid.initialize(context as Context, configuration.native, trackingConsent.native)
    }

    actual fun setTrackingConsent(consent: TrackingConsent) {
        DatadogAndroid.setTrackingConsent(consent.native)
    }

    actual fun setUserInfo(
        id: String?,
        name: String?,
        email: String?,
        extraInfo: Map<String, Any?>
    ) {
        DatadogAndroid.setUserInfo(id, name, email, extraInfo)
    }

    actual fun clearAllData() {
        DatadogAndroid.clearAllData()
    }
}

private val LogLevel?.native: Int
    get() = when (this) {
        LogLevel.DEBUG -> AndroidLog.DEBUG
        LogLevel.WARN -> AndroidLog.WARN
        LogLevel.ERROR -> AndroidLog.ERROR
        LogLevel.CRITICAL -> AndroidLog.ASSERT
        null -> Int.MAX_VALUE
    }

private val Int.toLogLevel: LogLevel?
    get() = when (this) {
        AndroidLog.DEBUG -> LogLevel.DEBUG
        AndroidLog.WARN -> LogLevel.WARN
        AndroidLog.ERROR -> LogLevel.ERROR
        AndroidLog.ASSERT -> LogLevel.CRITICAL
        else -> null
    }

private val Configuration.native: ConfigurationAndroid
    get() {
        return ConfigurationAndroid.Builder(
            clientToken = clientToken,
            env = env,
            service = service,
            variant = variant
        )
            .useSite(coreConfig.site.native)
            .setBatchSize(coreConfig.batchSize.native)
            .setUploadFrequency(coreConfig.uploadFrequency.native)
            .setBatchProcessingLevel(coreConfig.batchProcessingLevel.native)
            .build()
    }

private val TrackingConsent.native: TrackingConsentAndroid
    get() = when (this) {
        TrackingConsent.GRANTED -> TrackingConsentAndroid.GRANTED
        TrackingConsent.PENDING -> TrackingConsentAndroid.PENDING
        TrackingConsent.NOT_GRANTED -> TrackingConsentAndroid.NOT_GRANTED
    }

private val UploadFrequency.native: UploadFrequencyAndroid
    get() = when (this) {
        UploadFrequency.FREQUENT -> UploadFrequencyAndroid.FREQUENT
        UploadFrequency.AVERAGE -> UploadFrequencyAndroid.AVERAGE
        UploadFrequency.RARE -> UploadFrequencyAndroid.RARE
    }

private val BatchProcessingLevel.native: BatchProcessingLevelAndroid
    get() = when (this) {
        BatchProcessingLevel.HIGH -> BatchProcessingLevelAndroid.HIGH
        BatchProcessingLevel.MEDIUM -> BatchProcessingLevelAndroid.MEDIUM
        BatchProcessingLevel.LOW -> BatchProcessingLevelAndroid.LOW
    }

private val BatchSize.native: BatchSizeAndroid
    get() = when (this) {
        BatchSize.LARGE -> BatchSizeAndroid.LARGE
        BatchSize.MEDIUM -> BatchSizeAndroid.MEDIUM
        BatchSize.SMALL -> BatchSizeAndroid.SMALL
    }

private val DatadogSite.native: DatadogSiteAndroid
    get() = when (this) {
        DatadogSite.US1 -> DatadogSiteAndroid.US1
        DatadogSite.US1_FED -> DatadogSiteAndroid.US1_FED
        DatadogSite.US3 -> DatadogSiteAndroid.US3
        DatadogSite.US5 -> DatadogSiteAndroid.US5
        DatadogSite.EU1 -> DatadogSiteAndroid.EU1
        DatadogSite.AP1 -> DatadogSiteAndroid.AP1
    }
