/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

import com.datadog.kmp.log.internal.PlatformLogger
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertSame

class LoggerBuilderTest {

    private val mockPlatformLoggerBuilder = mock<PlatformLogger.Builder>()

    private val testedLoggerBuilder = Logger.Builder(mockPlatformLoggerBuilder)

    @Test
    fun `M return PlatformLogger W build`() {
        // Given
        val mockPlatformLogger = mock<PlatformLogger>()
        every { mockPlatformLoggerBuilder.build() } calls { mockPlatformLogger }

        // When
        val logger = testedLoggerBuilder.build()

        // Then
        assertSame(mockPlatformLogger, logger.platformLogger)
    }

    @Test
    fun `M call platform logger builder+setService W setService`() {
        // Given
        val fakeService = "fakeService"

        // When
        testedLoggerBuilder.setService(fakeService)

        // Then
        verify {
            mockPlatformLoggerBuilder.setService(fakeService)
        }
    }

    @Test
    fun `M call platform logger builder+setRemoteLogThreshold W setRemoteLogThreshold`() {
        // Given
        val fakeRemoteLogThreshold = LogLevel.DEBUG

        // When
        testedLoggerBuilder.setRemoteLogThreshold(fakeRemoteLogThreshold)

        // Then
        verify {
            mockPlatformLoggerBuilder.setRemoteLogThreshold(fakeRemoteLogThreshold)
        }
    }

    @Test
    fun `M call platform logger builder+setPrintLogsToConsole W setPrintLogsToConsole`() {
        // Given
        val fakePrintsLogsToConsole = true

        // When
        testedLoggerBuilder.setPrintLogsToConsole(fakePrintsLogsToConsole)

        // Then
        verify {
            mockPlatformLoggerBuilder.setPrintLogsToConsole(fakePrintsLogsToConsole)
        }
    }

    @Test
    fun `M call platform logger builder+setNetworkInfoEnabled W setNetworkInfoEnabled`() {
        // Given
        val fakeNetworkInfoEnabled = true

        // When
        testedLoggerBuilder.setNetworkInfoEnabled(fakeNetworkInfoEnabled)

        // Then
        verify {
            mockPlatformLoggerBuilder.setNetworkInfoEnabled(fakeNetworkInfoEnabled)
        }
    }

    @Test
    fun `M call platform logger builder+setName W setName`() {
        // Given
        val fakeName = "fakeName"

        // When
        testedLoggerBuilder.setName(fakeName)

        // Then
        verify {
            mockPlatformLoggerBuilder.setName(fakeName)
        }
    }

    @Test
    fun `M call platform logger builder+setBundleWithTraceEnabled W setBundleWithTraceEnabled`() {
        // Given
        val fakeBundleWithTraceEnabled = true

        // When
        testedLoggerBuilder.setBundleWithTraceEnabled(fakeBundleWithTraceEnabled)

        // Then
        verify {
            mockPlatformLoggerBuilder.setBundleWithTraceEnabled(fakeBundleWithTraceEnabled)
        }
    }

    @Test
    fun `M call platform logger builder+setBundleWithRumEnabled W setBundleWithRumEnabled`() {
        // Given
        val fakeBundleWithRumEnabled = true

        // When
        testedLoggerBuilder.setBundleWithRumEnabled(fakeBundleWithRumEnabled)

        // Then
        verify {
            mockPlatformLoggerBuilder.setBundleWithRumEnabled(fakeBundleWithRumEnabled)
        }
    }

    @Test
    fun `M call platform logger builder+setRemoteSampleRate W setRemoteSampleRate`() {
        // Given
        val fakeRemoteSampleRate = 42f

        // When
        testedLoggerBuilder.setRemoteSampleRate(fakeRemoteSampleRate)

        // Then
        verify {
            mockPlatformLoggerBuilder.setRemoteSampleRate(fakeRemoteSampleRate)
        }
    }
}
