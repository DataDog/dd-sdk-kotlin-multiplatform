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
}
