/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.configuration

import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.log.configuration.internal.PlatformLogsConfigurationBuilder
import com.datadog.kmp.log.model.LogEvent
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test

class LogsConfigurationBuilderTest {

    private val mockPlatformLogsConfigurationBuilder = mock<PlatformLogsConfigurationBuilder<Any>>()

    private val testedLogsConfigurationBuilder = LogsConfiguration.Builder(mockPlatformLogsConfigurationBuilder)

    @Test
    fun `M return LogsConfiguration W build`() {
        // Given
        val fakeNativeConfiguration = Any()
        every { mockPlatformLogsConfigurationBuilder.build() } returns fakeNativeConfiguration

        // When
        val logsConfiguration = testedLogsConfigurationBuilder.build()

        // Then
        kotlin.test.assertSame(fakeNativeConfiguration, logsConfiguration.nativeConfiguration)
    }

    @Test
    fun `M call platform Logs configuration builder+setEventMapper W setEventMapper`() {
        // Given
        val fakeLogsEventMapper = EventMapper<LogEvent> { it }

        // When
        testedLogsConfigurationBuilder.setEventMapper(fakeLogsEventMapper)

        // Then
        verify {
            mockPlatformLogsConfigurationBuilder.setEventMapper(fakeLogsEventMapper)
        }
    }
}
