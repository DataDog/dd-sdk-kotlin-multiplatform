/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

/**
 * Defines the frequency at which mobile vitals monitor updates the data.
 */
enum class VitalsUpdateFrequency {
    /** Every 100 milliseconds. */
    FREQUENT,

    /** Every 500 milliseconds. This is the default frequency. */
    AVERAGE,

    /** Every 1000 milliseconds. */
    RARE,

    /** No data will be sent for mobile vitals. */
    NEVER
}
