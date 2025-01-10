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
import com.datadog.kmp.core.configuration.setProxy
import com.datadog.kmp.internal.InternalAttributes
import com.datadog.kmp.privacy.TrackingConsent
import android.util.Log as AndroidLog
import com.datadog.android.Datadog as DatadogAndroidImpl
import com.datadog.android.DatadogSite as DatadogSiteAndroid
import com.datadog.android.core.configuration.BatchProcessingLevel as BatchProcessingLevelAndroid
import com.datadog.android.core.configuration.BatchSize as BatchSizeAndroid
import com.datadog.android.core.configuration.Configuration as ConfigurationAndroid
import com.datadog.android.core.configuration.UploadFrequency as UploadFrequencyAndroid
import com.datadog.android.privacy.TrackingConsent as TrackingConsentAndroid

/**
 * This class initializes the Datadog SDK, and sets up communication with the server.
 */
actual object Datadog {

    /**
     * Verbosity of the Datadog SDK.
     *
     * Messages with a priority level equal or above the given level will be sent to the platform-specific
     * logging output (Android - Logcat, iOS - debugger console).
     *
     * @see [SdkLogVerbosity]
     */
    actual var verbosity: SdkLogVerbosity?
        get() = platformImplementation.getVerbosity().toSdkLogVerbosity
        set(value) = platformImplementation.setVerbosity(value.native)

    @Volatile
    internal actual var isCrashReportingEnabled: Boolean = false

    /**
     * Initializes an instance of the Datadog SDK.
     * @param context your application context (applicable only for Android)
     * @param configuration the configuration for the SDK library
     * @param trackingConsent as the initial state of the tracking consent flag
     * @see [Configuration]
     * @see [TrackingConsent]
     * @throws IllegalArgumentException if the env name is using illegal characters and your
     * application is in debug mode otherwise returns null and stops initializing the SDK (applicable only for Android)
     */
    actual fun initialize(
        context: Any?,
        configuration: Configuration,
        trackingConsent: TrackingConsent
    ) {
        requireNotNull(context)
        platformImplementation.initialize(context as Context, configuration.native, trackingConsent.native)
        isCrashReportingEnabled = configuration.coreConfig.trackCrashes
    }

    /**
     * Checks if SDK instance is initialized.
     * @return whenever the instance is initialized or not.
     */
    actual fun isInitialized(): Boolean {
        return platformImplementation.isInitialized()
    }

    /**
     * Sets the tracking consent regarding the data collection for this instance of the Datadog SDK.
     *
     * @param consent which can take one of the values
     * ([TrackingConsent.PENDING], [TrackingConsent.GRANTED], [TrackingConsent.NOT_GRANTED])
     */
    actual fun setTrackingConsent(consent: TrackingConsent) {
        platformImplementation.setTrackingConsent(consent.native)
    }

    /**
     * Sets the user information.
     *
     * @param id (nullable) a unique user identifier (relevant to your business domain)
     * @param name (nullable) the user name or alias
     * @param email (nullable) the user email
     * @param extraInfo additional information. An extra information can be
     * nested up to 8 levels deep. Keys using more than 8 levels will be sanitized by SDK.
     */
    actual fun setUserInfo(
        id: String?,
        name: String?,
        email: String?,
        extraInfo: Map<String, Any?>
    ) {
        platformImplementation.setUserInfo(id, name, email, extraInfo)
    }

    /**
     * Sets additional information for the user.
     *
     * If properties had originally been set with [setUserInfo], they will be preserved.
     * In the event of a conflict on key, the new property will prevail.
     *
     * @param extraInfo additional information. An extra information can be
     * nested up to 8 levels deep. Keys using more than 8 levels will be sanitized by SDK.
     */
    actual fun addUserExtraInfo(extraInfo: Map<String, Any?>) {
        platformImplementation.addUserProperties(extraInfo)
    }

    /**
     * Clears all unsent data in all registered features.
     */
    actual fun clearAllData() {
        platformImplementation.clearAllData()
    }

    /**
     * Stop the initialized SDK instance.
     */
    actual fun stopInstance() {
        platformImplementation.stopInstance()
    }

    internal val platformImplementation: DatadogAndroidImpl
        get() = DatadogAndroidImpl
}

private val SdkLogVerbosity?.native: Int
    get() = when (this) {
        SdkLogVerbosity.DEBUG -> AndroidLog.DEBUG
        SdkLogVerbosity.WARN -> AndroidLog.WARN
        SdkLogVerbosity.ERROR -> AndroidLog.ERROR
        SdkLogVerbosity.CRITICAL -> AndroidLog.ASSERT
        null -> Int.MAX_VALUE
    }

private val Int.toSdkLogVerbosity: SdkLogVerbosity?
    get() = when (this) {
        AndroidLog.DEBUG -> SdkLogVerbosity.DEBUG
        AndroidLog.WARN -> SdkLogVerbosity.WARN
        AndroidLog.ERROR -> SdkLogVerbosity.ERROR
        AndroidLog.ASSERT -> SdkLogVerbosity.CRITICAL
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
            .setCrashReportsEnabled(coreConfig.trackCrashes)
            .setAdditionalConfiguration(
                mapOf(
                    InternalAttributes.SOURCE_ATTRIBUTE,
                    InternalAttributes.SDK_VERSION_ATTRIBUTE
                )
            )
            .setProxy(coreConfig.proxyConfiguration)
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
