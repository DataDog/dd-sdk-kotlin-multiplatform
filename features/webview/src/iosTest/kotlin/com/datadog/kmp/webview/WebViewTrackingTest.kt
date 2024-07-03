/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.webview

import platform.WebKit.WKWebView
import kotlin.test.Test

class WebViewTrackingTest {

    // Since we are using a custom header for the interop, the point of these tests is just to check than linking
    // is done correctly by verifying calls don't crash

    @Test
    fun `M dispatch enable call to linked iOS SDK W enable`() {
        // When
        WebViewTracking.enable(WKWebView(), allowedHosts = emptySet(), logsSampleRate = 100f)
    }

    @Test
    fun `M dispatch disable call to linked iOS SDK W disable`() {
        // When
        WebViewTracking.disable(WKWebView())
    }
}
