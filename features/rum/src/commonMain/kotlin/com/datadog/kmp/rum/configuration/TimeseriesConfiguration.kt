/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.rum.ExperimentalRumApi

/**
 * Configuration for memory and CPU timeseries collection.
 *
 * Use [Builder] to create an instance.
 */
@ExperimentalRumApi
class TimeseriesConfiguration internal constructor(
    internal val enabledTypes: Set<TimeseriesType>
) {

    /**
     * A Builder for [TimeseriesConfiguration].
     */
    @ExperimentalRumApi
    class Builder {

        private var enabledTypes: Set<TimeseriesType> = TimeseriesType.entries.toSet()

        /**
         * Restricts collection to the provided timeseries types.
         *
         * By default, all supported timeseries types are collected.
         * Passing an empty array disables collection of every timeseries type.
         *
         * @param types the timeseries types to collect.
         */
        fun collectTypes(vararg types: TimeseriesType): Builder = apply {
            enabledTypes = types.toSet()
        }

        /** Builds a [TimeseriesConfiguration] from the current builder state. */
        fun build(): TimeseriesConfiguration = TimeseriesConfiguration(
            enabledTypes = enabledTypes
        )
    }

    companion object {

        /** Default [TimeseriesConfiguration] built with all default settings. */
        @ExperimentalRumApi
        val DEFAULT: TimeseriesConfiguration = Builder().build()
    }
}

/**
 * Type of device timeseries that can be collected by RUM.
 */
@ExperimentalRumApi
enum class TimeseriesType {

    /** CPU usage timeseries. */
    CPU,

    /** Memory usage timeseries. */
    MEMORY
}
