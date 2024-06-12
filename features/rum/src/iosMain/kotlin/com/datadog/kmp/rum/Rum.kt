/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum

import cocoapods.DatadogObjc.DDRUM
import cocoapods.DatadogObjc.DDRUMConfiguration
import com.datadog.kmp.rum.configuration.RumConfiguration

/**
 * An entry point to Datadog RUM feature.
 */
actual object Rum {
    /**
     * Enables a RUM feature based on the configuration provided and registers RUM monitor.
     *
     * @param rumConfiguration Configuration to use for the feature.
     */
    actual fun enable(rumConfiguration: RumConfiguration) {
        DDRUM.enableWith(rumConfiguration.nativeConfiguration as DDRUMConfiguration)
    }
}
