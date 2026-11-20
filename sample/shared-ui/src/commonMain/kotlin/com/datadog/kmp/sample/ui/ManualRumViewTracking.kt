/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.datadog.kmp.rum.RumMonitor

/**
 * Tracks navigation destinations as RUM views using the manual [RumMonitor.startView] and [RumMonitor.stopView] API.
 *
 * The active view is stopped when the app goes to the background and started again when it comes back to the
 * foreground, so its duration only covers the time the screen is actually visible.
 *
 * Can be used on any platform. Android has a dedicated integration instead (`NavigationViewTrackingEffect` from
 * `dd-sdk-android-compose`).
 *
 * @param onViewStarted called right after each view is started, for example to add attributes to it.
 */
@Composable
internal fun ManualRumViewTrackingEffect(navController: NavHostController, onViewStarted: () -> Unit) {
    val tracker = remember { ActiveViewTracker(onViewStarted) }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            tracker.navigateTo(key = entry.id, name = entry.destination.route ?: return@collect)
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        // when added, the observer also receives the events up to the current state, so ON_START comes right away
        // if the app is already in the foreground
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                tracker.onForeground()
            } else if (event == Lifecycle.Event.ON_STOP) {
                tracker.onBackground()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            tracker.onBackground()
        }
    }
}

private class ActiveViewTracker(private val onViewStarted: () -> Unit) {
    private var currentKey: String? = null
    private var currentName: String? = null
    private var isInForeground = false
    private var isViewActive = false

    fun navigateTo(key: String, name: String) {
        if (key == currentKey) return
        stopActiveView()
        currentKey = key
        currentName = name
        if (isInForeground) startCurrentView()
    }

    fun onForeground() {
        isInForeground = true
        if (!isViewActive) startCurrentView()
    }

    fun onBackground() {
        isInForeground = false
        stopActiveView()
    }

    private fun startCurrentView() {
        val key = currentKey ?: return
        val name = currentName ?: return
        RumMonitor.get().startView(key, name)
        isViewActive = true
        onViewStarted()
    }

    private fun stopActiveView() {
        if (!isViewActive) return
        currentKey?.let { RumMonitor.get().stopView(it) }
        isViewActive = false
    }
}
