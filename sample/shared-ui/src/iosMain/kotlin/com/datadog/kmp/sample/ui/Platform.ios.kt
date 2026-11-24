/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.navigation.NavHostController
import com.datadog.kmp.sample.WEB_VIEW_TRACKING_LOAD_URL
import com.datadog.kmp.sample.startWebViewTracking
import com.datadog.kmp.sample.stopWebViewTracking
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@Composable
internal actual fun RumViewTrackingEffect(navController: NavHostController, onViewStarted: () -> Unit) {
    // Compose UI is rendered on a single canvas, so the iOS SDK can't track its screens automatically
    ManualRumViewTrackingEffect(navController, onViewStarted)
}

// Session Replay for iOS doesn't support Compose Multiplatform
internal actual fun Modifier.maskAllTextInSessionReplay(): Modifier = this

@Composable
internal actual fun TrackedWebView(modifier: Modifier) {
    val webView = remember {
        WKWebView(frame = CGRectZero.readValue(), configuration = WKWebViewConfiguration())
    }
    DisposableEffect(webView) {
        startWebViewTracking(webView)
        NSURL.URLWithString(WEB_VIEW_TRACKING_LOAD_URL)?.let {
            webView.loadRequest(NSURLRequest.requestWithURL(it))
        }
        onDispose { stopWebViewTracking(webView) }
    }
    UIKitView(factory = { webView }, modifier = modifier)
}
