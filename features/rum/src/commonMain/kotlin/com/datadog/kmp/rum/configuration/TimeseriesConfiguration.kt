/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

/**
 * Configuration for memory and CPU timeseries collection.
 *
 * @param enabledTypes the timeseries types to collect. By default, all supported
 * timeseries types are collected. Passing an empty set disables collection of every
 * timeseries type.
 */
data class TimeseriesConfiguration(
    val enabledTypes: Set<TimeseriesType> = TimeseriesType.entries.toSet()
) {

    companion object {
        /**
         * A default [TimeseriesConfiguration] built with all default settings.
         */
        val DEFAULT: TimeseriesConfiguration = TimeseriesConfiguration()
    }
}

/**
 * Type of device timeseries that can be collected by RUM.
 */
enum class TimeseriesType {

    /** CPU usage timeseries. */
    CPU,

    /** Memory usage timeseries. */
    MEMORY
}
