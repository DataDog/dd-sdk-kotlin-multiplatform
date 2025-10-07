/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.configuration.internal

import cocoapods.DatadogRUM.DDRUMConfiguration
import cocoapods.DatadogRUM.DDRUMVitalsFrequencyAverage
import cocoapods.DatadogRUM.DDRUMVitalsFrequencyFrequent
import cocoapods.DatadogRUM.DDRUMVitalsFrequencyNever
import cocoapods.DatadogRUM.DDRUMVitalsFrequencyRare
import com.datadog.kmp.Datadog
import com.datadog.kmp.core.configuration.Configuration
import com.datadog.kmp.privacy.TrackingConsent
import com.datadog.kmp.rum.Rum
import com.datadog.kmp.rum.RumActionType
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.configuration.RumConfiguration
import com.datadog.kmp.rum.configuration.RumSessionListener
import com.datadog.kmp.rum.configuration.VitalsUpdateFrequency
import com.datadog.kmp.rum.tracking.RumAction
import com.datadog.kmp.rum.tracking.RumView
import com.datadog.kmp.rum.tracking.SwiftUIRUMActionsPredicate
import com.datadog.kmp.rum.tracking.SwiftUIRUMViewsPredicate
import com.datadog.kmp.rum.tracking.UIKitRUMViewsPredicate
import com.datadog.tools.concurrent.CountDownLatch
import com.datadog.tools.random.exhaustiveAttributes
import com.datadog.tools.random.nullable
import com.datadog.tools.random.randomBoolean
import com.datadog.tools.random.randomEnumValue
import com.datadog.tools.random.randomFloat
import com.datadog.tools.random.randomInt
import com.datadog.tools.random.randomLong
import dev.mokkery.mock
import dev.mokkery.verify
import platform.UIKit.UIViewController
import platform.posix.usleep
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal abstract class AppleRumConfigurationBuilderTest<T : AppleRumConfigurationBuilder> {

    protected val fakeNativeRumConfiguration = DDRUMConfiguration("fake-app-id")

    protected val testedBuilder by lazy { createTestedBuilder() }

    protected abstract fun createTestedBuilder(): T

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
    fun `M set SwiftUI views predicate W setSwiftUIViewsPredicate`() {
        // Given
        val fakeViewName = "fake-view-name"
        val fakeViewAttributes = exhaustiveAttributes()
        val fakeRumView = RumView(fakeViewName, fakeViewAttributes)
        val stubPredicate = SwiftUIRUMViewsPredicate { fakeRumView }

        // When
        testedBuilder.setSwiftUIViewsPredicate(stubPredicate)

        // Then
        val nativeRumView = checkNotNull(fakeNativeRumConfiguration.swiftUIViewsPredicate())
            .rumViewFor("fake-swiftui-component")
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
    fun `M set SwiftUI views predicate W setSwiftUIActionsPredicate`() {
        // Given
        val fakeActionName = "fake-view-name"
        val fakeActionAttributes = exhaustiveAttributes()
        val fakeRumAction = RumAction(fakeActionName, fakeActionAttributes)
        val stubPredicate = SwiftUIRUMActionsPredicate { fakeRumAction }

        // When
        testedBuilder.setSwiftUIActionsPredicate(stubPredicate)

        // Then
        val nativeRumAction = checkNotNull(fakeNativeRumConfiguration.swiftUIActionsPredicate())
            .rumActionWith("fake-swiftui-component")
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
    fun `M set app hang threshold W setAppHangThreshold`() {
        // Given
        val fakeThresholdMs = randomLong(from = 1L)

        // When
        testedBuilder.setAppHangThreshold(fakeThresholdMs)

        // Then
        assertEquals(fakeThresholdMs.toDouble(), fakeNativeRumConfiguration.appHangThreshold() * 1000)
    }

    @Test
    fun `M disable app hang threshold W setAppHangThreshold + null value`() {
        // When
        testedBuilder.setAppHangThreshold(null)

        // Then
        assertEquals(0.0, fakeNativeRumConfiguration.appHangThreshold())
    }

    // region event mappers

    // There is no way to create an instance of the RUM events in iOS SDK ObjC API, so they only way we can get them
    // is by running the real SDK instance and interacting with RUM monitor.
    // The best we can do for the mappers tests is to assert that they were called and make sure nothing crashes
    // during the conversion to the native model.

    @Test
    fun `M call platform RUM configuration builder+setViewEventMapper W setViewEventMapper`() {
        // Given
        initializeSdkWithPendingConsent()

        val latch = CountDownLatch(1)

        initializeRum {
            setViewEventMapper {
                // beware: we have ApplicationLaunch view as well, so it can be called multiple times
                latch.countDown()
                it
            }
        }

        // When
        runRUMActions(imitateLongTask = true)
        latch.await(EVENTS_WAIT_TIMEOUT_MS)
        Datadog.stopInstance()

        // Then
        assertEquals(
            expected = true,
            actual = latch.isExhausted(),
            "Expected user-provided view event mapper to be called successfully, but it wasn't"
        )
    }

    @Test
    fun `M call platform RUM configuration builder+setResourceEventMapper W setResourceEventMapper`() {
        // Given
        initializeSdkWithPendingConsent()

        val latch = CountDownLatch(1)

        initializeRum {
            setResourceEventMapper {
                latch.countDown()
                it
            }
        }

        // When
        runRUMActions()
        latch.await(EVENTS_WAIT_TIMEOUT_MS)
        Datadog.stopInstance()

        // Then
        assertEquals(
            expected = true,
            actual = latch.isExhausted(),
            "Expected user-provided resource event mapper to be called successfully, but it wasn't"
        )
    }

    @Test
    fun `M call platform RUM configuration builder+setActionEventMapper W setActionEventMapper`() {
        // Given
        initializeSdkWithPendingConsent()

        val latch = CountDownLatch(1)

        initializeRum {
            setActionEventMapper {
                latch.countDown()
                it
            }
        }

        // When
        runRUMActions()
        latch.await(EVENTS_WAIT_TIMEOUT_MS)
        Datadog.stopInstance()

        // Then
        assertEquals(
            expected = true,
            actual = latch.isExhausted(),
            "Expected user-provided action event mapper to be called successfully, but it wasn't"
        )
    }

    @Test
    fun `M call platform RUM configuration builder+setErrorEventMapper W setErrorEventMapper`() {
        // Given
        initializeSdkWithPendingConsent()

        val latch = CountDownLatch(1)

        initializeRum {
            setErrorEventMapper {
                latch.countDown()
                it
            }
        }

        // When
        runRUMActions()
        latch.await(EVENTS_WAIT_TIMEOUT_MS)
        Datadog.stopInstance()

        // Then
        assertEquals(
            expected = true,
            actual = latch.isExhausted(),
            "Expected user-provided error event mapper to be called successfully, but it wasn't"
        )
    }

    // Long task detection seems not working in the test executable
    @Ignore
    @Test
    fun `M call platform RUM configuration builder+setLongTaskEventMapper W setLongTaskEventMapper`() {
        // Given
        initializeSdkWithPendingConsent()

        val latch = CountDownLatch(1)

        initializeRum {
            setLongTaskEventMapper {
                latch.countDown()
                it
            }
        }

        // When
        runRUMActions(imitateLongTask = true)
        latch.await(EVENTS_WAIT_TIMEOUT_MS)
        Datadog.stopInstance()

        // Then
        assertEquals(
            expected = true,
            actual = latch.isExhausted(),
            "Expected user-provided long task event mapper to be called successfully, but it wasn't"
        )
    }

    // endregion

    @Test
    fun `M set watchdog terminations tracking W trackWatchdogTerminations`() {
        // Given
        val fakeTrackWatchdogTerminations = randomBoolean()

        // When
        testedBuilder.trackWatchdogTerminations(fakeTrackWatchdogTerminations)

        // Then
        assertEquals(fakeTrackWatchdogTerminations, fakeNativeRumConfiguration.trackWatchdogTerminations())
    }

    @Test
    fun `M set track anonymous user W trackAnonymousUser`() {
        // Given
        val fakeTrackAnonymousUser = randomBoolean()

        // When
        testedBuilder.trackAnonymousUser(fakeTrackAnonymousUser)

        // Then
        assertEquals(fakeTrackAnonymousUser, fakeNativeRumConfiguration.trackAnonymousUser())
    }

    @Test
    fun `M set custom endpoint W useCustomEndpoint`() {
        // Given
        val fakeCustomEndpoint = "https://example.com/api/rum"

        // When
        testedBuilder.useCustomEndpoint(fakeCustomEndpoint)

        // Then
        assertEquals(fakeCustomEndpoint, fakeNativeRumConfiguration.customEndpoint()?.absoluteString)
    }

    @Test
    fun `M set memory warnings tracking W trackMemoryWarnings`() {
        // Given
        val fakeTrackMemoryWarnings = randomBoolean()

        // When
        testedBuilder.trackMemoryWarnings(fakeTrackMemoryWarnings)

        // Then
        assertEquals(fakeTrackMemoryWarnings, fakeNativeRumConfiguration.trackMemoryWarnings())
    }

    @Test
    fun `M return native configuration W build`() {
        // When
        val nativeConfiguration = testedBuilder.build()

        // Then
        assertSame(fakeNativeRumConfiguration, nativeConfiguration)
    }

    // region private

    private fun runRUMActions(imitateLongTask: Boolean = false) {
        with(RumMonitor.get()) {
            val fakeViewName = "FakeView"
            startView(fakeViewName, fakeViewName)

            addError("FakeErrorMessage", randomEnumValue(), IllegalStateException("fakeExceptionMessage"))

            addAction(RumActionType.CUSTOM, "FakeAction", mapOf("action-custom-attribute" to "action-custom-value"))
            val fakeResourceName = "FakeResource"
            startResource(fakeResourceName, randomEnumValue(), "https://www.datadoghq.com")
            stopResource(
                fakeResourceName,
                statusCode = randomInt(from = 100, until = 600),
                size = nullable(randomLong(from = 0L)),
                randomEnumValue(),
                mapOf("resource-custom-attribute" to "resource-custom-value")
            )

            // sleep for 500ms
            if (imitateLongTask) usleep(500_000U)

            stopView(fakeViewName)
        }
    }

    private fun initializeSdkWithPendingConsent() {
        val fakeConfiguration = Configuration.Builder(
            clientToken = "fakeToken",
            env = "fakeEnv"
        ).build()
        Datadog.initialize(null, fakeConfiguration, TrackingConsent.PENDING)
    }

    private fun initializeRum(configurationAction: RumConfiguration.Builder.() -> Unit) {
        Rum.enable(
            RumConfiguration.Builder(applicationId = "fakeApplicationId")
                .apply(configurationAction)
                .build()
        )
    }

    // endregion

    private companion object {
        const val EVENTS_WAIT_TIMEOUT_MS = 2000L
    }
}
