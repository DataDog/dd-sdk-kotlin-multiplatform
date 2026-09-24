/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.rum.ExperimentalRumApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalRumApi::class)
class TimeseriesConfigurationBuilderTest {

    private val testedBuilder = TimeseriesConfiguration.Builder()

    @Test
    fun `M enable all types W build - no collectTypes call`() {
        // When
        val configuration = testedBuilder.build()

        // Then
        assertEquals(TimeseriesType.entries.toSet(), configuration.enabledTypes)
    }

    @Test
    fun `M restrict enabled types W collectTypes + build`() {
        // When
        val configuration = testedBuilder.collectTypes(TimeseriesType.CPU).build()

        // Then
        assertEquals(setOf(TimeseriesType.CPU), configuration.enabledTypes)
    }

    @Test
    fun `M disable all types W collectTypes - empty + build`() {
        // When
        val configuration = testedBuilder.collectTypes().build()

        // Then
        assertEquals(emptySet(), configuration.enabledTypes)
    }

    @Test
    fun `M enable all types W DEFAULT`() {
        // When
        val configuration = TimeseriesConfiguration.DEFAULT

        // Then
        assertEquals(TimeseriesType.entries.toSet(), configuration.enabledTypes)
    }
}
