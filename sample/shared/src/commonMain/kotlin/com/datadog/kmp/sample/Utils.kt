/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */
@file:Suppress("TooManyFunctions")

package com.datadog.kmp.sample

import com.datadog.kmp.Datadog
import com.datadog.kmp.DatadogSite
import com.datadog.kmp.SdkLogVerbosity
import com.datadog.kmp.core.configuration.BatchProcessingLevel
import com.datadog.kmp.core.configuration.BatchSize
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.core.configuration.UploadFrequency
import com.datadog.kmp.log.Logger
import com.datadog.kmp.log.Logs
import com.datadog.kmp.log.configuration.LogsConfiguration
import com.datadog.kmp.privacy.TrackingConsent
import com.datadog.kmp.rum.Rum
import com.datadog.kmp.rum.RumActionType
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.sessionreplay.SessionReplay
import com.datadog.kmp.sessionreplay.configuration.SessionReplayConfiguration

const val HOME_SCREEN_NAME = "Home"
const val LOGS_SCREEN_NAME = "Logs"
const val CRASH_SCREEN_NAME = "Crash"
const val RUM_SCREEN_NAME = "RUM"
const val WEBVIEW_SCREEN_NAME = "WebView"

internal val WEB_VIEW_TRACKING_ALLOWED_HOSTS = setOf("datadoghq.dev")
const val WEB_VIEW_TRACKING_LOAD_URL = "https://datadoghq.dev/browser-sdk-test-playground/" +
    "?client_token=${LibraryConfig.DD_CLIENT_TOKEN}" +
    "&application_id=${LibraryConfig.DD_APPLICATION_ID}" +
    "&site=datadoghq.com"

@Suppress("MagicNumber")
fun initDatadog(context: Any? = null) {
    Datadog.verbosity = SdkLogVerbosity.DEBUG

    val configuration = Configuration.Builder(
        clientToken = LibraryConfig.DD_CLIENT_TOKEN,
        env = "prod"
    )
        .trackCrashes(true)
        .useSite(DatadogSite.US1)
        .setBatchSize(BatchSize.MEDIUM)
        .setUploadFrequency(UploadFrequency.AVERAGE)
        .setBatchProcessingLevel(BatchProcessingLevel.MEDIUM)
        .build()

    Datadog.initialize(context = context, configuration = configuration, trackingConsent = TrackingConsent.GRANTED)

    val logsConfiguration = LogsConfiguration.Builder()
        .setEventMapper {
            it.ddtags += ",mapped_tag:mapped_value"
            it
        }
        .build()
    Logs.enable(logsConfiguration)

    val rumConfiguration = RumConfiguration.Builder(LibraryConfig.DD_APPLICATION_ID)
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
            setupRumMappers()
        }
        .apply {
            platformSpecificSetup(this)
        }
        .build()
    Rum.enable(rumConfiguration)

    SessionReplay.enable(
        SessionReplayConfiguration.Builder(100f)
            .apply {
                platformSpecificSetup(this)
            }
            .build()
    )

    Datadog.setUserInfo(
        name = "Random User",
        email = "user@example.com",
        extraInfo = mapOf(
            "age" to 42,
            "location" to "universe",
            "boolean-attribute" to true,
            "null-attribute" to null,
            "boolean-attribute" to true
        )
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
        attributes = mapOf(
            "custom" to "attribute",
            "null-attribute" to null
        )
    )
}

fun trackView(viewName: String) {
    RumMonitor.get().startView(
        viewName,
        viewName,
        mapOf(
            "custom-view-attribute" to "view-attribute-value",
            "boolean-view-attribute" to true,
            "nullable-view-attribute" to null
        )
    )
}

fun trackAction(actionName: String) {
    RumMonitor.get().addAction(
        RumActionType.TAP,
        actionName,
        mapOf(
            "custom-action-attribute" to "action-attribute-value",
            "boolean-action-attribute" to true,
            "nullable-action-attribute" to null
        )
    )
}

@Suppress("unused")
@Throws(RuntimeException::class)
fun triggerCheckedException() {
    triggerUncheckedException()
}

@Suppress("unused", "TooGenericExceptionThrown")
fun triggerUncheckedException() {
    val cause = IllegalStateException("sample crash")
    throw RuntimeException(cause)
}

private fun RumConfiguration.Builder.setupRumMappers() {
    // TODO RUM-5992 additionalProperties cannot be mutable because of iOS SDK API, so using a referrer for
    //  the mapping example
    val referrer = "https://www.datadoghq.com"
    setViewEventMapper {
        it.view.referrer = referrer
        it
    }
    setResourceEventMapper {
        it.view.referrer = referrer
        it
    }
    setActionEventMapper {
        it.view.referrer = referrer
        it
    }
    setErrorEventMapper {
        it.view.referrer = referrer
        it
    }
    setLongTaskEventMapper {
        it.view.referrer = referrer
        it
    }
}

expect fun startWebViewTracking(webView: Any)
expect fun stopWebViewTracking(webView: Any)
internal expect fun platformSpecificSetup(rumConfigurationBuilder: RumConfiguration.Builder)
internal expect fun platformSpecificSetup(sessionReplayConfigurationBuilder: SessionReplayConfiguration.Builder)
