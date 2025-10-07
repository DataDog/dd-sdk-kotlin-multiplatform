/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.internal

import cocoapods.DatadogRUM.DDRUMActionType
import cocoapods.DatadogRUM.DDRUMActionTypeCustom
import cocoapods.DatadogRUM.DDRUMActionTypeScroll
import cocoapods.DatadogRUM.DDRUMActionTypeSwipe
import cocoapods.DatadogRUM.DDRUMActionTypeTap
import cocoapods.DatadogRUM.DDRUMErrorSource
import cocoapods.DatadogRUM.DDRUMErrorSourceNetwork
import cocoapods.DatadogRUM.DDRUMErrorSourceSource
import cocoapods.DatadogRUM.DDRUMErrorSourceWebview
import cocoapods.DatadogRUM.DDRUMFeatureOperationFailureReason
import cocoapods.DatadogRUM.DDRUMFeatureOperationFailureReasonAbandoned
import cocoapods.DatadogRUM.DDRUMFeatureOperationFailureReasonError
import cocoapods.DatadogRUM.DDRUMFeatureOperationFailureReasonOther
import cocoapods.DatadogRUM.DDRUMMethod
import cocoapods.DatadogRUM.DDRUMMethodConnect
import cocoapods.DatadogRUM.DDRUMMethodDelete
import cocoapods.DatadogRUM.DDRUMMethodGet
import cocoapods.DatadogRUM.DDRUMMethodHead
import cocoapods.DatadogRUM.DDRUMMethodOptions
import cocoapods.DatadogRUM.DDRUMMethodPatch
import cocoapods.DatadogRUM.DDRUMMethodPost
import cocoapods.DatadogRUM.DDRUMMethodPut
import cocoapods.DatadogRUM.DDRUMMethodTrace
import cocoapods.DatadogRUM.DDRUMResourceType
import cocoapods.DatadogRUM.DDRUMResourceTypeBeacon
import cocoapods.DatadogRUM.DDRUMResourceTypeCss
import cocoapods.DatadogRUM.DDRUMResourceTypeDocument
import cocoapods.DatadogRUM.DDRUMResourceTypeFetch
import cocoapods.DatadogRUM.DDRUMResourceTypeFont
import cocoapods.DatadogRUM.DDRUMResourceTypeImage
import cocoapods.DatadogRUM.DDRUMResourceTypeJs
import cocoapods.DatadogRUM.DDRUMResourceTypeMedia
import cocoapods.DatadogRUM.DDRUMResourceTypeNative
import cocoapods.DatadogRUM.DDRUMResourceTypeOther
import cocoapods.DatadogRUM.DDRUMResourceTypeXhr
import com.datadog.kmp.rum.RumActionType
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import com.datadog.kmp.rum.featureoperations.FailureReason
import com.datadog.tools.random.exhaustiveAttributes
import com.datadog.tools.random.nullable
import com.datadog.tools.random.randomBoolean
import com.datadog.tools.random.randomEnumValue
import com.datadog.tools.random.randomInt
import com.datadog.tools.random.randomLong
import com.datadog.tools.random.randomThrowable
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSNumber
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSessionTaskMetrics
import platform.Foundation.numberWithInt
import platform.Foundation.numberWithLong
import platform.UIKit.UIViewController
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class RumMonitorAdapterTest {

    private val mockNativeRumMonitor = mock<DDRumMonitorProxy>()

    private lateinit var testedRumMonitorAdapter: RumMonitorAdapter

    @BeforeTest
    fun `set up`() {
        testedRumMonitorAdapter = RumMonitorAdapter(mockNativeRumMonitor)
    }

    // region RumMonitor

    @Test
    fun `M set native debug W debug`() {
        // Given
        val fakeDebug = randomBoolean()

        // When
        testedRumMonitorAdapter.debug = fakeDebug

        // Then
        verify {
            mockNativeRumMonitor.setDebug(fakeDebug)
        }
    }

    @Test
    fun `M return native debug W debug`() {
        // Given
        val fakeDebug = randomBoolean()
        every { mockNativeRumMonitor.debug() } returns fakeDebug

        // When
        val isDebug = testedRumMonitorAdapter.debug

        // Then
        assertEquals(fakeDebug, isDebug)
    }

    @Test
    fun `M call currentSessionIDWithCompletion ID W getCurrentSessionId`() {
        // Given
        val fakeClosure = mock<(String?) -> Unit>()

        // When
        testedRumMonitorAdapter.getCurrentSessionId(fakeClosure)

        // Then
        verify {
            mockNativeRumMonitor.currentSessionIDWithCompletion(fakeClosure)
        }
    }

    @Test
    fun `M call native startViewWithKey W startView + generic key type `() {
        // Given
        val fakeKey = Any()
        val fakeViewName = "fakeView"
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startView(fakeKey, fakeViewName, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.startViewWithKey(
                fakeKey.toString(),
                fakeViewName,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native startViewWithViewController W startView + ViewController key type `() {
        // Given
        val fakeKey = UIViewController()
        val fakeViewName = "fakeView"
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startView(fakeKey, fakeViewName, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.startViewWithViewController(
                fakeKey,
                fakeViewName,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native stopViewWithKey W stopView + generic key type`() {
        // Given
        val fakeKey = Any()
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.stopView(fakeKey, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.stopViewWithKey(fakeKey.toString(), fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native stopViewWithViewController W stopView + ViewController key type`() {
        // Given
        val fakeKey = UIViewController()
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.stopView(fakeKey, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.stopViewWithViewController(fakeKey, fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native addActionWithType W addAction`() {
        // Given
        val fakeActionType = randomEnumValue<RumActionType>()
        val fakeActionName = "fakeAction"
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.addAction(fakeActionType, fakeActionName, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.addActionWithType(
                fakeActionType.native,
                fakeActionName,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native startActionWithType W startAction`() {
        // Given
        val fakeActionType = randomEnumValue<RumActionType>()
        val fakeActionName = "fakeAction"
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startAction(fakeActionType, fakeActionName, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.startActionWithType(
                fakeActionType.native,
                fakeActionName,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native stopActionWithType W stopAction`() {
        // Given
        val fakeActionType = randomEnumValue<RumActionType>()
        val fakeActionName = "fakeAction"
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.stopAction(fakeActionType, fakeActionName, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.stopActionWithType(
                fakeActionType.native,
                fakeActionName,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native startResourceWithResourceKey W startResource`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeMethod = randomEnumValue<RumResourceMethod>()
        val fakeUrl = "http://example.com"
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startResource(fakeKey, fakeMethod, fakeUrl, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.startResourceWithResourceKey(
                fakeKey,
                fakeMethod.native,
                fakeUrl,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native stopResourceWithResourceKey W stopResource`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeResourceKind = randomEnumValue<RumResourceKind>()
        val fakeAttributes = exhaustiveAttributes()
        val fakeStatusCode = nullable(randomInt(from = 100, until = 600))
        val fakeSize = nullable(randomLong(from = 0L))

        // When
        testedRumMonitorAdapter.stopResource(fakeKey, fakeStatusCode, fakeSize, fakeResourceKind, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.stopResourceWithResourceKey(
                fakeKey,
                fakeStatusCode?.let { NSNumber.numberWithInt(it) },
                fakeResourceKind.native,
                fakeSize?.let { NSNumber.numberWithLong(it) },
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Ignore // TODO RUM-11751 Segfault due to null status code, although Objective-C API allow it
    @OptIn(DelicateMokkeryApi::class)
    @Test
    fun `M call native stopResourceWithErrorWithResourceKey W stopResourceWithError + no status code`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeMessage = "fakeMessage"
        val fakeAttributes = exhaustiveAttributes()
        val fakeThrowable = randomThrowable()

        // When
        testedRumMonitorAdapter.stopResourceWithError(
            fakeKey,
            null,
            fakeMessage,
            fakeThrowable,
            fakeAttributes
        )

        // Then
        verify {
            mockNativeRumMonitor.stopResourceWithErrorWithResourceKey(
                fakeKey,
                matches<NSError> {
                    it.domain == "KotlinException" &&
                        it.localizedDescription == "$fakeMessage\n${fakeThrowable.message}"
                },
                null,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @OptIn(DelicateMokkeryApi::class)
    @Test
    fun `M call native stopResourceWithErrorWithResourceKey W stopResourceWithError + with status code`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeMessage = "fakeMessage"
        val fakeAttributes = exhaustiveAttributes()
        val fakeThrowable = randomThrowable()
        val fakeStatusCode = randomInt(from = 100, until = 600)

        // When
        testedRumMonitorAdapter.stopResourceWithError(
            fakeKey,
            fakeStatusCode,
            fakeMessage,
            fakeThrowable,
            fakeAttributes
        )

        // Then
        verify {
            mockNativeRumMonitor.stopResourceWithErrorWithResourceKey(
                fakeKey,
                matches<NSError> {
                    it.domain == "KotlinException" &&
                        it.localizedDescription == "$fakeMessage\n${fakeThrowable.message}"
                },
                matches {
                    it is NSHTTPURLResponse && it.statusCode == fakeStatusCode.toLong()
                },
                (fakeAttributes + (INCLUDE_BINARY_IMAGES_KEY to true)).eraseKeyType()
            )
        }
    }

    @OptIn(DelicateMokkeryApi::class)
    @Test
    fun `M call native addErrorWithError W addError + null throwable`() {
        // Given
        val fakeMessage = "fakeMessage"
        val fakeErrorSource = randomEnumValue<RumErrorSource>()
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.addError(
            fakeMessage,
            fakeErrorSource,
            null,
            fakeAttributes
        )

        // Then
        verify {
            mockNativeRumMonitor.addErrorWithError(
                matches {
                    it.domain == "Error" &&
                        it.localizedDescription == fakeMessage
                },
                fakeErrorSource.native,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @OptIn(DelicateMokkeryApi::class)
    @Test
    fun `M call native addErrorWithError W addError + with throwable`() {
        // Given
        val fakeMessage = "fakeMessage"
        val fakeErrorSource = randomEnumValue<RumErrorSource>()
        val fakeAttributes = exhaustiveAttributes()
        val fakeThrowable = randomThrowable()

        // When
        testedRumMonitorAdapter.addError(
            fakeMessage,
            fakeErrorSource,
            fakeThrowable,
            fakeAttributes
        )

        // Then
        verify {
            mockNativeRumMonitor.addErrorWithError(
                matches {
                    it.domain == "KotlinException" &&
                        it.localizedDescription == "$fakeMessage\n${fakeThrowable.message}"
                },
                fakeErrorSource.native,
                (fakeAttributes + (INCLUDE_BINARY_IMAGES_KEY to true)).eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native addTimingWithName W addTiming`() {
        // Given
        val fakeTimingName = "fakeTiming"

        // When
        testedRumMonitorAdapter.addTiming(fakeTimingName)

        // Then
        verify { mockNativeRumMonitor.addTimingWithName(fakeTimingName) }
    }

    @Test
    fun `M call native addFeatureFlagEvaluationWithName W addFeatureFlagEvaluation`() {
        // Given
        val fakeFlagName = "fakeFlag"
        val fakeFlagValue = Any()

        // When
        testedRumMonitorAdapter.addFeatureFlagEvaluation(fakeFlagName, fakeFlagValue)

        // Then
        verify { mockNativeRumMonitor.addFeatureFlagEvaluationWithName(fakeFlagName, fakeFlagValue) }
    }

    @Test
    fun `M call native addFeatureFlagEvaluationWithName W addFeatureFlagEvaluations`() {
        // Given
        val fakeFeatureFlags = exhaustiveAttributes()
            .filterValues { it != null }
            .mapValues { it }

        // When
        testedRumMonitorAdapter.addFeatureFlagEvaluations(fakeFeatureFlags)

        // Then
        verify {
            fakeFeatureFlags.forEach {
                mockNativeRumMonitor.addFeatureFlagEvaluationWithName(it.key, it.value)
            }
        }
    }

    @Test
    fun `M call native addAttributeForKey W addAttribute`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeValue = nullable(Any())

        // When
        testedRumMonitorAdapter.addAttribute(fakeKey, fakeValue)

        // Then
        verify { mockNativeRumMonitor.addAttributeForKey(fakeKey, fakeValue) }
    }

    @Test
    fun `M call native removeAttributeForKey W removeAttribute`() {
        // Given
        val fakeKey = "fakeKey"

        // When
        testedRumMonitorAdapter.removeAttribute(fakeKey)

        // Then
        verify { mockNativeRumMonitor.removeAttributeForKey(fakeKey) }
    }

    @Test
    fun `M call native startFeatureOperation W startFeatureOperation`() {
        // Given
        val fakeName = "fakeOperationName"
        val fakeOperationKey = nullable("fakeOperationKey")
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.startFeatureOperation(fakeName, fakeOperationKey, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.startFeatureOperation(fakeName, fakeOperationKey, fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native succeedFeatureOperation W succeedFeatureOperation`() {
        // Given
        val fakeName = "fakeOperationName"
        val fakeOperationKey = nullable("fakeOperationKey")
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.succeedFeatureOperation(fakeName, fakeOperationKey, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.succeedFeatureOperation(fakeName, fakeOperationKey, fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native failFeatureOperation W failFeatureOperation`() {
        // Given
        val fakeName = "fakeOperationName"
        val fakeOperationKey = nullable("fakeOperationKey")
        val fakeFailureReason = randomEnumValue<FailureReason>()
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.failFeatureOperation(fakeName, fakeOperationKey, fakeFailureReason, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.failFeatureOperation(
                fakeName,
                fakeOperationKey,
                fakeFailureReason.native,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native addViewLoadingTime W addViewLoadingTime`() {
        // Given
        val fakeOverwrite = randomBoolean()

        // When
        testedRumMonitorAdapter.addViewLoadingTime(fakeOverwrite)

        // Then
        verify {
            mockNativeRumMonitor.addViewLoadingTime(fakeOverwrite)
        }
    }

    @Test
    fun `M call native stopSession W stopSession`() {
        // When
        testedRumMonitorAdapter.stopSession()

        // Then
        verify { mockNativeRumMonitor.stopSession() }
    }

    // endregion

    // region AdvanceRumNetworkMonitor

    @Test
    fun `M call native addResourceMetricsWithResourceKey W addResourceMetrics`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeMetrics = NSURLSessionTaskMetrics()
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.addResourceMetrics(fakeKey, fakeMetrics, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.addResourceMetricsWithResourceKey(
                fakeKey,
                fakeMetrics,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    // endregion

    @Test
    fun `M call native stopResourceWithResourceKey W stopResource + platform types`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeResponse = NSURLResponse()
        val fakeSize = nullable(randomLong(from = 0L))
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.stopResource(fakeKey, fakeResponse, fakeSize, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.stopResourceWithResourceKey(
                fakeKey,
                fakeResponse,
                fakeSize?.let { NSNumber.numberWithLong(it) },
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native stopResourceWithErrorWithResourceKey W stopResourceWithError + platform types + NSError`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeError = NSError.errorWithDomain("Error", 0, null)
        val fakeResponse = nullable(NSURLResponse())
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.stopResourceWithError(fakeKey, fakeError, fakeResponse, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.stopResourceWithErrorWithResourceKey(
                fakeKey,
                fakeError,
                fakeResponse,
                (fakeAttributes + (INCLUDE_BINARY_IMAGES_KEY to true)).eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native stopResourceWithErrorWithResourceKey W stopResourceWithError + platform types + message`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeMessage = "fakeError"
        val fakeResponse = nullable(NSURLResponse())
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.stopResourceWithError(fakeKey, fakeMessage, fakeResponse, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.stopResourceWithErrorWithResourceKey(
                fakeKey,
                fakeMessage,
                fakeResponse,
                fakeAttributes.eraseKeyType()
            )
        }
    }

    @Test
    fun `M call native addErrorWithError W addError + platform types`() {
        // Given
        val fakeSource = randomEnumValue<RumErrorSource>()
        val fakeError = NSError.errorWithDomain("Error", 0, null)
        val fakeAttributes = exhaustiveAttributes()

        // When
        testedRumMonitorAdapter.addError(fakeSource, fakeError, fakeAttributes)

        // Then
        verify {
            mockNativeRumMonitor.addErrorWithError(
                fakeError,
                fakeSource.native,
                (fakeAttributes + (INCLUDE_BINARY_IMAGES_KEY to true)).eraseKeyType()
            )
        }
    }

    // region private

    private fun Map<String, Any?>.eraseKeyType(): Map<Any?, Any?> = mapKeys {
        @Suppress("USELESS_CAST")
        it.key as Any
    }

    private val RumActionType.native: DDRUMActionType
        get() {
            return when (this) {
                // not a bug, iOS SDK has no click/back alternatives
                RumActionType.TAP -> DDRUMActionTypeTap
                RumActionType.BACK -> DDRUMActionTypeTap
                RumActionType.CLICK -> DDRUMActionTypeTap
                RumActionType.SWIPE -> DDRUMActionTypeSwipe
                RumActionType.SCROLL -> DDRUMActionTypeScroll
                RumActionType.CUSTOM -> DDRUMActionTypeCustom
            }
        }

    private val RumResourceMethod.native: DDRUMMethod
        get() {
            return when (this) {
                RumResourceMethod.GET -> DDRUMMethodGet
                RumResourceMethod.POST -> DDRUMMethodPost
                RumResourceMethod.PUT -> DDRUMMethodPut
                RumResourceMethod.PATCH -> DDRUMMethodPatch
                RumResourceMethod.DELETE -> DDRUMMethodDelete
                RumResourceMethod.HEAD -> DDRUMMethodHead
                RumResourceMethod.CONNECT -> DDRUMMethodConnect
                RumResourceMethod.TRACE -> DDRUMMethodTrace
                RumResourceMethod.OPTIONS -> DDRUMMethodOptions
            }
        }

    private val RumResourceKind.native: DDRUMResourceType
        get() {
            return when (this) {
                RumResourceKind.NATIVE -> DDRUMResourceTypeNative
                RumResourceKind.IMAGE -> DDRUMResourceTypeImage
                RumResourceKind.MEDIA -> DDRUMResourceTypeMedia
                RumResourceKind.FONT -> DDRUMResourceTypeFont
                RumResourceKind.FETCH -> DDRUMResourceTypeFetch
                RumResourceKind.DOCUMENT -> DDRUMResourceTypeDocument
                RumResourceKind.XHR -> DDRUMResourceTypeXhr
                RumResourceKind.JS -> DDRUMResourceTypeJs
                RumResourceKind.CSS -> DDRUMResourceTypeCss
                RumResourceKind.BEACON -> DDRUMResourceTypeBeacon
                RumResourceKind.OTHER -> DDRUMResourceTypeOther
            }
        }

    private val RumErrorSource.native: DDRUMErrorSource
        get() {
            return when (this) {
                RumErrorSource.SOURCE -> DDRUMErrorSourceSource
                // TODO RUM-4844 iOS has no value for logger
                RumErrorSource.LOGGER -> DDRUMErrorSourceSource
                RumErrorSource.WEBVIEW -> DDRUMErrorSourceWebview
                RumErrorSource.NETWORK -> DDRUMErrorSourceNetwork
            }
        }

    private val FailureReason.native: DDRUMFeatureOperationFailureReason
        get() {
            return when (this) {
                FailureReason.ERROR -> DDRUMFeatureOperationFailureReasonError
                FailureReason.OTHER -> DDRUMFeatureOperationFailureReasonOther
                FailureReason.ABANDONED -> DDRUMFeatureOperationFailureReasonAbandoned
            }
        }

    private companion object {
        const val INCLUDE_BINARY_IMAGES_KEY = "_dd.error.include_binary_images"
    }

    // endregion
}
