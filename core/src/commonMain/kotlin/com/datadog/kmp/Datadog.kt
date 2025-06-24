/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp

import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.privacy.TrackingConsent

/**
 * This class initializes the Datadog SDK, and sets up communication with the server.
 */
expect object Datadog {

    /**
     * Verbosity of the Datadog SDK.
     *
     * Messages with a priority level equal or above the given level will be sent to the platform-specific
     * logging output (Android - Logcat, iOS - debugger console).
     *
     * @see [SdkLogVerbosity]
     */
    var verbosity: SdkLogVerbosity?

    internal var isCrashReportingEnabled: Boolean

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
    fun initialize(
        // TODO RUM-4288 iOS SDK ObjC API doesn't support custom instance name
        context: Any? = null,
        configuration: Configuration,
        trackingConsent: TrackingConsent
    )

    /**
     * Checks if SDK instance is initialized.
     * @return whenever the instance is initialized or not.
     */
    fun isInitialized(): Boolean

    /**
     * Sets the tracking consent regarding the data collection for this instance of the Datadog SDK.
     *
     * @param consent which can take one of the values
     * ([TrackingConsent.PENDING], [TrackingConsent.GRANTED], [TrackingConsent.NOT_GRANTED])
     */
    fun setTrackingConsent(consent: TrackingConsent)

    /**
     * Sets the user information.
     *
     * @param id (nullable) a unique user identifier (relevant to your business domain)
     * @param name (nullable) the user name or alias
     * @param email (nullable) the user email
     * @param extraInfo additional information. An extra information can be
     * nested up to 8 levels deep. Keys using more than 8 levels will be sanitized by SDK.
     */
    @Deprecated(
        "Use setUserInfo call with mandatory User ID instead."
    )
    fun setUserInfo(
        id: String? = null,
        name: String? = null,
        email: String? = null,
        extraInfo: Map<String, Any?> = emptyMap()
    )

    /**
     * Sets the user information.
     *
     * @param id (mandatory) a unique user identifier (relevant to your business domain)
     * @param name (nullable) the user name or alias
     * @param email (nullable) the user email
     * @param extraInfo additional information. An extra information can be
     * nested up to 8 levels deep. Keys using more than 8 levels will be sanitized by SDK.
     */
    fun setUserInfo(
        id: String,
        name: String? = null,
        email: String? = null,
        extraInfo: Map<String, Any?> = emptyMap()
    )

    /**
     * Sets additional information for the user.
     *
     * If properties had originally been set with [setUserInfo], they will be preserved.
     * In the event of a conflict on key, the new property will prevail.
     *
     * @param extraInfo additional information. An extra information can be
     * nested up to 8 levels deep. Keys using more than 8 levels will be sanitized by SDK.
     */
    fun addUserExtraInfo(extraInfo: Map<String, Any?>)

    /**
     * Sets the account information that the user is currently logged into.
     *
     * This API should be used to assign an identifier for the user's account which represents a
     * contextual identity within the app, typically tied to business or tenant logic. The
     * information set here will be added to logs, traces and RUM events.
     *
     * This value should be set when user logs in with his account, and cleared by calling
     * [clearAccountInfo] when he logs out.
     *
     * @param id Account ID.
     * @param name representing the account, if exists.
     * @param extraInfo Account custom attributes, if exists.
     */
    fun setAccountInfo(
        id: String,
        name: String? = null,
        extraInfo: Map<String, Any?> = emptyMap()
    )

    /**
     * Add custom attributes to the current account information.
     *
     * This extra info will be added to already existing extra info that is added
     * to Logs, Traces and RUM events automatically.
     *
     * @param extraInfo Account additional custom attributes.
     */
    fun addAccountExtraInfo(extraInfo: Map<String, Any?>)

    /**
     * Clear the current account information.
     *
     * Account information will be set to null.
     * Following Logs, Traces, RUM Events will not include the account information anymore.
     *
     * Any active RUM Session, active RUM View at the time of call will have their `account` attribute cleared.
     *
     * If you want to retain the current `account` on the active RUM session,
     * you need to stop the session first by using `RumMonitor.get().stopSession()`.
     *
     * If you want to retain the current `account` on the active RUM views,
     * you need to stop the view first by using `RumMonitor.get().stopView()`.
     *
     */
    fun clearAccountInfo()

    /**
     * Clears all unsent data in all registered features.
     */
    fun clearAllData()

    /**
     * Stop the initialized SDK instance.
     */
    fun stopInstance()
}
