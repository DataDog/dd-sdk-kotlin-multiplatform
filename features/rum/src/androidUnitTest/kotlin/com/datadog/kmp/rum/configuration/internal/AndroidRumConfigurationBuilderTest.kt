/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import android.content.Context
import android.view.View
import com.datadog.kmp.event.EventMapper
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.event.ViewEventMapper
import com.datadog.kmp.rum.model.ActionEvent
import com.datadog.kmp.rum.model.ErrorEvent
import com.datadog.kmp.rum.model.LongTaskEvent
import com.datadog.kmp.rum.model.ResourceEvent
import com.datadog.kmp.rum.tracking.InteractionPredicate
import com.datadog.kmp.rum.tracking.ViewAttributesProvider
import com.datadog.kmp.rum.tracking.ViewTrackingStrategy
import com.datadog.kmp.rum.utils.forge.Configurator
import com.datadog.tools.unit.forge.exhaustiveAttributes
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.annotation.BoolForgery
import fr.xgouchet.elmyr.annotation.FloatForgery
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.annotation.LongForgery
import fr.xgouchet.elmyr.annotation.StringForgery
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
import com.datadog.android.event.EventMapper as NativeEventMapper
import com.datadog.android.rum.RumConfiguration as NativeAndroidConfiguration
import com.datadog.android.rum.RumConfiguration as NativeRumConfiguration
import com.datadog.android.rum.RumSessionListener as NativeRumSessionListener
import com.datadog.android.rum.configuration.VitalsUpdateFrequency as NativeVitalsUpdateFrequency
import com.datadog.android.rum.event.ViewEventMapper as NativeViewEventMapper
import com.datadog.android.rum.model.ActionEvent as NativeActionEvent
import com.datadog.android.rum.model.ErrorEvent as NativeErrorEvent
import com.datadog.android.rum.model.LongTaskEvent as NativeLongTaskEvent
import com.datadog.android.rum.model.ResourceEvent as NativeResourceEvent
import com.datadog.android.rum.model.ViewEvent as NativeViewEvent
import com.datadog.android.rum.tracking.InteractionPredicate as NativeInteractionPredicate
import com.datadog.android.rum.tracking.ViewAttributesProvider as NativeViewAttributesProvider
import com.datadog.android.rum.tracking.ViewTrackingStrategy as NativeViewTrackingStrategy

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(Configurator::class)
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
    fun `M call platform RUM configuration builder+setViewEventMapper W setViewEventMapper`(
        @Forgery fakeNativeViewEvent: NativeViewEvent,
        @StringForgery(
            regex = "(https|http)://([a-z][a-z0-9-]{3,9}\\.){1,4}[a-z][a-z0-9]{2,3}"
        ) fakeViewReferrer: String,
        @StringForgery fakeViewName: String,
        @StringForgery fakeViewUrl: String
    ) {
        // Given
        val fakeViewEventMapper = ViewEventMapper {
            it.view.referrer = fakeViewReferrer
            it.view.name = fakeViewName
            it.view.url = fakeViewUrl
            it
        }

        // When
        testedBuilder.setViewEventMapper(fakeViewEventMapper)

        // Then
        argumentCaptor<NativeViewEventMapper> {
            verify(mockNativeRumConfigurationBuilder).setViewEventMapper(capture())
            firstValue.map(fakeNativeViewEvent)

            assertThat(fakeNativeViewEvent.view.referrer).isEqualTo(fakeViewReferrer)
            assertThat(fakeNativeViewEvent.view.name).isEqualTo(fakeViewName)
            assertThat(fakeNativeViewEvent.view.url).isEqualTo(fakeViewUrl)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setActionEventMapper W setActionEventMapper`(
        @Forgery fakeNativeActionEvent: NativeActionEvent,
        @StringForgery(
            regex = "(https|http)://([a-z][a-z0-9-]{3,9}\\.){1,4}[a-z][a-z0-9]{2,3}"
        ) fakeViewReferrer: String,
        @StringForgery fakeViewName: String,
        @StringForgery fakeViewUrl: String,
        @StringForgery fakeActionTargetName: String
    ) {
        // Given
        val fakeActionEventMapper = EventMapper<ActionEvent> {
            it.view.referrer = fakeViewReferrer
            it.view.name = fakeViewName
            it.view.url = fakeViewUrl

            it.action.target?.let {
                it.name = fakeActionTargetName
            }

            it
        }

        // When
        testedBuilder.setActionEventMapper(fakeActionEventMapper)

        // Then
        argumentCaptor<NativeEventMapper<NativeActionEvent>> {
            verify(mockNativeRumConfigurationBuilder).setActionEventMapper(capture())
            firstValue.map(fakeNativeActionEvent)

            assertThat(fakeNativeActionEvent.view.referrer).isEqualTo(fakeViewReferrer)
            assertThat(fakeNativeActionEvent.view.name).isEqualTo(fakeViewName)
            assertThat(fakeNativeActionEvent.view.url).isEqualTo(fakeViewUrl)

            fakeNativeActionEvent.action.target?.let {
                assertThat(it.name).isEqualTo(fakeActionTargetName)
            }
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setResourceEventMapper W setResourceEventMapper`(
        @Forgery fakeNativeResourceEvent: NativeResourceEvent,
        @StringForgery(
            regex = "(https|http)://([a-z][a-z0-9-]{3,9}\\.){1,4}[a-z][a-z0-9]{2,3}"
        ) fakeViewReferrer: String,
        @StringForgery fakeViewName: String,
        @StringForgery fakeViewUrl: String,
        @StringForgery(
            regex = "(https|http)://([a-z][a-z0-9-]{3,9}\\.){1,4}[a-z][a-z0-9]{2,3}"
        ) fakeResourceUrl: String
    ) {
        // Given
        val fakeResourceEventMapper = EventMapper<ResourceEvent> {
            it.view.referrer = fakeViewReferrer
            it.view.name = fakeViewName
            it.view.url = fakeViewUrl

            it.resource.url = fakeResourceUrl

            it
        }

        // When
        testedBuilder.setResourceEventMapper(fakeResourceEventMapper)

        // Then
        argumentCaptor<NativeEventMapper<NativeResourceEvent>> {
            verify(mockNativeRumConfigurationBuilder).setResourceEventMapper(capture())
            firstValue.map(fakeNativeResourceEvent)

            assertThat(fakeNativeResourceEvent.view.referrer).isEqualTo(fakeViewReferrer)
            assertThat(fakeNativeResourceEvent.view.name).isEqualTo(fakeViewName)
            assertThat(fakeNativeResourceEvent.view.url).isEqualTo(fakeViewUrl)

            assertThat(fakeNativeResourceEvent.resource.url).isEqualTo(fakeResourceUrl)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setErrorEventMapper W setErrorEventMapper`(
        @Forgery fakeNativeErrorEvent: NativeErrorEvent,
        @StringForgery(
            regex = "(https|http)://([a-z][a-z0-9-]{3,9}\\.){1,4}[a-z][a-z0-9]{2,3}"
        ) fakeViewReferrer: String,
        @StringForgery fakeViewName: String,
        @StringForgery fakeViewUrl: String,
        @StringForgery fakeErrorFingerprint: String
    ) {
        // Given
        val fakeErrorEventMapper = EventMapper<ErrorEvent> {
            it.view.referrer = fakeViewReferrer
            it.view.name = fakeViewName
            it.view.url = fakeViewUrl

            it.error.fingerprint = fakeErrorFingerprint

            it
        }

        // When
        testedBuilder.setErrorEventMapper(fakeErrorEventMapper)

        // Then
        argumentCaptor<NativeEventMapper<NativeErrorEvent>> {
            verify(mockNativeRumConfigurationBuilder).setErrorEventMapper(capture())
            firstValue.map(fakeNativeErrorEvent)

            assertThat(fakeNativeErrorEvent.view.referrer).isEqualTo(fakeViewReferrer)
            assertThat(fakeNativeErrorEvent.view.name).isEqualTo(fakeViewName)
            assertThat(fakeNativeErrorEvent.view.url).isEqualTo(fakeViewUrl)

            assertThat(fakeNativeErrorEvent.error.fingerprint).isEqualTo(fakeErrorFingerprint)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+setLongTaskEventMapper W setLongTaskEventMapper`(
        @Forgery fakeNativeLongTaskEvent: NativeLongTaskEvent,
        @StringForgery(
            regex = "(https|http)://([a-z][a-z0-9-]{3,9}\\.){1,4}[a-z][a-z0-9]{2,3}"
        ) fakeViewReferrer: String,
        @StringForgery fakeViewName: String,
        @StringForgery fakeViewUrl: String
    ) {
        // Given
        val fakeLongTaskEventMapper = EventMapper<LongTaskEvent> {
            it.view.referrer = fakeViewReferrer
            it.view.name = fakeViewName
            it.view.url = fakeViewUrl
            it
        }

        // When
        testedBuilder.setLongTaskEventMapper(fakeLongTaskEventMapper)

        // Then
        argumentCaptor<NativeEventMapper<NativeLongTaskEvent>> {
            verify(mockNativeRumConfigurationBuilder).setLongTaskEventMapper(capture())
            firstValue.map(fakeNativeLongTaskEvent)

            assertThat(fakeNativeLongTaskEvent.view.referrer).isEqualTo(fakeViewReferrer)
            assertThat(fakeNativeLongTaskEvent.view.name).isEqualTo(fakeViewName)
            assertThat(fakeNativeLongTaskEvent.view.url).isEqualTo(fakeViewUrl)
        }
    }

    @Test
    fun `M call platform RUM configuration builder+trackAnonymousUser W trackAnonymousUser`(
        @BoolForgery fakeTrackAnonymousUser: Boolean
    ) {
        // When
        testedBuilder.trackAnonymousUser(fakeTrackAnonymousUser)

        // Then
        verify(mockNativeRumConfigurationBuilder).trackAnonymousUser(fakeTrackAnonymousUser)
    }

    @Test
    fun `M call platform RUM configuration builder+useCustomEndpoint W useCustomEndpoint`(
        @StringForgery(regex = "https://[a-z]+\\.com(/[a-z]+)+") fakeCustomEndpoint: String
    ) {
        // When
        testedBuilder.useCustomEndpoint(fakeCustomEndpoint)

        // Then
        verify(mockNativeRumConfigurationBuilder).useCustomEndpoint(fakeCustomEndpoint)
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
