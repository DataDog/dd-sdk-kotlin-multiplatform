/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.datadog.kmp.sample.PlatformActions
import platform.UIKit.UIViewController

/**
 * Creates the view controller hosting the shared sample UI.
 *
 * @param platformActions actions implemented in Swift, displayed along with the shared ones.
 */
@Suppress("FunctionNaming", "FunctionName")
fun MainViewController(platformActions: PlatformActions): UIViewController =
    ComposeUIViewController { SampleApp(platformActions) }
