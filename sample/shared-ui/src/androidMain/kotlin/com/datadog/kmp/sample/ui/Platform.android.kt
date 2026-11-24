/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.datadog.android.compose.ExperimentalTrackingApi
import com.datadog.android.compose.NavigationViewTrackingEffect
import com.datadog.android.sessionreplay.TextAndInputPrivacy
import com.datadog.android.sessionreplay.compose.sessionReplayTextAndInputPrivacy
import com.datadog.kmp.sample.createTrackedWebView

@OptIn(ExperimentalTrackingApi::class)
@Composable
internal actual fun RumViewTrackingEffect(navController: NavHostController, onViewStarted: () -> Unit) {
    // On Android, Compose Multiplatform navigation is backed by AndroidX Navigation, so the dedicated
    // integration from dd-sdk-android-compose can be used. It also handles the app lifecycle.
    NavigationViewTrackingEffect(navController = navController)
    // NavigationViewTrackingEffect starts views from a destination-changed listener, which NavController notifies
    // before emitting to currentBackStackEntryFlow
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { onViewStarted() }
    }
    // NavigationViewTrackingEffect also stops the view on ON_PAUSE and starts a new one on ON_RESUME. This observer is
    // added after its own one, so it is notified after the new view is started.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        // the ON_RESUME replayed when the observer is added comes before the first screen view is started (the
        // navigation graph isn't set yet), so only an ON_RESUME following an ON_PAUSE is a view restart
        var isPaused = false
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isPaused = true
            } else if (event == Lifecycle.Event.ON_RESUME && isPaused) {
                isPaused = false
                onViewStarted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

internal actual fun Modifier.maskAllTextInSessionReplay(): Modifier =
    sessionReplayTextAndInputPrivacy(TextAndInputPrivacy.MASK_ALL)

@Composable
internal actual fun TrackedWebView(modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = ::createTrackedWebView,
        // a new WebView is created each time the screen is shown, so release the old one when leaving it
        onRelease = { webView -> webView.destroy() }
    )
}
