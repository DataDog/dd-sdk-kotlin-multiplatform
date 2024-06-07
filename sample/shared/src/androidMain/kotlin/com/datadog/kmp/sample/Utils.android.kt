/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sample

import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.trackNonFatalAnrs
import com.datadog.kmp.rum.configuration.trackUserInteractions
import com.datadog.kmp.rum.configuration.useViewTrackingStrategy
import com.datadog.kmp.rum.tracking.ActivityViewTrackingStrategy

internal actual fun platformSpecificSetup(rumConfigurationBuilder: RumConfiguration.Builder) {
    with(rumConfigurationBuilder) {
        useViewTrackingStrategy(ActivityViewTrackingStrategy(trackExtras = true))
        trackUserInteractions()
        trackNonFatalAnrs(true)
    }
}
