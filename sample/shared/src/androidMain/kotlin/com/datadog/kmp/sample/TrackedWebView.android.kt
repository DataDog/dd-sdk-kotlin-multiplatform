/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Creates a [WebView] with WebView tracking enabled, which loads [WEB_VIEW_TRACKING_LOAD_URL].
 */
@SuppressLint("SetJavaScriptEnabled")
fun createTrackedWebView(context: Context): WebView = WebView(context).apply {
    settings.javaScriptEnabled = true
    webViewClient = WebViewClient()

    settings.loadWithOverviewMode = true
    settings.useWideViewPort = true
    settings.setSupportZoom(true)
    startWebViewTracking(this)
    loadUrl(WEB_VIEW_TRACKING_LOAD_URL)
}
