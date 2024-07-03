/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import android.webkit.WebView
import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.trackNonFatalAnrs
import com.datadog.kmp.rum.configuration.trackUserInteractions
import com.datadog.kmp.rum.configuration.useViewTrackingStrategy
import com.datadog.kmp.webview.WebViewTracking

internal actual fun platformSpecificSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    with(rumConfigurationBuilder) {
        // going to use NavigationViewTrackingEffect
        useViewTrackingStrategy(null)
        trackUserInteractions()
        trackNonFatalAnrs(true)
    }
}

actual fun startWebViewTracking(webView: Any) {
    WebViewTracking.enable(webView as WebView, WEB_VIEW_TRACKING_ALLOWED_HOSTS)
}

actual fun stopWebViewTracking(webView: Any) {
    // no-op
}
