/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.internal

import android.util.Log
import com.datadog.android.log.Logger
import com.datadog.kmp.log.LogLevel
import com.datadog.tools.unit.forge.BaseConfigurator
import fr.xgouchet.elmyr.annotation.BoolForgery
import fr.xgouchet.elmyr.annotation.FloatForgery
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.annotation.StringForgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(BaseConfigurator::class)
@Suppress("FunctionNaming", "TooManyFunctions")
internal class AndroidPlatformLoggerBuilderTest {

    private lateinit var testedPlatformLoggerBuilder: AndroidPlatformLogger.Builder

    @Mock
    lateinit var mockNativeLoggerBuilder: Logger.Builder

    @BeforeEach
    fun `set up`() {
        testedPlatformLoggerBuilder = AndroidPlatformLogger.Builder(mockNativeLoggerBuilder)
    }

    @Test
    fun `M return AndroidPlatformLogger W build()`() {
        // Given
        whenever(mockNativeLoggerBuilder.build()) doReturn mock()

        // When
        val platformLogger = testedPlatformLoggerBuilder.build()

        // Then
        assertThat(platformLogger).isInstanceOf(AndroidPlatformLogger::class.java)
    }

    @Test
    fun `M call native logger builder+setService W setService()`(
        @StringForgery fakeService: String
    ) {
        // When
        testedPlatformLoggerBuilder.setService(fakeService)

        // Then
        verify(mockNativeLoggerBuilder).setService(fakeService)
    }

    @Test
    fun `M call native logger builder+setRemoteLogThreshold W setRemoteLogThreshold()`(
        @Forgery fakeRemoteLogThreshold: LogLevel
    ) {
        // When
        testedPlatformLoggerBuilder.setRemoteLogThreshold(fakeRemoteLogThreshold)

        // Then
        val expectedValue = when (fakeRemoteLogThreshold) {
            LogLevel.DEBUG -> Log.DEBUG
            LogLevel.INFO -> Log.INFO
            LogLevel.WARN -> Log.WARN
            LogLevel.ERROR -> Log.ERROR
            LogLevel.CRITICAL -> Log.ASSERT
        }
        verify(mockNativeLoggerBuilder).setRemoteLogThreshold(expectedValue)
    }

    @Test
    fun `M call native logger builder+setLogcatLogsEnabled W setPrintLogsToConsole()`(
        @BoolForgery fakePrintsLogsToConsole: Boolean
    ) {
        // When
        testedPlatformLoggerBuilder.setPrintLogsToConsole(fakePrintsLogsToConsole)

        // Then
        verify(mockNativeLoggerBuilder).setLogcatLogsEnabled(fakePrintsLogsToConsole)
    }

    @Test
    fun `M call native logger builder+setNetworkInfoEnabled W setNetworkInfoEnabled()`(
        @BoolForgery fakeNetworkInfoEnabled: Boolean
    ) {
        // When
        testedPlatformLoggerBuilder.setNetworkInfoEnabled(fakeNetworkInfoEnabled)

        // Then
        verify(mockNativeLoggerBuilder).setNetworkInfoEnabled(fakeNetworkInfoEnabled)
    }

    @Test
    fun `M call native logger builder+setName W setName()`(
        @StringForgery fakeName: String
    ) {
        // When
        testedPlatformLoggerBuilder.setName(fakeName)

        // Then
        verify(mockNativeLoggerBuilder).setName(fakeName)
    }

    @Test
    fun `M call native logger builder+setBundleWithTraceEnabled W setBundleWithTraceEnabled()`(
        @BoolForgery fakeBundleWithTraceEnabled: Boolean
    ) {
        // When
        testedPlatformLoggerBuilder.setBundleWithTraceEnabled(fakeBundleWithTraceEnabled)

        // Then
        verify(mockNativeLoggerBuilder).setBundleWithTraceEnabled(fakeBundleWithTraceEnabled)
    }

    @Test
    fun `M call native logger builder+setBundleWithRumEnabled W setBundleWithRumEnabled()`(
        @BoolForgery fakeBundleWithRumEnabled: Boolean
    ) {
        // When
        testedPlatformLoggerBuilder.setBundleWithRumEnabled(fakeBundleWithRumEnabled)

        // Then
        verify(mockNativeLoggerBuilder).setBundleWithRumEnabled(fakeBundleWithRumEnabled)
    }

    @Test
    fun `M call native logger builder+setRemoteSampleRate W setRemoteSampleRate()`(
        @FloatForgery(min = 0f, max = 100f) fakeRemoteSampleRate: Float
    ) {
        // When
        testedPlatformLoggerBuilder.setRemoteSampleRate(fakeRemoteSampleRate)

        // Then
        verify(mockNativeLoggerBuilder).setRemoteSampleRate(fakeRemoteSampleRate)
    }
}
