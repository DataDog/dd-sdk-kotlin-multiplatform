/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.internal

import com.datadog.android.rum.internal.domain.event.ResourceTiming
import com.datadog.android.rum.resource.ResourceId
import com.datadog.kmp.rum.ExperimentalRumApi
import com.datadog.kmp.rum.RumActionType
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumMonitor
import com.datadog.kmp.rum.RumResourceKind
import com.datadog.kmp.rum.RumResourceMethod
import com.datadog.kmp.rum.featureoperations.FailureReason
import com.datadog.android.rum.ExperimentalRumApi as NativeExperimentalRumApi
import com.datadog.android.rum.RumActionType as NativeRumActionType
import com.datadog.android.rum.RumErrorSource as NativeRumErrorSource
import com.datadog.android.rum.RumMonitor as NativeRumMonitor
import com.datadog.android.rum.RumResourceKind as NativeRumResourceKind
import com.datadog.android.rum.RumResourceMethod as NativeRumResourceMethod
import com.datadog.android.rum.featureoperations.FailureReason as NativeFailureReason

internal class RumMonitorAdapter(private val nativeRumMonitor: NativeRumMonitor) :
    RumMonitor,
    AdvancedNetworkRumMonitor {

    // region RumMonitor

    override var debug: Boolean
        get() = nativeRumMonitor.debug
        set(value) {
            nativeRumMonitor.debug = value
        }

    override fun getCurrentSessionId(callback: (String?) -> Unit) {
        nativeRumMonitor.getCurrentSessionId(callback)
    }

    override fun startView(key: Any, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.startView(key, name, attributes)
    }

    override fun stopView(key: Any, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopView(key, attributes)
    }

    override fun addAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.addAction(type.native, name, attributes)
    }

    override fun startAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.startAction(type.native, name, attributes)
    }

    override fun stopAction(type: RumActionType, name: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.stopAction(type.native, name, attributes)
    }

    override fun startResource(key: String, method: RumResourceMethod, url: String, attributes: Map<String, Any?>) {
        nativeRumMonitor.startResource(key, method.native, url, attributes)
    }

    override fun stopResource(
        key: String,
        statusCode: Int?,
        size: Long?,
        kind: RumResourceKind,
        attributes: Map<String, Any?>
    ) {
        nativeRumMonitor.stopResource(key, statusCode, size, kind.native, attributes)
    }

    override fun stopResourceWithError(
        key: String,
        statusCode: Int?,
        message: String,
        throwable: Throwable,
        attributes: Map<String, Any?>
    ) {
        nativeRumMonitor.stopResourceWithError(
            key,
            statusCode,
            message,
            NativeRumErrorSource.NETWORK,
            throwable,
            attributes
        )
    }

    override fun addError(
        message: String,
        source: RumErrorSource,
        throwable: Throwable?,
        attributes: Map<String, Any?>
    ) {
        nativeRumMonitor.addError(message, source.native, throwable, attributes)
    }

    override fun addTiming(name: String) {
        nativeRumMonitor.addTiming(name)
    }

    override fun addFeatureFlagEvaluation(name: String, value: Any) {
        nativeRumMonitor.addFeatureFlagEvaluation(name, value)
    }

    override fun addFeatureFlagEvaluations(featureFlags: Map<String, Any>) {
        nativeRumMonitor.addFeatureFlagEvaluations(featureFlags)
    }

    override fun addAttribute(key: String, value: Any?) {
        nativeRumMonitor.addAttribute(key, value)
    }

    override fun removeAttribute(key: String) {
        nativeRumMonitor.removeAttribute(key)
    }

    @OptIn(NativeExperimentalRumApi::class, ExperimentalRumApi::class)
    override fun startFeatureOperation(name: String, operationKey: String?, attributes: Map<String, Any?>) {
        nativeRumMonitor.startFeatureOperation(name, operationKey, attributes)
    }

    @OptIn(NativeExperimentalRumApi::class, ExperimentalRumApi::class)
    override fun succeedFeatureOperation(name: String, operationKey: String?, attributes: Map<String, Any?>) {
        nativeRumMonitor.succeedFeatureOperation(name, operationKey, attributes)
    }

    @OptIn(NativeExperimentalRumApi::class, ExperimentalRumApi::class)
    override fun failFeatureOperation(
        name: String,
        operationKey: String?,
        failureReason: FailureReason,
        attributes: Map<String, Any?>
    ) {
        nativeRumMonitor.failFeatureOperation(name, operationKey, failureReason.native, attributes)
    }

    override fun stopSession() {
        nativeRumMonitor.stopSession()
    }

    // endregion

    // region AdvancedRumNetworkMonitor

    override fun addResourceTiming(key: Any, timing: ResourceTiming) {
        (nativeRumMonitor as? AdvancedNetworkRumMonitor)?.addResourceTiming(key, timing)
    }

    override fun notifyInterceptorInstantiated() {
        (nativeRumMonitor as? AdvancedNetworkRumMonitor)?.notifyInterceptorInstantiated()
    }

    override fun waitForResourceTiming(key: Any) {
        (nativeRumMonitor as? AdvancedNetworkRumMonitor)?.waitForResourceTiming(key)
    }

    override fun startResource(
        key: ResourceId,
        method: NativeRumResourceMethod,
        url: String,
        attributes: Map<String, Any?>
    ) {
        (nativeRumMonitor as? AdvancedNetworkRumMonitor)?.startResource(
            key,
            method,
            url,
            attributes
        )
    }

    override fun stopResource(
        key: ResourceId,
        statusCode: Int?,
        size: Long?,
        kind: NativeRumResourceKind,
        attributes: Map<String, Any?>
    ) {
        (nativeRumMonitor as? AdvancedNetworkRumMonitor)?.stopResource(
            key,
            statusCode,
            size,
            kind,
            attributes
        )
    }

    override fun stopResourceWithError(
        key: ResourceId,
        statusCode: Int?,
        message: String,
        source: NativeRumErrorSource,
        stackTrace: String,
        errorType: String?,
        attributes: Map<String, Any?>
    ) {
        (nativeRumMonitor as? AdvancedNetworkRumMonitor)?.stopResourceWithError(
            key,
            statusCode,
            message,
            source,
            stackTrace,
            errorType,
            attributes
        )
    }

    override fun stopResourceWithError(
        key: ResourceId,
        statusCode: Int?,
        message: String,
        source: NativeRumErrorSource,
        throwable: Throwable,
        attributes: Map<String, Any?>
    ) {
        (nativeRumMonitor as? AdvancedNetworkRumMonitor)?.stopResourceWithError(
            key,
            statusCode,
            message,
            source,
            throwable,
            attributes
        )
    }

    // endregion
}

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
