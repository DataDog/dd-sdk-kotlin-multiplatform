/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.rum

import cocoapods.DatadogObjc.DDRUMMonitor
import com.datadog.kmp.rum.internal.DDRumMonitorProxy
import com.datadog.kmp.rum.internal.RumMonitorAdapter
import platform.Foundation.NSError
import platform.Foundation.NSURLResponse

/**
 *  Stops RUM resource.
 *  @param key the key representing the resource. It must match the one used to start the resource.
 *  @param response the [NSURLResponse] received for the resource.
 *  @param size an optional size of the data received for the resource (in bytes). If not provided, it will be
 *  inferred from the "Content-Length" header of the `response`.
 *  @param attributes custom attributes to attach to this resource.
 */
fun RumMonitor.stopResource(key: String, response: NSURLResponse, size: Long?, attributes: Map<String, Any?>) {
    (this as RumMonitorAdapter).stopResource(key, response, size, attributes)
}

/**
 *  Stops RUM resource with reporting an error.
 *  @param key the key representing the resource. It must match the one used to start the resource.
 *  @param error the [NSError] object received when loading the resource.
 *  @param response an optional [NSURLResponse] received for the resource.
 *  @param attributes custom attributes to attach to this resource.
 */
fun RumMonitor.stopResourceWithError(
    key: String,
    error: NSError,
    response: NSURLResponse?,
    attributes: Map<String, Any?>
) {
    (this as RumMonitorAdapter).stopResourceWithError(key, error, response, attributes)
}

/**
 *  Stops RUM resource with reporting an error.
 *  @param key the key representing the resource. It must match the one used to start the resource.
 *  @param message the message explaining the Resource failure.
 *  @param response an optional [NSURLResponse] received for the resource.
 *  @param attributes custom attributes to attach to this resource.
 */
fun RumMonitor.stopResourceWithError(
    key: String,
    message: String,
    response: NSURLResponse?,
    attributes: Map<String, Any?>
) {
    (this as RumMonitorAdapter).stopResourceWithError(key, message, response, attributes)
}

/**
 *  Adds RUM error to current RUM view.
 *  @param source the origin of the error.
 *  @param error the [NSError] object. It will be used to infer error details.
 *  @param attributes custom attributes to attach to this error.
 */
fun RumMonitor.addError(
    source: RumErrorSource,
    error: NSError,
    attributes: Map<String, Any?>
) {
    (this as RumMonitorAdapter).addError(source, error, attributes)
}

internal actual fun platformRumMonitor(): RumMonitor {
    return RumMonitorAdapter(DDRumMonitorProxy.create(DDRUMMonitor.shared()))
}
