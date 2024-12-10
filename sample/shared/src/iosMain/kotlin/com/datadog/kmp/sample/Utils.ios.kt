/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.trackUiKitActions
import com.datadog.kmp.sessionreplay.SessionReplay
import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
import com.datadog.kmp.sessionreplay.configuration.SessionReplayConfiguration
import com.datadog.kmp.sessionreplay.configuration.TextAndInputPrivacy
import com.datadog.kmp.sessionreplay.configuration.TouchPrivacy
import com.datadog.kmp.webview.WebViewTracking
import platform.WebKit.WKWebView

actual fun startWebViewTracking(webView: Any) {
    WebViewTracking.enable(webView as WKWebView, WEB_VIEW_TRACKING_ALLOWED_HOSTS)
}

actual fun stopWebViewTracking(webView: Any) {
    WebViewTracking.disable(webView as WKWebView)
}

internal actual fun setupUiKitActionsTracking(rumConfigurationBuilder: RumConfiguration.Builder) {
    // interface for UIKit actions tracking is different for tvOS and iOS
    rumConfigurationBuilder.trackUiKitActions()
}

@Suppress("MagicNumber")
internal actual fun initSessionReplay() {
    SessionReplay.enable(
        SessionReplayConfiguration.Builder(100f)
            .setImagePrivacy(ImagePrivacy.MASK_LARGE_ONLY)
            .setTouchPrivacy(TouchPrivacy.SHOW)
            .setTextAndInputPrivacy(TextAndInputPrivacy.MASK_SENSITIVE_INPUTS)
            .build()
    )
}
