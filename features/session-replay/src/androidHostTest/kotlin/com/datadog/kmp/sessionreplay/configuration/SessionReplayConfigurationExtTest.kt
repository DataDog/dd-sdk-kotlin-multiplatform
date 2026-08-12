/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration

import com.datadog.kmp.sessionreplay.configuration.internal.AndroidSessionReplayConfigurationBuilder
import fr.xgouchet.elmyr.annotation.BoolForgery
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
class SessionReplayConfigurationExtTest {

    @Mock
    private lateinit var mockPlatformBuilder: AndroidSessionReplayConfigurationBuilder

    @Test
    fun `M call platform builder and return same builder W setHeatmapsEnabled`(
        @BoolForgery enabled: Boolean
    ) {
        // Given
        val testedBuilder = SessionReplayConfiguration.Builder(mockPlatformBuilder)

        // When
        val result = testedBuilder.setHeatmapsEnabled(enabled)

        // Then
        verify(mockPlatformBuilder).setHeatmapsEnabled(enabled)
        assertThat(result).isSameAs(testedBuilder)
    }
}
