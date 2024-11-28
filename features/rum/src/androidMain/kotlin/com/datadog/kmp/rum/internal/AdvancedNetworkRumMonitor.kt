/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.internal

import com.datadog.android.rum.RumErrorSource
import com.datadog.android.rum.RumResourceKind
import com.datadog.android.rum.RumResourceMethod
import com.datadog.android.rum.internal.domain.event.ResourceTiming
import com.datadog.android.rum.resource.ResourceId

internal interface AdvancedNetworkRumMonitor {
    fun waitForResourceTiming(key: Any)
    fun addResourceTiming(key: Any, timing: ResourceTiming)
    fun notifyInterceptorInstantiated()

    fun startResource(
        key: ResourceId,
        method: RumResourceMethod,
        url: String,
        attributes: Map<String, Any?> = emptyMap()
    )

    fun stopResource(
        key: ResourceId,
        statusCode: Int?,
        size: Long?,
        kind: RumResourceKind,
        attributes: Map<String, Any?>
    )

    fun stopResourceWithError(
        key: ResourceId,
        statusCode: Int?,
        message: String,
        source: RumErrorSource,
        throwable: Throwable,
        attributes: Map<String, Any?> = emptyMap()
    )

    @SuppressWarnings("LongParameterList")
    fun stopResourceWithError(
        key: ResourceId,
        statusCode: Int?,
        message: String,
        source: RumErrorSource,
        stackTrace: String,
        errorType: String?,
        attributes: Map<String, Any?> = emptyMap()
    )
}
