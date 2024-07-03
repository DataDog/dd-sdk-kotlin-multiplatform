/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.webview

import android.webkit.WebView
import com.datadog.android.webview.WebViewTracking as NativeWebViewTracking

/**
 * Real User Monitoring allows you to monitor web views and eliminate blind spots in your hybrid iOS applications.
 *
 * # Prerequisites:
 * Set up the web page you want rendered on your mobile Android application with the RUM Browser SDK
 * first. For more information, see [RUM Browser Monitoring](https://docs.datadoghq.com/real_user_monitoring/browser/#npm).
 *
 * You can perform the following:
 * - Track user journeys across web and native components in mobile applications
 * - Scope the root cause of latency to web pages or native components in mobile applications
 * - Support users that have difficulty loading web pages on mobile devices
 */
object WebViewTracking {

    /**
     * Enables SDK to correlate Datadog RUM events and Logs from the WebView with native RUM session.
     *
     * If the content loaded in WebView uses Datadog Browser SDK (`v4.2.0+`) and matches specified
     * `hosts`, web events will be correlated with the RUM session from native SDK.
     *
     * Please note that:
     * - you need to enable the JavaScript support in the WebView settings for this feature
     * to be functional:
     * ```
     * webView.settings.javaScriptEnabled = true
     * ```
     * - by default, navigation will happen outside of your application (in a browser or a different app). To prevent
     * that and ensure Datadog can track the full WebView user journey, attach a [android.webkit.WebViewClient] to your
     * [WebView], as following:
     * ```
     * webView.webViewClient = WebViewClient()
     * ```
     * The WebView events will not be tracked unless the web page's URL Host is part of
     * the list of allowed hosts.
     *
     * @param webView the webView on which to attach the bridge.
     * @param allowedHosts a set of all the hosts that you want to track when loaded in the
     * WebView (e.g.: `setOf("example.com", "example.net")`).
     * @param logsSampleRate the sample rate for logs coming from the WebView, in percent. A value of `30` means we'll
     * send 30% of the logs. If value is `0`, no logs will be sent to Datadog. Default is 100.0 (ie: all logs are sent).
     */
    fun enable(
        webView: WebView,
        allowedHosts: Set<String>,
        logsSampleRate: Float = 100f
    ) = NativeWebViewTracking.enable(webView, allowedHosts.toList(), logsSampleRate)
}
