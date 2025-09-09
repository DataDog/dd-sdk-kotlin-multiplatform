/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.internal

import cocoapods.DatadogRUM.DDRUMActionType
import cocoapods.DatadogRUM.DDRUMErrorSource
import cocoapods.DatadogRUM.DDRUMMethod
import cocoapods.DatadogRUM.DDRUMMonitor
import cocoapods.DatadogRUM.DDRUMResourceType
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSessionTaskMetrics
import platform.UIKit.UIViewController

/**
 * This one is needed, because we cannot mock DDRUMMonitor (mixing ObjC and Kotlin supertypes is not allowed) and
 * DDRUMMonitor has no public constructor, so we cannot extend it manually to intercept calls. This interface
 * implementation will just proxy all calls to DDRUMMonitor without any additional business logic, but
 * we will be available to mock it.
 */
@Suppress("TooManyFunctions")
internal interface DDRumMonitorProxy {
    fun addActionWithType(
        type: DDRUMActionType,
        name: String,
        attributes: Map<Any?, *>
    )

    fun addAttributeForKey(key: String, value: Any?)

    fun addErrorWithError(
        error: NSError,
        source: DDRUMErrorSource,
        attributes: Map<Any?, *>
    )

    fun addErrorWithMessage(
        message: String,
        stack: String?,
        source: DDRUMErrorSource,
        attributes: Map<Any?, *>
    )

    fun addFeatureFlagEvaluationWithName(name: String, value: Any?)

    fun addResourceMetricsWithResourceKey(
        resourceKey: String,
        metrics: NSURLSessionTaskMetrics,
        attributes: Map<Any?, *>
    )

    fun addTimingWithName(name: String)

    fun currentSessionIDWithCompletion(completion: (String?) -> Unit)

    fun debug(): Boolean

    fun removeAttributeForKey(key: String)

    fun setDebug(debug: Boolean)

    fun startActionWithType(
        type: DDRUMActionType,
        name: String,
        attributes: Map<Any?, *>
    )

    fun startResourceWithResourceKey(
        resourceKey: String,
        httpMethod: DDRUMMethod,
        urlString: String,
        attributes: Map<Any?, *>
    )

    fun startResourceWithResourceKey(
        resourceKey: String,
        request: NSURLRequest,
        attributes: Map<Any?, *>
    )

    fun startResourceWithResourceKey(
        resourceKey: String,
        url: platform.Foundation.NSURL,
        attributes: Map<Any?, *>
    )

    fun startViewWithKey(key: String, name: String?, attributes: Map<Any?, *>)

    fun startViewWithViewController(
        viewController: UIViewController,
        name: String?,
        attributes: Map<Any?, *>
    )

    fun stopActionWithType(
        type: DDRUMActionType,
        name: String?,
        attributes: Map<Any?, *>
    )

    fun stopResourceWithErrorWithResourceKey(
        resourceKey: String,
        error: NSError,
        response: NSURLResponse?,
        attributes: Map<Any?, *>
    )

    fun stopResourceWithErrorWithResourceKey(
        resourceKey: String,
        message: String,
        response: NSURLResponse?,
        attributes: Map<Any?, *>
    )

    fun stopResourceWithResourceKey(
        resourceKey: String,
        response: NSURLResponse,
        size: NSNumber?,
        attributes: Map<Any?, *>
    )

    fun stopResourceWithResourceKey(
        resourceKey: String,
        statusCode: NSNumber?,
        kind: DDRUMResourceType,
        size: NSNumber?,
        attributes: Map<Any?, *>
    )

    fun stopSession()

    fun stopViewWithKey(key: String, attributes: Map<Any?, *>)

    fun stopViewWithViewController(
        viewController: UIViewController,
        attributes: Map<Any?, *>
    )

    companion object {
        fun create(nativeRumMonitor: DDRUMMonitor): DDRumMonitorProxy = object : DDRumMonitorProxy {
            override fun addActionWithType(
                type: DDRUMActionType,
                name: String,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.addActionWithType(type, name, attributes)

            override fun addAttributeForKey(key: String, value: Any?) = nativeRumMonitor.addAttributeForKey(key, value)

            override fun addErrorWithError(
                error: NSError,
                source: DDRUMErrorSource,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.addErrorWithError(error, source, attributes)

            override fun addErrorWithMessage(
                message: String,
                stack: String?,
                source: DDRUMErrorSource,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.addErrorWithMessage(message, stack, source, attributes)

            override fun addFeatureFlagEvaluationWithName(name: String, value: Any?) =
                nativeRumMonitor.addFeatureFlagEvaluationWithName(name, value)

            override fun addResourceMetricsWithResourceKey(
                resourceKey: String,
                metrics: NSURLSessionTaskMetrics,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.addResourceMetricsWithResourceKey(resourceKey, metrics, attributes)

            override fun addTimingWithName(name: String) = nativeRumMonitor.addTimingWithName(name)

            override fun currentSessionIDWithCompletion(completion: (String?) -> Unit) =
                nativeRumMonitor.currentSessionIDWithCompletion(completion)

            override fun debug(): Boolean = nativeRumMonitor.debug()

            override fun removeAttributeForKey(key: String) = nativeRumMonitor.removeAttributeForKey(key)

            override fun setDebug(debug: Boolean) = nativeRumMonitor.setDebug(debug)

            override fun startActionWithType(
                type: DDRUMActionType,
                name: String,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.startActionWithType(type, name, attributes)

            override fun startResourceWithResourceKey(
                resourceKey: String,
                httpMethod: DDRUMMethod,
                urlString: String,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.startResourceWithResourceKey(resourceKey, httpMethod, urlString, attributes)

            override fun startResourceWithResourceKey(
                resourceKey: String,
                request: NSURLRequest,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.startResourceWithResourceKey(resourceKey, request, attributes)

            override fun startResourceWithResourceKey(
                resourceKey: String,
                url: platform.Foundation.NSURL,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.startResourceWithResourceKey(resourceKey, url, attributes)

            override fun startViewWithKey(key: String, name: String?, attributes: Map<Any?, *>) =
                nativeRumMonitor.startViewWithKey(key, name, attributes)

            override fun startViewWithViewController(
                viewController: UIViewController,
                name: String?,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.startViewWithViewController(viewController, name, attributes)

            override fun stopActionWithType(
                type: DDRUMActionType,
                name: String?,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.stopActionWithType(type, name, attributes)

            override fun stopResourceWithErrorWithResourceKey(
                resourceKey: String,
                error: NSError,
                response: NSURLResponse?,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.stopResourceWithErrorWithResourceKey(resourceKey, error, response, attributes)

            override fun stopResourceWithErrorWithResourceKey(
                resourceKey: String,
                message: String,
                response: NSURLResponse?,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.stopResourceWithErrorWithResourceKey(resourceKey, message, response, attributes)

            override fun stopResourceWithResourceKey(
                resourceKey: String,
                response: NSURLResponse,
                size: NSNumber?,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.stopResourceWithResourceKey(resourceKey, response, size, attributes)

            override fun stopResourceWithResourceKey(
                resourceKey: String,
                statusCode: NSNumber?,
                kind: DDRUMResourceType,
                size: NSNumber?,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.stopResourceWithResourceKey(resourceKey, statusCode, kind, size, attributes)

            override fun stopSession() = nativeRumMonitor.stopSession()

            override fun stopViewWithKey(key: String, attributes: Map<Any?, *>) =
                nativeRumMonitor.stopViewWithKey(key, attributes)

            override fun stopViewWithViewController(
                viewController: UIViewController,
                attributes: Map<Any?, *>
            ) = nativeRumMonitor.stopViewWithViewController(viewController, attributes)
        }
    }
}
