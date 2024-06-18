/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.internal

import cocoapods.DatadogObjc.DDRUMActionType
import cocoapods.DatadogObjc.DDRUMActionTypeCustom
import cocoapods.DatadogObjc.DDRUMActionTypeScroll
import cocoapods.DatadogObjc.DDRUMActionTypeSwipe
import cocoapods.DatadogObjc.DDRUMActionTypeTap
import cocoapods.DatadogObjc.DDRUMErrorSource
import cocoapods.DatadogObjc.DDRUMErrorSourceNetwork
import cocoapods.DatadogObjc.DDRUMErrorSourceSource
import cocoapods.DatadogObjc.DDRUMErrorSourceWebview
import cocoapods.DatadogObjc.DDRUMMethod
import cocoapods.DatadogObjc.DDRUMMethodConnect
import cocoapods.DatadogObjc.DDRUMMethodDelete
import cocoapods.DatadogObjc.DDRUMMethodGet
import cocoapods.DatadogObjc.DDRUMMethodHead
import cocoapods.DatadogObjc.DDRUMMethodOptions
import cocoapods.DatadogObjc.DDRUMMethodPatch
import cocoapods.DatadogObjc.DDRUMMethodPost
import cocoapods.DatadogObjc.DDRUMMethodPut
import cocoapods.DatadogObjc.DDRUMMethodTrace
import cocoapods.DatadogObjc.DDRUMResourceType
import cocoapods.DatadogObjc.DDRUMResourceTypeBeacon
import cocoapods.DatadogObjc.DDRUMResourceTypeCss
import cocoapods.DatadogObjc.DDRUMResourceTypeDocument
import cocoapods.DatadogObjc.DDRUMResourceTypeFetch
import cocoapods.DatadogObjc.DDRUMResourceTypeFont
import cocoapods.DatadogObjc.DDRUMResourceTypeImage
import cocoapods.DatadogObjc.DDRUMResourceTypeJs
import cocoapods.DatadogObjc.DDRUMResourceTypeMedia
import cocoapods.DatadogObjc.DDRUMResourceTypeNative
import cocoapods.DatadogObjc.DDRUMResourceTypeOther
import cocoapods.DatadogObjc.DDRUMResourceTypeXhr
import com.datadog.kmp.rum.RumActionType
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSLocalizedDescriptionKey
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
            nativeRumMonitor.startViewWithViewController(key, name, attributes.eraseKeyType())
        } else {
            nativeRumMonitor.startViewWithKey(key.toString(), name, attributes.eraseKeyType())
        }
    }

    override fun stopView(key: Any, attributes: Map<String, Any?>) {
        if (key is UIViewController) {
            nativeRumMonitor.stopViewWithViewController(key, attributes.eraseKeyType())
        } else {
            nativeRumMonitor.stopViewWithKey(key.toString(), attributes.eraseKeyType())
        }
    }

    override fun addAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.addActionWithType(type.native, name, attributes.eraseKeyType())
    }

    override fun startAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.startActionWithType(type.native, name, attributes.eraseKeyType())
    }

    override fun stopAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopActionWithType(type.native, name, attributes.eraseKeyType())
    }

    override fun startResource(key: String, method: RumResourceMethod, url: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.startResourceWithResourceKey(key, method.native, url, attributes.eraseKeyType())
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
            attributes.eraseKeyType()
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
            toNsError(throwable, message),
            response,
            attributes.eraseKeyType()
        )
    }

    override fun addError(
        message: String,
        source: RumErrorSource,
        throwable: Throwable?,
        attributes: Map<String, Any?>
    ) {
        // iOS addError signature doesn't allow nullable NSError argument, while similar API on Android allows
        // nullable Throwable
        val error = if (throwable != null) {
            toNsError(throwable, message)
        } else {
            NSError.errorWithDomain("Error", 0, mapOf(NSLocalizedDescriptionKey to message))
        }
        nativeRumMonitor.addErrorWithError(error, source.native, attributes.eraseKeyType())
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
        nativeRumMonitor.addResourceMetricsWithResourceKey(key, metrics, attributes.eraseKeyType())
    }

    // endregion

    fun stopResource(key: String, response: NSURLResponse, size: Long?, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopResourceWithResourceKey(key, response, size.asNSNumber, attributes.eraseKeyType())
    }

    fun stopResourceWithError(key: String, error: NSError, response: NSURLResponse?, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopResourceWithErrorWithResourceKey(key, error, response, attributes.eraseKeyType())
    }

    fun stopResourceWithError(key: String, message: String, response: NSURLResponse?, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopResourceWithErrorWithResourceKey(key, message, response, attributes.eraseKeyType())
    }

    fun addError(
        source: RumErrorSource,
        error: NSError,
        attributes: Map<String, Any?>
    ) {
        nativeRumMonitor.addErrorWithError(error, source.native, attributes.eraseKeyType())
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

private fun Map<String, Any?>.eraseKeyType(): Map<Any?, Any?> = mapKeys {
    @Suppress("USELESS_CAST")
    it.key as Any
}

// TODO RUM-4491 This is temporary, we need to have a proper conversion between Throwable and Error
private fun toNsError(throwable: Throwable, userMessage: String? = null): NSError {
    val userInfo = mutableMapOf<Any?, Any>()
    userInfo["KotlinException"] = throwable
    val message = if (userMessage != null && !throwable.message.isNullOrBlank()) {
        "${userMessage}\n${throwable.message}"
    } else if (throwable.message.isNullOrBlank()) {
        userMessage
    } else {
        throwable.message
    }
    if (message != null) {
        userInfo[NSLocalizedDescriptionKey] = message
    }
    return NSError.errorWithDomain("KotlinException", 0, userInfo.ifEmpty { null })
}
