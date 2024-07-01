/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import cocoapods.DatadogObjc.DDRUMConfiguration
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyAverage
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyFrequent
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyNever
import cocoapods.DatadogObjc.DDRUMVitalsFrequencyRare
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.tracking.RumAction
import com.datadog.kmp.rum.tracking.RumView
import com.datadog.kmp.rum.tracking.UIKitRUMActionsPredicate
import com.datadog.kmp.rum.tracking.UIKitRUMViewsPredicate
import com.datadog.tools.random.exhaustiveAttributes
import com.datadog.tools.random.randomBoolean
import com.datadog.tools.random.randomEnumValue
import com.datadog.tools.random.randomFloat
import com.datadog.tools.random.randomLong
import dev.mokkery.mock
import dev.mokkery.verify
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// TODO RUM-5099 Update Mokkery to the version compatible with Kotlin 2.0.20+
@Ignore
class IOSRumConfigurationBuilderTest {

    private val fakeNativeRumConfiguration = DDRUMConfiguration("fake-app-id")

    private val testedBuilder = IOSRumConfigurationBuilder(fakeNativeRumConfiguration)

    @Test
    fun `M set session sample rate W setSessionSampleRate`() {
        // Given
        val fakeSessionSampleRate = randomFloat(from = 0f, until = 100f)

        // When
        testedBuilder.setSessionSampleRate(fakeSessionSampleRate)

        // Then
        assertEquals(fakeSessionSampleRate, fakeNativeRumConfiguration.sessionSampleRate())
    }

    @Test
    fun `M set telemetry sample rate W setTelemetrySampleRate`() {
        // Given
        val fakeTelemetrySampleRate = randomFloat(from = 0f, until = 100f)

        // When
        testedBuilder.setTelemetrySampleRate(fakeTelemetrySampleRate)

        // Then
        assertEquals(fakeTelemetrySampleRate, fakeNativeRumConfiguration.telemetrySampleRate())
    }

    @Test
    fun `M set long task threshold W trackLongTasks`() {
        // Given
        val fakeLongTaskThresholdMs = randomLong(from = 0L)

        // When
        testedBuilder.trackLongTasks(fakeLongTaskThresholdMs)

        // Then
        assertEquals(fakeLongTaskThresholdMs.toDouble(), fakeNativeRumConfiguration.longTaskThreshold() * 1000)
    }

    @Test
    fun `M set track background events W trackBackgroundEvents`() {
        // Given
        val fakeTrackBackgroundEvents = randomBoolean()

        // When
        testedBuilder.trackBackgroundEvents(fakeTrackBackgroundEvents)

        // Then
        assertEquals(fakeTrackBackgroundEvents, fakeNativeRumConfiguration.trackBackgroundEvents())
    }

    @Test
    fun `M set track frustrations W trackFrustrations`() {
        // Given
        val fakeTrackFrustrations = randomBoolean()

        // When
        testedBuilder.trackFrustrations(fakeTrackFrustrations)

        // Then
        assertEquals(fakeTrackFrustrations, fakeNativeRumConfiguration.trackFrustrations())
    }

    @Test
    fun `M set vitals update frequency W setVitalsUpdateFrequency`() {
        // Given
        val fakeFrequency = randomEnumValue<VitalsUpdateFrequency>()

        // When
        testedBuilder.setVitalsUpdateFrequency(fakeFrequency)

        // Then
        val actualFrequency = fakeNativeRumConfiguration.vitalsUpdateFrequency().run {
            when (this) {
                DDRUMVitalsFrequencyNever -> VitalsUpdateFrequency.NEVER
                DDRUMVitalsFrequencyRare -> VitalsUpdateFrequency.RARE
                DDRUMVitalsFrequencyAverage -> VitalsUpdateFrequency.AVERAGE
                DDRUMVitalsFrequencyFrequent -> VitalsUpdateFrequency.FREQUENT
                else -> throw IllegalArgumentException("Unknown native vitals update frequency value = $this")
            }
        }
        assertEquals(fakeFrequency, actualFrequency)
    }

    @Test
    fun `M set session listener W setSessionListener`() {
        // Given
        val mockListener = mock<RumSessionListener>()
        val fakeSessionId = "fake-session-id"
        val fakeIsDiscarded = randomBoolean()

        // When
        testedBuilder.setSessionListener(mockListener)
        fakeNativeRumConfiguration.onSessionStart()?.invoke(fakeSessionId, fakeIsDiscarded)

        // Then
        verify {
            mockListener.onSessionStarted(fakeSessionId, fakeIsDiscarded)
        }
    }

    @Test
    fun `M set UIKit views predicate W setUiKitViewsPredicate`() {
        // Given
        val fakeViewName = "fake-view-name"
        val fakeViewAttributes = exhaustiveAttributes()
        val fakeRumView = RumView(fakeViewName, fakeViewAttributes)
        val stubPredicate = UIKitRUMViewsPredicate { fakeRumView }

        // When
        testedBuilder.setUiKitViewsPredicate(stubPredicate)

        // Then
        val nativeRumView = checkNotNull(fakeNativeRumConfiguration.uiKitViewsPredicate())
            .rumViewFor(UIViewController())
        checkNotNull(nativeRumView)
        assertEquals(fakeViewName, nativeRumView.name())
        assertEquals(
            expected = fakeViewAttributes,
            actual = nativeRumView.attributes().mapKeys {
                it.key as String
            }
        )
    }

    @Test
    fun `M set UIKit actions predicate W setUiKitActionsPredicate`() {
        // Given
        val fakeActionName = "fake-action-name"
        val fakeActionAttributes = exhaustiveAttributes()
        val fakeRumAction = RumAction(fakeActionName, fakeActionAttributes)
        val stubPredicate = UIKitRUMActionsPredicate { fakeRumAction }

        // When
        testedBuilder.setUiKitActionsPredicate(stubPredicate)

        // Then
        val nativeRumAction = checkNotNull(fakeNativeRumConfiguration.uiKitActionsPredicate())
            .rumActionWithTargetView(UIView())
        checkNotNull(nativeRumAction)
        assertEquals(fakeActionName, nativeRumAction.name())
        assertEquals(
            expected = fakeActionAttributes,
            actual = nativeRumAction.attributes().mapKeys {
                it.key as String
            }
        )
    }

    @Test
    fun `M return native configuration W build`() {
        // When
        val nativeConfiguration = testedBuilder.build()

        // Then
        assertSame(fakeNativeRumConfiguration, nativeConfiguration)
    }
}
