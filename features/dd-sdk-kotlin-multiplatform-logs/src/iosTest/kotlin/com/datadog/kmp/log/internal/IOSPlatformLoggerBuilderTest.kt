/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.internal

import cocoapods.DatadogObjc.DDLogLevelCritical
import cocoapods.DatadogObjc.DDLogLevelDebug
import cocoapods.DatadogObjc.DDLogLevelError
import cocoapods.DatadogObjc.DDLogLevelInfo
import cocoapods.DatadogObjc.DDLogLevelWarn
import cocoapods.DatadogObjc.DDLoggerConfiguration
import com.datadog.kmp.log.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class IOSPlatformLoggerBuilderTest {

    // cannot mock, because it has final members coming from parent platform.darwin.NSObject,
    // also mixing Kotlin (comes from mocking) and Objective-C supertypes is not supported by KMP
    private val nativeLoggerConfiguration = DDLoggerConfiguration.default()

    private val testedPlatformLoggerBuilder = IOSPlatformLogger.Builder(nativeLoggerConfiguration)

    @Test
    fun `M return IOSPlatformLogger W build`() {
        // When
        val platformLogger = testedPlatformLoggerBuilder.build()

        // Then
        assertIs<IOSPlatformLogger>(platformLogger)
    }

    @Test
    fun `M call native logger builder+setService W setService`() {
        // Given
        val fakeService = "fakeService"

        // When
        testedPlatformLoggerBuilder.setService(fakeService)

        // Then
        assertEquals(fakeService, nativeLoggerConfiguration.service())
    }

    @Test
    fun `M call native logger builder+setRemoteLogThreshold_debug_ W setRemoteLogThreshold_debug_`() {
        // Given
        val fakeRemoteLogThreshold = LogLevel.DEBUG

        // When
        testedPlatformLoggerBuilder.setRemoteLogThreshold(fakeRemoteLogThreshold)

        // Then
        assertEquals(DDLogLevelDebug, nativeLoggerConfiguration.remoteLogThreshold())
    }

    @Test
    fun `M call native logger builder+setRemoteLogThreshold_info_ W setRemoteLogThreshold_info_`() {
        // Given
        val fakeRemoteLogThreshold = LogLevel.INFO

        // When
        testedPlatformLoggerBuilder.setRemoteLogThreshold(fakeRemoteLogThreshold)

        // Then
        assertEquals(DDLogLevelInfo, nativeLoggerConfiguration.remoteLogThreshold())
    }

    @Test
    fun `M call native logger builder+setRemoteLogThreshold_warn_ W setRemoteLogThreshold_warn_`() {
        // Given
        val fakeRemoteLogThreshold = LogLevel.WARN

        // When
        testedPlatformLoggerBuilder.setRemoteLogThreshold(fakeRemoteLogThreshold)

        // Then
        assertEquals(DDLogLevelWarn, nativeLoggerConfiguration.remoteLogThreshold())
    }

    @Test
    fun `M call native logger builder+setRemoteLogThreshold_error_ W setRemoteLogThreshold_error_`() {
        // Given
        val fakeRemoteLogThreshold = LogLevel.ERROR

        // When
        testedPlatformLoggerBuilder.setRemoteLogThreshold(fakeRemoteLogThreshold)

        // Then
        assertEquals(DDLogLevelError, nativeLoggerConfiguration.remoteLogThreshold())
    }

    @Test
    fun `M call native logger builder+setRemoteLogThreshold_critical_ W setRemoteLogThreshold_critical_`() {
        // Given
        val fakeRemoteLogThreshold = LogLevel.CRITICAL

        // When
        testedPlatformLoggerBuilder.setRemoteLogThreshold(fakeRemoteLogThreshold)

        // Then
        assertEquals(DDLogLevelCritical, nativeLoggerConfiguration.remoteLogThreshold())
    }

    @Test
    fun `M call native logger builder+setPrintLogsToConsole W setPrintLogsToConsole`() {
        // Given
        val fakePrintsLogsToConsole = true

        // When
        testedPlatformLoggerBuilder.setPrintLogsToConsole(fakePrintsLogsToConsole)

        // Then
        assertEquals(fakePrintsLogsToConsole, nativeLoggerConfiguration.printLogsToConsole())
    }

    @Test
    fun `M call native logger builder+setNetworkInfoEnabled W setNetworkInfoEnabled`() {
        // Given
        val fakeNetworkInfoEnabled = true

        // When
        testedPlatformLoggerBuilder.setNetworkInfoEnabled(fakeNetworkInfoEnabled)

        // Then
        assertEquals(fakeNetworkInfoEnabled, nativeLoggerConfiguration.networkInfoEnabled())
    }

    @Test
    fun `M call native logger builder+setName W setName`() {
        // Given
        val fakeName = "fakeName"

        // When
        testedPlatformLoggerBuilder.setName(fakeName)

        // Then
        assertEquals(fakeName, nativeLoggerConfiguration.name())
    }

    @Test
    fun `M call native logger builder+setBundleWithTraceEnabled W setBundleWithTraceEnabled`() {
        // Given
        val fakeBundleWithTraceEnabled = true

        // When
        testedPlatformLoggerBuilder.setBundleWithTraceEnabled(fakeBundleWithTraceEnabled)

        // Then
        assertEquals(fakeBundleWithTraceEnabled, nativeLoggerConfiguration.bundleWithTraceEnabled())
    }

    @Test
    fun `M call native logger builder+setBundleWithRumEnabled W setBundleWithRumEnabled`() {
        // Given
        val fakeBundleWithRumEnabled = true

        // When
        testedPlatformLoggerBuilder.setBundleWithRumEnabled(fakeBundleWithRumEnabled)

        // Then
        assertEquals(fakeBundleWithRumEnabled, nativeLoggerConfiguration.bundleWithRumEnabled())
    }

    @Test
    fun `M call native logger builder+setRemoteSampleRate W setRemoteSampleRate`() {
        // Given
        val fakeRemoteSampleRate = 42f

        // When
        testedPlatformLoggerBuilder.setRemoteSampleRate(fakeRemoteSampleRate)

        // Then
        assertEquals(fakeRemoteSampleRate, nativeLoggerConfiguration.remoteSampleRate())
    }
}
