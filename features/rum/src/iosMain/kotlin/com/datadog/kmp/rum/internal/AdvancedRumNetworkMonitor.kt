/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum.internal

import platform.Foundation.NSURLSessionTaskMetrics

/**
 * For internal usage only.
 */
interface AdvancedRumNetworkMonitor {
    /**
     *  Adds temporal metrics to given RUM resource.
     *  It must be called before the resource is stopped.
     *  @param key the key representing the resource. It must match the one used to start the resource.
     *  @param metrics the [NSURLSessionTaskMetrics] for this resource.
     *  @param attributes custom attributes to attach to this resource.
     */
    fun addResourceMetrics(
        key: String,
        metrics: NSURLSessionTaskMetrics,
        attributes: Map<String, Any?>
    )
}
