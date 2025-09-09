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
import com.datadog.kmp.internal.createNSErrorFromMessage
import com.datadog.kmp.internal.createNSErrorFromThrowable
import com.datadog.kmp.internal.eraseKeyType
import com.datadog.kmp.internal.withIncludeBinaryImages
import com.datadog.kmp.rum.RumActionType
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSessionTaskMetrics
import platform.Foundation.numberWithInt
import platform.Foundation.numberWithLong
import platform.UIKit.UIViewController

internal class RumMonitorAdapter(
    private val nativeRumMonitor: DDRumMonitorProxy
) : RumMonitor, AdvancedRumNetworkMonitor {

    // region RumMonitor

    override var debug: Boolean
        get() = nativeRumMonitor.debug()
        set(value) {
            nativeRumMonitor.setDebug(value)
        }

    override fun getCurrentSessionId(callback: (String?) -> Unit) {
        nativeRumMonitor.currentSessionIDWithCompletion(callback)
    }

    override fun startView(key: Any, name: String, attributes: Map<String, Any?>) {
        if (key is UIViewController) {
            nativeRumMonitor.startViewWithViewController(key, name, eraseKeyType(attributes))
        } else {
            nativeRumMonitor.startViewWithKey(key.toString(), name, eraseKeyType(attributes))
        }
    }

    override fun stopView(key: Any, attributes: Map<String, Any?>) {
        if (key is UIViewController) {
            nativeRumMonitor.stopViewWithViewController(key, eraseKeyType(attributes))
        } else {
            nativeRumMonitor.stopViewWithKey(key.toString(), eraseKeyType(attributes))
        }
    }

    override fun addAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.addActionWithType(type.native, name, eraseKeyType(attributes))
    }

    override fun startAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.startActionWithType(type.native, name, eraseKeyType(attributes))
    }

    override fun stopAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopActionWithType(type.native, name, eraseKeyType(attributes))
    }

    override fun startResource(key: String, method: RumResourceMethod, url: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.startResourceWithResourceKey(key, method.native, url, eraseKeyType(attributes))
    }

    override fun stopResource(
        key: String,
        statusCode: Int?,
        size: Long?,
        kind: RumResourceKind,
        attributes: Map<String, Any?>
    ) {
        nativeRumMonitor.stopResourceWithResourceKey(
            key,
            statusCode.asNSNumber,
            kind.native,
            size.asNSNumber,
            eraseKeyType(attributes)
        )
    }

    override fun stopResourceWithError(
        key: String,
        statusCode: Int?,
        message: String,
        throwable: Throwable,
        attributes: Map<String, Any?>
    ) {
        val response = if (statusCode != null) {
            // TODO RUM-4963 Add iOS SDK API to stop resource with error without a need of NSURLResponse
            // NSHTTPURLResponse cannot be constructed by valid URL, but it won't be used anyway by iOS SDK, it will
            // fetch only status code value
            NSHTTPURLResponse(NSURL(), statusCode.toLong(), null, null)
        } else {
            null
        }
        nativeRumMonitor.stopResourceWithErrorWithResourceKey(
            key,
            createNSErrorFromThrowable(throwable, message),
            response,
            eraseKeyType(withIncludeBinaryImages(attributes))
        )
    }

    override fun addError(
        message: String,
        source: RumErrorSource,
        throwable: Throwable?,
        attributes: Map<String, Any?>
    ) {
        val (error, resolvedAttributes) = if (throwable != null) {
            createNSErrorFromThrowable(throwable, message) to withIncludeBinaryImages(attributes)
        } else {
            createNSErrorFromMessage(message) to attributes
        }

        nativeRumMonitor.addErrorWithError(
            error,
            source.native,
            eraseKeyType(resolvedAttributes)
        )
    }

    override fun addTiming(name: String) {
        nativeRumMonitor.addTimingWithName(name)
    }

    override fun addFeatureFlagEvaluation(name: String, value: Any) {
        nativeRumMonitor.addFeatureFlagEvaluationWithName(name, value)
    }

    override fun addFeatureFlagEvaluations(featureFlags: Map<String, Any>) {
        featureFlags.forEach {
            nativeRumMonitor.addFeatureFlagEvaluationWithName(it.key, it.value)
        }
    }

    override fun addAttribute(key: String, value: Any?) {
        nativeRumMonitor.addAttributeForKey(key, value)
    }

    override fun removeAttribute(key: String) {
        nativeRumMonitor.removeAttributeForKey(key)
    }

    override fun stopSession() {
        nativeRumMonitor.stopSession()
    }

    // endregion

    // region platform-specific methods

    // region AdvancedRumNetworkMonitor

    override fun addResourceMetrics(
        key: String,
        metrics: NSURLSessionTaskMetrics,
        attributes: Map<String, Any?>
    ) {
        nativeRumMonitor.addResourceMetricsWithResourceKey(key, metrics, eraseKeyType(attributes))
    }

    // endregion

    fun stopResource(key: String, response: NSURLResponse, size: Long?, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopResourceWithResourceKey(key, response, size.asNSNumber, eraseKeyType(attributes))
    }

    fun stopResourceWithError(key: String, error: NSError, response: NSURLResponse?, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopResourceWithErrorWithResourceKey(
            key,
            error,
            response,
            eraseKeyType(withIncludeBinaryImages(attributes))
        )
    }

    fun stopResourceWithError(key: String, message: String, response: NSURLResponse?, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopResourceWithErrorWithResourceKey(key, message, response, eraseKeyType(attributes))
    }

    fun addError(
        source: RumErrorSource,
        error: NSError,
        attributes: Map<String, Any?>
    ) {
        nativeRumMonitor.addErrorWithError(error, source.native, eraseKeyType(withIncludeBinaryImages(attributes)))
    }

    // endregion
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

private val Int?.asNSNumber: NSNumber?
    get() = if (this != null) NSNumber.numberWithInt(this) else null

private val Long?.asNSNumber: NSNumber?
    get() = if (this != null) NSNumber.numberWithLong(this) else null
