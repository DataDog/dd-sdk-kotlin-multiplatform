/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.webview

import com.datadog.kmp.ios.cinterop.webview.DDWebViewTracking
import platform.WebKit.WKWebView

/**
 * Real User Monitoring allows you to monitor web views and eliminate blind spots in your hybrid iOS applications.
 *
 * # Prerequisites:
 * Set up the web page you want rendered on your mobile iOS and tvOS application with the RUM Browser SDK
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
     * The WebView events will not be tracked unless the web page's URL Host is part of
     * the list of allowed hosts.
     *
     * Note: [disable] must be called when [WKWebView] is de-initialized.
     *
     * @param webView the webView on which to attach the bridge.
     * @param allowedHosts a list of all the hosts that you want to track when loaded in the
     * WebView (e.g.: `listOf("example.com", "example.net")`).
     * @param logsSampleRate the sample rate for logs coming from the WebView, in percent. A value of `30` means we'll
     * send 30% of the logs. If value is `0`, no logs will be sent to Datadog. Default is 100.0 (ie: all logs are sent).
     */
    fun enable(webView: WKWebView, allowedHosts: Set<String>, logsSampleRate: Float = 100f) {
        DDWebViewTracking.enableWithWebView(webView, allowedHosts, logsSampleRate)
    }

    /**
     * Disables Datadog iOS SDK and Datadog Browser SDK integration.
     *
     * Removes Datadog's ScriptMessageHandler and UserScript from the caller.
     * Note: This method **must** be called when the webview can be de-initialized.
     *
     * @param webView: The web-view to stop tracking.
     */
    fun disable(webView: WKWebView) = DDWebViewTracking.disableWithWebView(webView)
}
