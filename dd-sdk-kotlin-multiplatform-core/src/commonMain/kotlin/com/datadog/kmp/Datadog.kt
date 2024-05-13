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
     * @see [LogLevel]
     */
    var verbosity: LogLevel?

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
    fun setUserInfo(
        id: String? = null,
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
     * Clears all unsent data in all registered features.
     */
    fun clearAllData()

    /**
     * Stop the initialized SDK instance.
     */
    fun stopInstance()
}
