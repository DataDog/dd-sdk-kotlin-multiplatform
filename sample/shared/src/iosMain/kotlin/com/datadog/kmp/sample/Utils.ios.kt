/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.setAppHangThreshold
import com.datadog.kmp.rum.configuration.trackUiKitActions
import com.datadog.kmp.rum.configuration.trackUiKitViews
import com.datadog.kmp.webview.WebViewTracking
import platform.WebKit.WKWebView

actual fun startWebViewTracking(webView: Any) {
    WebViewTracking.enable(webView as WKWebView, WEB_VIEW_TRACKING_ALLOWED_HOSTS)
}
actual fun stopWebViewTracking(webView: Any) {
    WebViewTracking.disable(webView as WKWebView)
}

internal actual fun platformSpecificSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    with(rumConfigurationBuilder) {
        trackUiKitViews()
        trackUiKitActions()
        setAppHangThreshold(APP_HANG_THRESHOLD_MS)
    }
}

const val APP_HANG_THRESHOLD_MS = 100L
