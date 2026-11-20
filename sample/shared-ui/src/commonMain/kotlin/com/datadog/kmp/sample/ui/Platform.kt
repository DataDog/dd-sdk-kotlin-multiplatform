/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

/**
 * Tracks the destinations of the given [navController] as RUM views.
 *
 * @param onViewStarted called after each RUM view is started, for example to add attributes to it.
 */
@Composable
internal expect fun RumViewTrackingEffect(navController: NavHostController, onViewStarted: () -> Unit)

/**
 * Masks all the text of the element in Session Replay, when the platform supports it for Compose.
 */
internal expect fun Modifier.maskAllTextInSessionReplay(): Modifier

/**
 * Top app bar following the platform conventions: start-aligned title with an "up" arrow on Android, centered title
 * with a "Back" button on iOS.
 */
@Composable
internal expect fun SampleTopBar(title: String, isBackNavigationAvailable: Boolean, onNavigateBack: () -> Unit)

/**
 * Native WebView with WebView tracking enabled, which loads [com.datadog.kmp.sample.WEB_VIEW_TRACKING_LOAD_URL].
 */
@Composable
internal expect fun TrackedWebView(modifier: Modifier)
