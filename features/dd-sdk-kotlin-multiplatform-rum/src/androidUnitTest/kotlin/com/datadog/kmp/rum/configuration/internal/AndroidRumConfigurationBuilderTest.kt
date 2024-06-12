/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import android.content.Context
import android.view.View
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.tracking.InteractionPredicate
import com.datadog.kmp.rum.tracking.ViewAttributesProvider
import com.datadog.kmp.rum.tracking.ViewTrackingStrategy
import com.datadog.tools.unit.forge.BaseConfigurator
import com.datadog.tools.unit.forge.exhaustiveAttributes
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.annotation.BoolForgery
import fr.xgouchet.elmyr.annotation.FloatForgery
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.annotation.LongForgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import java.util.UUID
import com.datadog.android.rum.RumConfiguration as NativeAndroidConfiguration
import com.datadog.android.rum.RumConfiguration as NativeRumConfiguration
import com.datadog.android.rum.RumSessionListener as NativeRumSessionListener
import com.datadog.android.rum.configuration.VitalsUpdateFrequency as NativeVitalsUpdateFrequency
import com.datadog.android.rum.tracking.InteractionPredicate as NativeInteractionPredicate
import com.datadog.android.rum.tracking.ViewAttributesProvider as NativeViewAttributesProvider
import com.datadog.android.rum.tracking.ViewTrackingStrategy as NativeViewTrackingStrategy

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(BaseConfigurator::class)
internal class AndroidRumConfigurationBuilderTest {

    private lateinit var testedBuilder: AndroidRumConfigurationBuilder

    @Mock
    lateinit var mockNativeRumConfigurationBuilder: NativeRumConfiguration.Builder

    @BeforeEach
    fun `set up`() {
        testedBuilder = AndroidRumConfigurationBuilder(mockNativeRumConfigurationBuilder)
    }

    @Test
    fun `M call platform RUM configuration builder+setSessionSampleRate W setSessionSampleRate`(
        @FloatForgery(min = 0f, max = 100f) fakeSessionSampleRate: Float
    ) {
        // When
        testedBuilder.setSessionSampleRate(fakeSessionSampleRate)

        // Then
        verify(mockNativeRumConfigurationBuilder).setSessionSampleRate(fakeSessionSampleRate)
    }

    @Test
    fun `M call platform RUM configuration builder+setTelemetrySampleRate W setTelemetrySampleRate`(
        @FloatForgery(min = 0f, max = 100f) fakeTelemetrySampleRate: Float
    ) {
        // When
        testedBuilder.setTelemetrySampleRate(fakeTelemetrySampleRate)

        // Then
        verify(mockNativeRumConfigurationBuilder).setTelemetrySampleRate(fakeTelemetrySampleRate)
    }

    @Test
    fun `M call platform RUM configuration builder+trackLongTasks W trackLongTasks`(
        @LongForgery(min = 0L) fakeLongTaskThresholdMs: Long
    ) {
        // When
        testedBuilder.trackLongTasks(fakeLongTaskThresholdMs)

        // Then
        verify(mockNativeRumConfigurationBuilder).trackLongTasks(fakeLongTaskThresholdMs)
    }

    @Test
    fun `M call platform RUM configuration builder+trackBackgroundEvents W trackBackgroundEvents`(
        @BoolForgery fakeTrackBackgroundEvents: Boolean
    ) {
        // When
        testedBuilder.trackBackgroundEvents(fakeTrackBackgroundEvents)

        // Then
        verify(mockNativeRumConfigurationBuilder).trackBackgroundEvents(fakeTrackBackgroundEvents)
    }

    @Test
    fun `M call platform RUM configuration builder+trackFrustrations W trackFrustrations`(
        @BoolForgery fakeTrackFrustrations: Boolean
    ) {
        // When
        testedBuilder.trackFrustrations(fakeTrackFrustrations)

        // Then
        verify(mockNativeRumConfigurationBuilder).trackFrustrations(fakeTrackFrustrations)
    }

    @Test
    fun `M call platform RUM configuration builder+setVitalsUpdateFrequency W setVitalsUpdateFrequency`(
        @Forgery fakeVitalsUpdateFrequency: VitalsUpdateFrequency
    ) {
        // Given
        val expectedValue = when (fakeVitalsUpdateFrequency) {
            VitalsUpdateFrequency.NEVER -> NativeVitalsUpdateFrequency.NEVER
            VitalsUpdateFrequency.RARE -> NativeVitalsUpdateFrequency.RARE
            VitalsUpdateFrequency.AVERAGE -> NativeVitalsUpdateFrequency.AVERAGE
            VitalsUpdateFrequency.FREQUENT -> NativeVitalsUpdateFrequency.FREQUENT
        }
        // When
        testedBuilder.setVitalsUpdateFrequency(fakeVitalsUpdateFrequency)

        // Then
        verify(mockNativeRumConfigurationBuilder).setVitalsUpdateFrequency(expectedValue)
    }

    @Test
    fun `M call platform RUM configuration builder+setSessionListener W setSessionListener`(
        @Forgery fakeSessionId: UUID,
        @BoolForgery fakeIsDiscarded: Boolean
    ) {
        // Given
        val mockListener = mock<RumSessionListener>()

        // When
        testedBuilder.setSessionListener(mockListener)

        // Then
        argumentCaptor<NativeRumSessionListener> {
            verify(mockNativeRumConfigurationBuilder).setSessionListener(capture())
            firstValue.onSessionStarted(fakeSessionId.toString(), fakeIsDiscarded)

            verify(mockListener).onSessionStarted(fakeSessionId.toString(), fakeIsDiscarded)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+trackNonFatalAnrs W trackNonFatalAnrs`(
        @BoolForgery fakeTrackNonFatalAnrs: Boolean
    ) {
        // When
        testedBuilder.trackNonFatalAnrs(fakeTrackNonFatalAnrs)

        // Then
        verify(mockNativeRumConfigurationBuilder).trackNonFatalAnrs(fakeTrackNonFatalAnrs)
    }

    @Test
    fun `M call platform RUM configuration builder+useViewTrackingStrategy W useViewTrackingStrategy`() {
        // Given
        val mockViewTrackingStrategy = mock<ViewTrackingStrategy>()

        // When
        testedBuilder.useViewTrackingStrategy(mockViewTrackingStrategy)

        // Then
        argumentCaptor<NativeViewTrackingStrategy> {
            verify(mockNativeRumConfigurationBuilder).useViewTrackingStrategy(capture())
            val mockContext = mock<Context>()
            firstValue.register(sdkCore = mock(), mockContext)
            firstValue.unregister(mockContext)

            verify(mockViewTrackingStrategy).register(mockContext)
            verify(mockViewTrackingStrategy).unregister(mockContext)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+trackUserInteractions W trackUserInteractions`(
        forge: Forge
    ) {
        // Given
        val mockTouchTargetExtraAttributeProviders = forge.aList {
            mock<ViewAttributesProvider>()
        }.toTypedArray()
        val mockInteractionPredicate = mock<InteractionPredicate>()

        // When
        testedBuilder.trackUserInteractions(mockTouchTargetExtraAttributeProviders, mockInteractionPredicate)

        // Then
        val attributesProvidersCaptor = argumentCaptor<Array<NativeViewAttributesProvider>>()
        val interactionPredicateCaptor = argumentCaptor<NativeInteractionPredicate>()
        verify(mockNativeRumConfigurationBuilder)
            .trackUserInteractions(attributesProvidersCaptor.capture(), interactionPredicateCaptor.capture())

        attributesProvidersCaptor.firstValue.let {
            val mockView = mock<View>()
            val fakeAttributes = forge.exhaustiveAttributes().toMutableMap()
            assertThat(it).hasSameSizeAs(mockTouchTargetExtraAttributeProviders)
            it.forEach {
                it.extractAttributes(mockView, fakeAttributes)
            }
            mockTouchTargetExtraAttributeProviders.forEach {
                verify(it).extractAttributes(mockView, fakeAttributes)
            }
        }

        val fakeTarget = Any()
        interactionPredicateCaptor.firstValue.getTargetName(fakeTarget)
        verify(mockInteractionPredicate).getTargetName(fakeTarget)
    }

    @Test
    fun `M call platform RUM configuration builder+build W build`() {
        // Given
        val mockNativeConfiguration = mock<NativeAndroidConfiguration>()
        whenever(mockNativeRumConfigurationBuilder.build()) doReturn mockNativeConfiguration

        // When
        val rumConfiguration = testedBuilder.build()

        // Then
        assertThat(rumConfiguration).isSameAs(mockNativeConfiguration)
    }
}
