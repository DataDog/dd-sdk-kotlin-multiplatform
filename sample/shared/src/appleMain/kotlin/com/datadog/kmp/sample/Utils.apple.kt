/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.core.configuration.enableBackgroundTasks
import com.datadog.kmp.rum.ExperimentalRumApi
import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.setAppHangThreshold
import com.datadog.kmp.rum.configuration.trackSwiftUIViews
import com.datadog.kmp.rum.configuration.trackWatchdogTerminations

@OptIn(ExperimentalRumApi::class)
internal actual fun platformSpecificSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    with(rumConfigurationBuilder) {
        // uncomment the following if your application is UIKit-based
        // trackUiKitViews()
        // setupUiKitActionsTracking(this)
        trackSwiftUIViews()
        // SwiftUI actions detection is not capable of capturing labels yet, so we will track them manually
        // trackSwiftUIActions(isLegacyDetectionEnabled = true)
        setAppHangThreshold(APP_HANG_THRESHOLD_MS)
        trackWatchdogTerminations(true)
    }
}

internal actual fun platformSpecificSetup(configurationBuilder: Configuration.Builder) {
    configurationBuilder.enableBackgroundTasks(true)
}

const val APP_HANG_THRESHOLD_MS = 100L

internal expect fun setupUiKitActionsTracking(rumConfigurationBuilder: RumConfiguration.Builder)
