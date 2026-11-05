/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.coil3

import coil3.Uri
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumMonitor
import okio.Path

/**
 * Provides an implementation of [ImageRequest.Listener] already set up to send relevant information
 * to Datadog.
 *
 * It will automatically send RUM error events whenever a Coil [ImageRequest]
 * fails with any [Throwable].
 */
class DatadogCoilRequestListener internal constructor(
    private val rumMonitorProvider: () -> RumMonitor
) : ImageRequest.Listener {

    /**
     * Creates a listener reporting errors to the default [RumMonitor].
     */
    constructor() : this({ RumMonitor.get() })

    // region Listener

    /** @inheritDoc */
    override fun onError(request: ImageRequest, result: ErrorResult) {
        rumMonitorProvider().addError(
            REQUEST_ERROR_MESSAGE,
            RumErrorSource.SOURCE,
            result.throwable,
            extractRequestAttributes(request)
        )
    }

    // endregion

    // region Internal

    private fun extractRequestAttributes(request: ImageRequest): Map<String, Any?> {
        val requestPath = when (val data = request.data) {
            is String -> data
            is Uri -> data.toString()
            is Path -> data.toString()
            else -> platformRequestPath(data)
        }
        return if (requestPath != null) {
            mapOf(REQUEST_PATH_TAG to requestPath)
        } else {
            emptyMap()
        }
    }

    // endregion

    internal companion object {
        internal const val REQUEST_ERROR_MESSAGE = "Coil request error"
        internal const val REQUEST_PATH_TAG = "request_path"
    }
}
