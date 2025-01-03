/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import android.webkit.WebView
import com.datadog.android.sessionreplay.compose.ComposeExtensionSupport
import com.datadog.android.sessionreplay.compose.ExperimentalSessionReplayApi
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.trackNonFatalAnrs
import com.datadog.kmp.rum.configuration.trackUserInteractions
import com.datadog.kmp.rum.configuration.useViewTrackingStrategy
import com.datadog.kmp.sessionreplay.SessionReplay
import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
import com.datadog.kmp.sessionreplay.configuration.SessionReplayConfiguration
import com.datadog.kmp.sessionreplay.configuration.TextAndInputPrivacy
import com.datadog.kmp.sessionreplay.configuration.TouchPrivacy
import com.datadog.kmp.sessionreplay.configuration.addExtensionSupport
import com.datadog.kmp.webview.WebViewTracking

internal actual fun platformSpecificSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    with(rumConfigurationBuilder) {
        // going to use NavigationViewTrackingEffect
        useViewTrackingStrategy(null)
        trackUserInteractions()
        trackNonFatalAnrs(true)
    }
}

internal actual fun platformSpecificSetup(configurationBuilder: Configuration.Builder) {
    // nothing
}

actual fun startWebViewTracking(webView: Any) {
    WebViewTracking.enable(webView as WebView, WEB_VIEW_TRACKING_ALLOWED_HOSTS)
}

actual fun stopWebViewTracking(webView: Any) {
    // no-op
}

@Suppress("MagicNumber")
@OptIn(ExperimentalSessionReplayApi::class)
internal actual fun initSessionReplay() {
    // alternatively it is possible to use classes from com.datadog.android.sessionreplay package
    SessionReplay.enable(
        SessionReplayConfiguration.Builder(100f)
            .setImagePrivacy(ImagePrivacy.MASK_LARGE_ONLY)
            .setTouchPrivacy(TouchPrivacy.SHOW)
            .setTextAndInputPrivacy(TextAndInputPrivacy.MASK_SENSITIVE_INPUTS)
            .addExtensionSupport(ComposeExtensionSupport())
            .build()
    )
}
