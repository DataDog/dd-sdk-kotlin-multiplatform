package com.datadog.kmp.rum.internal

import com.datadog.android.rum.ExperimentalRumApi
import com.datadog.kmp.rum.RumActionType
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import com.datadog.kmp.rum.featureoperations.FailureReason
import com.datadog.tools.unit.forge.BaseConfigurator
import com.datadog.tools.unit.forge.aThrowable
import com.datadog.tools.unit.forge.exhaustiveAttributes
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.annotation.BoolForgery
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import com.datadog.android.rum.RumActionType as NativeRumActionType
import com.datadog.android.rum.RumErrorSource as NativeRumErrorSource
import com.datadog.android.rum.RumMonitor as NativeRumMonitor
import com.datadog.android.rum.RumResourceKind as NativeRumResourceKind
import com.datadog.android.rum.RumResourceMethod as NativeRumResourceMethod
import com.datadog.android.rum.featureoperations.FailureReason as NativeFailureReason

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(BaseConfigurator::class)
class RumMonitorAdapterTest {

    @Mock
    lateinit var mockNativeRumMonitor: NativeRumMonitor

    private lateinit var testedRumMonitorAdapter: RumMonitorAdapter

    @BeforeEach
    fun `set up`() {
        testedRumMonitorAdapter = RumMonitorAdapter(mockNativeRumMonitor)
    }

    @Test
    fun `M set native debug W debug`(
        @BoolForgery fakeDebug: Boolean
    ) {
        // When
        testedRumMonitorAdapter.debug = fakeDebug

        // Then
        verify(mockNativeRumMonitor)::debug.set(fakeDebug)
    }

    @Test
    fun `M return native debug W debug`(
        @BoolForgery fakeDebug: Boolean
    ) {
        // Given
        whenever(mockNativeRumMonitor.debug) doReturn fakeDebug

        // When
        val isDebug = testedRumMonitorAdapter.debug

        // Then
        assertThat(isDebug).isEqualTo(fakeDebug)
    }

    @Test
    fun `M get current session ID W getCurrentSessionId`(
        @StringForgery fakeSessionId: String,
        forge: Forge
    ) {
        // Given
        val mockClosure = mock<(String?) -> Unit>()

        // When
        testedRumMonitorAdapter.getCurrentSessionId(mockClosure)

        // Then
        argumentCaptor<(String?) -> Unit> {
            verify(mockNativeRumMonitor).getCurrentSessionId(capture())
            val expectedSessionId = forge.aNullable { fakeSessionId }

            lastValue.invoke(expectedSessionId)

            verify(mockClosure).invoke(expectedSessionId)
        }
    }

    @Test
    fun `M call native startView W startView`(
        @StringForgery fakeViewName: String,
        forge: Forge
    ) {
        // Given
        val fakeKey = Any()
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startView(fakeKey, fakeViewName, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).startView(fakeKey, fakeViewName, fakeAttributes)
    }

    @Test
    fun `M call native stopView W stopView`(forge: Forge) {
        // Given
        val fakeKey = Any()
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.stopView(fakeKey, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).stopView(fakeKey, fakeAttributes)
    }

    @Test
    fun `M call native addAction W addAction`(
        @Forgery fakeActionType: RumActionType,
        @StringForgery fakeActionName: String,
        forge: Forge
    ) {
        // Given
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.addAction(fakeActionType, fakeActionName, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).addAction(fakeActionType.native, fakeActionName, fakeAttributes)
    }

    @Test
    fun `M call native startAction W startAction`(
        @Forgery fakeActionType: RumActionType,
        @StringForgery fakeActionName: String,
        forge: Forge
    ) {
        // Given
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startAction(fakeActionType, fakeActionName, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).startAction(fakeActionType.native, fakeActionName, fakeAttributes)
    }

    @Test
    fun `M call native stopAction W stopAction`(
        @Forgery fakeActionType: RumActionType,
        @StringForgery fakeActionName: String,
        forge: Forge
    ) {
        // Given
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.stopAction(fakeActionType, fakeActionName, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).stopAction(fakeActionType.native, fakeActionName, fakeAttributes)
    }

    @Test
    fun `M call native startResource W startResource`(
        @StringForgery fakeKey: String,
        @Forgery fakeMethod: RumResourceMethod,
        @StringForgery fakeUrl: String,
        forge: Forge
    ) {
        // Given
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startResource(fakeKey, fakeMethod, fakeUrl, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).startResource(fakeKey, fakeMethod.native, fakeUrl, fakeAttributes)
    }

    @Test
    fun `M call native stopResource W stopResource`(
        @StringForgery fakeKey: String,
        @Forgery fakeResourceKind: RumResourceKind,
        forge: Forge
    ) {
        // Given
        val fakeAttributes = forge.exhaustiveAttributes()
        val fakeStatusCode = forge.aNullable { forge.anInt(min = 100, max = 600) }
        val fakeSize = forge.aNullable { forge.aPositiveLong() }

        // When
        testedRumMonitorAdapter.stopResource(fakeKey, fakeStatusCode, fakeSize, fakeResourceKind, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).stopResource(
            fakeKey,
            fakeStatusCode,
            fakeSize,
            fakeResourceKind.native,
            fakeAttributes
        )
    }

    @Test
    fun `M call native stopResourceWithError W stopResourceWithError`(
        @StringForgery fakeKey: String,
        @StringForgery fakeMessage: String,
        forge: Forge
    ) {
        // Given
        val fakeAttributes = forge.exhaustiveAttributes()
        val fakeThrowable = forge.aThrowable()
        val fakeStatusCode = forge.aNullable { forge.anInt(min = 100, max = 600) }

        // When
        testedRumMonitorAdapter.stopResourceWithError(
            fakeKey,
            fakeStatusCode,
            fakeMessage,
            fakeThrowable,
            fakeAttributes
        )

        // Then
        verify(mockNativeRumMonitor).stopResourceWithError(
            fakeKey,
            fakeStatusCode,
            fakeMessage,
            NativeRumErrorSource.NETWORK,
            fakeThrowable,
            fakeAttributes
        )
    }

    @Test
    fun `M call native addError W addError`(
        @StringForgery fakeMessage: String,
        @Forgery fakeErrorSource: RumErrorSource,
        forge: Forge
    ) {
        // Given
        val fakeAttributes = forge.exhaustiveAttributes()
        val fakeThrowable = forge.aNullable { forge.aThrowable() }

        // When
        testedRumMonitorAdapter.addError(
            fakeMessage,
            fakeErrorSource,
            fakeThrowable,
            fakeAttributes
        )

        // Then
        verify(mockNativeRumMonitor).addError(
            fakeMessage,
            fakeErrorSource.native,
            fakeThrowable,
            fakeAttributes
        )
    }

    @Test
    fun `M call native addTiming W addTiming`(
        @StringForgery fakeTimingName: String
    ) {
        // When
        testedRumMonitorAdapter.addTiming(fakeTimingName)

        // Then
        verify(mockNativeRumMonitor).addTiming(fakeTimingName)
    }

    @Test
    fun `M call native addFeatureFlagEvaluation W addFeatureFlagEvaluation`(
        @StringForgery fakeFlagName: String
    ) {
        // Given
        val fakeFlagValue = Any()

        // When
        testedRumMonitorAdapter.addFeatureFlagEvaluation(fakeFlagName, fakeFlagValue)

        // Then
        verify(mockNativeRumMonitor).addFeatureFlagEvaluation(fakeFlagName, fakeFlagValue)
    }

    @Test
    fun `M call native addFeatureFlagEvaluations W addFeatureFlagEvaluations`(
        forge: Forge
    ) {
        // Given
        val fakeFeatureFlags = forge.aMap {
            anAlphabeticalString() to Any()
        }

        // When
        testedRumMonitorAdapter.addFeatureFlagEvaluations(fakeFeatureFlags)

        // Then
        verify(mockNativeRumMonitor).addFeatureFlagEvaluations(fakeFeatureFlags)
    }

    @Test
    fun `M call native addAttribute W addAttribute`(
        @StringForgery fakeKey: String,
        forge: Forge
    ) {
        // Given
        val fakeValue = forge.aNullable { Any() }

        // When
        testedRumMonitorAdapter.addAttribute(fakeKey, fakeValue)

        // Then
        verify(mockNativeRumMonitor).addAttribute(fakeKey, fakeValue)
    }

    @Test
    fun `M call native removeAttribute W removeAttribute`(
        @StringForgery fakeKey: String
    ) {
        // When
        testedRumMonitorAdapter.removeAttribute(fakeKey)

        // Then
        verify(mockNativeRumMonitor).removeAttribute(fakeKey)
    }

    @OptIn(ExperimentalRumApi::class)
    @Test
    fun `M call native startFeatureOperation W startFeatureOperation`(
        @StringForgery fakeName: String,
        forge: Forge

    ) {
        // Given
        val fakeOperationKey = forge.aNullable { aString() }
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startFeatureOperation(fakeName, fakeOperationKey, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).startFeatureOperation(fakeName, fakeOperationKey, fakeAttributes)
    }

    @OptIn(ExperimentalRumApi::class)
    @Test
    fun `M call native succeedFeatureOperation W succeedFeatureOperation`(
        @StringForgery fakeName: String,
        forge: Forge
    ) {
        // Given
        val fakeOperationKey = forge.aNullable { aString() }
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.succeedFeatureOperation(fakeName, fakeOperationKey, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).succeedFeatureOperation(fakeName, fakeOperationKey, fakeAttributes)
    }

    @OptIn(ExperimentalRumApi::class)
    @Test
    fun `M call native failFeatureOperation W failFeatureOperation`(
        @StringForgery fakeName: String,
        @Forgery fakeFailureReason: FailureReason,
        forge: Forge
    ) {
        // Given
        val fakeOperationKey = forge.aNullable { aString() }
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.failFeatureOperation(fakeName, fakeOperationKey, fakeFailureReason, fakeAttributes)

        // Then
        verify(mockNativeRumMonitor).failFeatureOperation(
            fakeName,
            fakeOperationKey,
            fakeFailureReason.native,
            fakeAttributes
        )
    }

    @OptIn(ExperimentalRumApi::class)
    @Test
    fun `M call native addViewLoadingTime W addViewLoadingTime`(
        @BoolForgery fakeOverwrite: Boolean
    ) {
        // When
        testedRumMonitorAdapter.addViewLoadingTime(fakeOverwrite)

        // Then
        verify(mockNativeRumMonitor).addViewLoadingTime(fakeOverwrite)
    }

    @Test
    fun `M call native stopSession W stopSession`() {
        // When
        testedRumMonitorAdapter.stopSession()

        // Then
        verify(mockNativeRumMonitor).stopSession()
    }

    // region private

    private val RumActionType.native: NativeRumActionType
        get() {
            return when (this) {
                RumActionType.TAP -> NativeRumActionType.TAP
                RumActionType.BACK -> NativeRumActionType.BACK
                RumActionType.CLICK -> NativeRumActionType.CLICK
                RumActionType.SWIPE -> NativeRumActionType.SWIPE
                RumActionType.SCROLL -> NativeRumActionType.SCROLL
                RumActionType.CUSTOM -> NativeRumActionType.CUSTOM
            }
        }

    private val RumResourceMethod.native: NativeRumResourceMethod
        get() {
            return when (this) {
                RumResourceMethod.GET -> NativeRumResourceMethod.GET
                RumResourceMethod.POST -> NativeRumResourceMethod.POST
                RumResourceMethod.PUT -> NativeRumResourceMethod.PUT
                RumResourceMethod.PATCH -> NativeRumResourceMethod.PATCH
                RumResourceMethod.DELETE -> NativeRumResourceMethod.DELETE
                RumResourceMethod.HEAD -> NativeRumResourceMethod.HEAD
                RumResourceMethod.CONNECT -> NativeRumResourceMethod.CONNECT
                RumResourceMethod.TRACE -> NativeRumResourceMethod.TRACE
                RumResourceMethod.OPTIONS -> NativeRumResourceMethod.OPTIONS
            }
        }

    private val RumResourceKind.native: NativeRumResourceKind
        get() {
            return when (this) {
                RumResourceKind.NATIVE -> NativeRumResourceKind.NATIVE
                RumResourceKind.IMAGE -> NativeRumResourceKind.IMAGE
                RumResourceKind.MEDIA -> NativeRumResourceKind.MEDIA
                RumResourceKind.FONT -> NativeRumResourceKind.FONT
                RumResourceKind.FETCH -> NativeRumResourceKind.FETCH
                RumResourceKind.DOCUMENT -> NativeRumResourceKind.DOCUMENT
                RumResourceKind.XHR -> NativeRumResourceKind.XHR
                RumResourceKind.JS -> NativeRumResourceKind.JS
                RumResourceKind.CSS -> NativeRumResourceKind.CSS
                RumResourceKind.BEACON -> NativeRumResourceKind.BEACON
                RumResourceKind.OTHER -> NativeRumResourceKind.OTHER
            }
        }

    private val RumErrorSource.native: NativeRumErrorSource
        get() {
            return when (this) {
                RumErrorSource.SOURCE -> NativeRumErrorSource.SOURCE
                RumErrorSource.LOGGER -> NativeRumErrorSource.LOGGER
                RumErrorSource.WEBVIEW -> NativeRumErrorSource.WEBVIEW
                RumErrorSource.NETWORK -> NativeRumErrorSource.NETWORK
            }
        }

    private val FailureReason.native: NativeFailureReason
        get() {
            return when (this) {
                FailureReason.ERROR -> NativeFailureReason.ERROR
                FailureReason.OTHER -> NativeFailureReason.OTHER
                FailureReason.ABANDONED -> NativeFailureReason.ABANDONED
            }
        }

    // endregion
}
