/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import cocoapods.DatadogCrashReporting.DDCrashReporter
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
import kotlin.concurrent.Volatile
import cocoapods.DatadogObjc.DDDatadog as DatadogIOS

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
        get() = DatadogIOS.verbosityLevel().toSdkLogVerbosity
        set(value) = DatadogIOS.setVerbosityLevel(value.native)

    @Volatile
    internal actual var isCrashReportingEnabled: Boolean = false

    /**
     * Initializes an instance of the Datadog SDK.
     * @param context your application context (applicable only for Android)
     * @param configuration the configuration for the SDK library
     * @param trackingConsent as the initial state of the tracking consent flag
     * @see [Configuration]
     * @see [TrackingConsent]
     */
    actual fun initialize(
        // unused
        context: Any?,
        configuration: Configuration,
        trackingConsent: TrackingConsent
    ) {
        DatadogIOS.initializeWithConfiguration(configuration.native, trackingConsent.native)

        if (configuration.coreConfig.trackCrashes) {
            DDCrashReporter.enable()
            isCrashReportingEnabled = true
        }
    }

    /**
     * Checks if SDK instance is initialized.
     * @return whenever the instance is initialized or not.
     */
    actual fun isInitialized(): Boolean {
        return DatadogIOS.isInitialized()
    }

    /**
     * Sets the tracking consent regarding the data collection for this instance of the Datadog SDK.
     *
     * @param consent which can take one of the values
     * ([TrackingConsent.PENDING], [TrackingConsent.GRANTED], [TrackingConsent.NOT_GRANTED])
     */
    actual fun setTrackingConsent(consent: TrackingConsent) {
        DatadogIOS.setTrackingConsentWithConsent(consent.native)
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
        DatadogIOS.setUserInfoWithId(
            id,
            name,
            email,
            extraInfo.eraseKeyType()
        )
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
        DatadogIOS.addUserExtraInfo(extraInfo.eraseKeyType())
    }

    /**
     * Clears all unsent data in all registered features.
     */
    actual fun clearAllData() {
        DatadogIOS.clearAllData()
    }

    /**
     * Stop the initialized SDK instance.
     */
    actual fun stopInstance() {
        DatadogIOS.stopInstance()
    }
}

private val SdkLogVerbosity?.native: Long
    get() = when (this) {
        SdkLogVerbosity.DEBUG -> DDSDKVerbosityLevelDebug
        SdkLogVerbosity.WARN -> DDSDKVerbosityLevelWarn
        SdkLogVerbosity.ERROR -> DDSDKVerbosityLevelError
        SdkLogVerbosity.CRITICAL -> DDSDKVerbosityLevelCritical
        null -> DDSDKVerbosityLevelNone
    }

private val Long.toSdkLogVerbosity: SdkLogVerbosity?
    get() = when (this) {
        DDSDKVerbosityLevelDebug -> SdkLogVerbosity.DEBUG
        DDSDKVerbosityLevelWarn -> SdkLogVerbosity.WARN
        DDSDKVerbosityLevelError -> SdkLogVerbosity.ERROR
        DDSDKVerbosityLevelCritical -> SdkLogVerbosity.CRITICAL
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

private fun Map<String, Any?>.eraseKeyType(): Map<Any?, *> {
    return mapKeys {
        // in reality in ObjC it is [String: Any], but KMP generates the
        // signature extraInfo: Map<kotlin.Any?, *>, erasing String type,
        // so we have to do that
        @Suppress("USELESS_CAST")
        it.key as Any
    }
}
