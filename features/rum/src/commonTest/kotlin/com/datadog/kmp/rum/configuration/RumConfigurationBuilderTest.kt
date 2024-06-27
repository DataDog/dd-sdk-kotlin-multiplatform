/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.rum.configuration.internal.PlatformRumConfigurationBuilder
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertSame

// TODO RUM-5099 Update Mokkery to the version compatible with Kotlin 2.0.20+
@Ignore
class RumConfigurationBuilderTest {

    private val mockPlatformRumConfigurationBuilder = mock<PlatformRumConfigurationBuilder<*>>()

    private val testedRumConfigurationBuilder = RumConfiguration.Builder(mockPlatformRumConfigurationBuilder)

    @Test
    fun `M return RumConfiguration W build`() {
        // Given
        val fakeNativeConfiguration = Any()
        every { mockPlatformRumConfigurationBuilder.build() } returns fakeNativeConfiguration

        // When
        val rumConfiguration = testedRumConfigurationBuilder.build()

        // Then
        assertSame(fakeNativeConfiguration, rumConfiguration.nativeConfiguration)
    }

    @Test
    fun `M call platform RUM configuration builder+setSessionSampleRate W setSessionSampleRate`() {
        // Given
        val fakeSessionSampleRate = 42f

        // When
        testedRumConfigurationBuilder.setSessionSampleRate(fakeSessionSampleRate)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setSessionSampleRate(fakeSessionSampleRate)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setTelemetrySampleRate W setTelemetrySampleRate`() {
        // Given
        val fakeTelemetrySampleRate = 42f

        // When
        testedRumConfigurationBuilder.setTelemetrySampleRate(fakeTelemetrySampleRate)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setTelemetrySampleRate(fakeTelemetrySampleRate)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+trackLongTasks W trackLongTasks`() {
        // Given
        val fakeLongTasksThreshold = 250L

        // When
        testedRumConfigurationBuilder.trackLongTasks(fakeLongTasksThreshold)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.trackLongTasks(fakeLongTasksThreshold)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+trackBackgroundEvents W trackBackgroundEvents`() {
        // Given
        val fakeTrackBackgroundEvents = true

        // When
        testedRumConfigurationBuilder.trackBackgroundEvents(fakeTrackBackgroundEvents)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.trackBackgroundEvents(fakeTrackBackgroundEvents)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+trackFrustrations W trackFrustrations`() {
        // Given
        val fakeTrackFrustrations = true

        // When
        testedRumConfigurationBuilder.trackFrustrations(fakeTrackFrustrations)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.trackFrustrations(fakeTrackFrustrations)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setVitalsUpdateFrequency W setVitalsUpdateFrequency`() {
        // Given
        val fakeVitalsUpdateFrequency = VitalsUpdateFrequency.RARE

        // When
        testedRumConfigurationBuilder.setVitalsUpdateFrequency(fakeVitalsUpdateFrequency)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setVitalsUpdateFrequency(fakeVitalsUpdateFrequency)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setSessionListener W setSessionListener`() {
        // Given
        val fakeSessionListener = RumSessionListener { _, _ -> }

        // When
        testedRumConfigurationBuilder.setSessionListener(fakeSessionListener)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setSessionListener(fakeSessionListener)
        }
    }
}
