/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.Datadog
import com.datadog.kmp.SdkLogVerbosity
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.log.Logger
import com.datadog.kmp.log.Logs
import com.datadog.kmp.privacy.TrackingConsent
import com.datadog.kmp.rum.Rum
import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency

@Suppress("MagicNumber")
fun initDatadog(context: Any? = null) {
    Datadog.verbosity = SdkLogVerbosity.DEBUG

    val configuration = Configuration.Builder(
        clientToken = "foobar",
        env = "prod"
    ).build()

    Datadog.initialize(context = context, configuration = configuration, trackingConsent = TrackingConsent.GRANTED)

    Logs.enable()

    val rumConfiguration = RumConfiguration.Builder("fake-app-id")
        .setSessionSampleRate(100f)
        .setTelemetrySampleRate(100f)
        .setVitalsUpdateFrequency(VitalsUpdateFrequency.AVERAGE)
        .trackBackgroundEvents(true)
        .setSessionListener { sessionId, isDiscarded ->
            applicationLogger.info("New session $sessionId ${if (isDiscarded) "was discarded" else "started"}")
        }
        .trackLongTasks()
        .trackFrustrations(true)
        .apply {
            platformSpecificSetup(this)
        }
        .build()
    Rum.enable(rumConfiguration)

    Datadog.setUserInfo(
        name = "Random User",
        email = "user@example.com",
        extraInfo = mapOf("age" to 42, "location" to "universe")
    )
}

val applicationLogger by lazy {
    Logger.Builder()
        .setName("kmp-logger")
        .setNetworkInfoEnabled(true)
        .setService("kmp-shared")
        .setPrintLogsToConsole(true)
        .build()
}

fun logInfo() {
    applicationLogger.info("Logging info")
}

fun logErrorWithThrowable() {
    applicationLogger.error(
        "Logging error with Throwable",
        throwable = RuntimeException("Just for logging!"),
        attributes = mapOf("custom" to "attribute")
    )
}

internal expect fun platformSpecificSetup(rumConfigurationBuilder: RumConfiguration.Builder)
