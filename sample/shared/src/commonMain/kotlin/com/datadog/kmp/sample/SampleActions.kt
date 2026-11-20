/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.sample.network.startGetRequest
import com.datadog.kmp.sample.network.startPostRequest
import kotlin.random.Random

/**
 * Screens of the sample apps. The name is also used as the RUM view name and the navigation route.
 */
enum class SampleScreen(val screenName: String) {
    HOME("Home"),
    LOGS("Logs"),
    CRASH("Crash"),
    RUM("RUM"),
    WEBVIEW("WebView")
}

/**
 * Color of the button of a [SampleAction]. [argb] is used by the Compose UIs, SwiftUI maps it to its system colors.
 */
@Suppress("MagicNumber")
enum class SampleActionColor(val argb: Long) {
    BLUE(0xFF0000FF),
    RED(0xFFFF0000),
    BROWN(0xFF996633),
    ORANGE(0xFFFFA500),
    MAGENTA(0xFFFF00FF),
    TEAL(0xFF03DAC5),
    BLACK(0xFF000000)
}

/**
 * Scenario of the sample apps, displayed as a button.
 *
 * @param actionName name of the RUM action tracked when the scenario is performed.
 * @param title text of the button.
 * @param color color of the button.
 * @param block the scenario itself.
 */
class SampleAction(
    val actionName: String,
    val title: String,
    val color: SampleActionColor,
    private val block: () -> Unit
) {
    constructor(actionName: String, color: SampleActionColor, block: () -> Unit) :
        this(actionName, actionName, color, block)

    /**
     * Tracks a RUM action named [actionName] and runs the scenario.
     */
    fun perform() {
        trackAction(actionName)
        block()
    }
}

/**
 * Scenarios which can only be implemented in the native platform code (for example, catching a Kotlin exception in
 * Swift or triggering a native crash), grouped by the screen they belong to. They are displayed after the ones from
 * [SampleActions].
 */
class PlatformActions(
    val logs: List<SampleAction>,
    val crash: List<SampleAction>,
    val rum: List<SampleAction>
) {
    companion object {
        val NONE: PlatformActions = PlatformActions(emptyList(), emptyList(), emptyList())
    }
}

/**
 * Scenarios shared by all the sample apps, grouped by the screen they belong to.
 */
object SampleActions {

    private const val HTTPBIN_URL = "https://httpbin.org"

    val logs: List<SampleAction> = listOf(
        SampleAction("Log info", SampleActionColor.BLUE) { logInfo() },
        SampleAction("Log error with Throwable", SampleActionColor.RED) { logErrorWithThrowable() }
    )

    val crash: List<SampleAction> = listOf(
        SampleAction("Crash: unchecked KMP exception", SampleActionColor.RED) { triggerUncheckedException() }
    )

    val rumNetwork: List<SampleAction> = listOf(
        SampleAction("Start GET request", SampleActionColor.ORANGE) {
            val url = if (Random.nextBoolean()) "$HTTPBIN_URL/get" else "$HTTPBIN_URL/redirect-to?url=get"
            startGetRequest(url)
        },
        SampleAction("Start POST request", SampleActionColor.ORANGE) {
            startPostRequest("$HTTPBIN_URL/post", "Some payload")
        },
        SampleAction("Start server error request", SampleActionColor.ORANGE) {
            startGetRequest("$HTTPBIN_URL/status/500")
        },
        SampleAction("Start network error request", SampleActionColor.ORANGE) {
            startGetRequest("https://some-domain.in-non-existing-zone")
        }
    )

    const val RUM_FEATURE_OPERATIONS_TITLE: String = "Feature Operations"

    val rumFeatureOperations: List<SampleAction> = listOf(
        SampleAction("Start Feature Operation", "Start", SampleActionColor.MAGENTA) { startFeatureOperation() },
        SampleAction("Stop Feature Operation successfully", "Stop successfully", SampleActionColor.TEAL) {
            stopFeatureOperation()
        },
        SampleAction("Stop Feature Operation unsuccessfully", "Stop unsuccessfully", SampleActionColor.RED) {
            failFeatureOperation()
        }
    )
}
