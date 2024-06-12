/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum

import com.datadog.kmp.rum.configuration.RumConfiguration

/**
 * An entry point to Datadog RUM feature.
 */
expect object Rum {

    /**
     * Enables a RUM feature based on the configuration provided and registers RUM monitor.
     *
     * @param rumConfiguration Configuration to use for the feature.
     */
    fun enable(rumConfiguration: RumConfiguration)
}
