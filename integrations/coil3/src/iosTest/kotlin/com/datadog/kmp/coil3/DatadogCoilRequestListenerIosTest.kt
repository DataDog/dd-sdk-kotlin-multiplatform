/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.coil3

import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumMonitor
import com.datadog.tools.random.randomThrowable
import dev.mokkery.mock
import dev.mokkery.verify
import platform.Foundation.NSURL
import kotlin.test.Test

/**
 * Covers request data types which only exist on iOS. Common data types are covered by
 * [DatadogCoilRequestListenerTest].
 */
class DatadogCoilRequestListenerIosTest {

    private val mockRumMonitor: RumMonitor = mock()
    private val fakeException: Throwable = randomThrowable()
    private val testable = DatadogCoilRequestListener { mockRumMonitor }

    @Test
    fun `M send RUM error event W NSURL Request fails`() {
        // GIVEN
        val fakeUrl = "https://example.com/image.png"
        val request = ImageRequest.Builder(forgePlatformContext())
            .data(NSURL(string = fakeUrl))
            .build()

        // WHEN
        testable.onError(request, ErrorResult(image = null, request = request, throwable = fakeException))

        // THEN
        verify {
            mockRumMonitor.addError(
                DatadogCoilRequestListener.REQUEST_ERROR_MESSAGE,
                RumErrorSource.SOURCE,
                fakeException,
                mapOf(DatadogCoilRequestListener.REQUEST_PATH_TAG to fakeUrl)
            )
        }
    }
}
