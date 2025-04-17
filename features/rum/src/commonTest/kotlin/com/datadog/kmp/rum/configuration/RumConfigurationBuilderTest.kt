/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration

import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.rum.configuration.internal.PlatformRumConfigurationBuilder
import com.datadog.kmp.rum.event.ViewEventMapper
import com.datadog.kmp.rum.model.ActionEvent
import com.datadog.kmp.rum.model.ErrorEvent
import com.datadog.kmp.rum.model.LongTaskEvent
import com.datadog.kmp.rum.model.ResourceEvent
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertSame

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

    @Test
    fun `M call platform RUM configuration builder+setViewEventMapper W setViewEventMapper`() {
        // Given
        val fakeViewEventMapper = ViewEventMapper { it }

        // When
        testedRumConfigurationBuilder.setViewEventMapper(fakeViewEventMapper)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setViewEventMapper(fakeViewEventMapper)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setResourceEventMapper W setResourceEventMapper`() {
        // Given
        val fakeResourceEventMapper = EventMapper<ResourceEvent> { it }

        // When
        testedRumConfigurationBuilder.setResourceEventMapper(fakeResourceEventMapper)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setResourceEventMapper(fakeResourceEventMapper)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setActionEventMapper W setActionEventMapper`() {
        // Given
        val fakeActionEventMapper = EventMapper<ActionEvent> { it }

        // When
        testedRumConfigurationBuilder.setActionEventMapper(fakeActionEventMapper)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setActionEventMapper(fakeActionEventMapper)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setErrorEventMapper W setErrorEventMapper`() {
        // Given
        val fakeErrorEventMapper = EventMapper<ErrorEvent> { it }

        // When
        testedRumConfigurationBuilder.setErrorEventMapper(fakeErrorEventMapper)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setErrorEventMapper(fakeErrorEventMapper)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setLongTaskEventMapper W setLongTaskEventMapper`() {
        // Given
        val fakeLongTaskEventMapper = EventMapper<LongTaskEvent> { it }

        // When
        testedRumConfigurationBuilder.setLongTaskEventMapper(fakeLongTaskEventMapper)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.setLongTaskEventMapper(fakeLongTaskEventMapper)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+trackAnonymousUser W trackAnonymousUser`() {
        // Given
        val fakeTrackAnonymousUser = true

        // When
        testedRumConfigurationBuilder.trackAnonymousUser(fakeTrackAnonymousUser)

        // Then
        verify {
            mockPlatformRumConfigurationBuilder.trackAnonymousUser(fakeTrackAnonymousUser)
        }
    }
}
