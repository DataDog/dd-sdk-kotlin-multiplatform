/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.coil3

import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.toUri
import com.datadog.kmp.rum.RumErrorSource
import com.datadog.kmp.rum.RumMonitor
import com.datadog.tools.random.randomElement
import com.datadog.tools.random.randomThrowable
import com.datadog.tools.random.randomULong
import dev.mokkery.mock
import dev.mokkery.verify
import okio.Path.Companion.toPath
import kotlin.test.BeforeTest
import kotlin.test.Test

class DatadogCoilRequestListenerTest {

    private lateinit var testable: DatadogCoilRequestListener

    private val mockRumMonitor: RumMonitor = mock()

    private val fakeException: Throwable = randomThrowable()

    @BeforeTest
    fun `set up`() {
        testable = DatadogCoilRequestListener { mockRumMonitor }
    }

    // region Unit Tests

    @Test
    fun `M send RUM error event W raw string path Request fails`() {
        // GIVEN
        val fakePath = forgeImagePath()
        val fakeRequest = forgeImageRequest(fakePath)

        // WHEN
        testable.onError(fakeRequest, forgeErrorResult(fakeRequest))

        // THEN
        verifyErrorSent(mapOf(DatadogCoilRequestListener.REQUEST_PATH_TAG to fakePath))
    }

    @Test
    fun `M send RUM error event W coil3 Uri Request fails`() {
        // GIVEN
        val fakePath = forgeImagePath()
        val fakeRequest = forgeImageRequest(fakePath.toUri())

        // WHEN
        testable.onError(fakeRequest, forgeErrorResult(fakeRequest))

        // THEN
        verifyErrorSent(mapOf(DatadogCoilRequestListener.REQUEST_PATH_TAG to fakePath))
    }

    @Test
    fun `M send RUM error event W okio Path Request fails`() {
        // GIVEN
        val fakePath = "/images/${forgeImageFileName()}".toPath()
        val fakeRequest = forgeImageRequest(fakePath)

        // WHEN
        testable.onError(fakeRequest, forgeErrorResult(fakeRequest))

        // THEN
        verifyErrorSent(mapOf(DatadogCoilRequestListener.REQUEST_PATH_TAG to fakePath.toString()))
    }

    @Test
    fun `M send RUM error event with empty attributes W unknown data type Request fails`() {
        // GIVEN
        val fakeRequest = forgeImageRequest(Any())

        // WHEN
        testable.onError(fakeRequest, forgeErrorResult(fakeRequest))

        // THEN
        verifyErrorSent(emptyMap())
    }

    // endregion

    // region Internals

    private fun forgeImageRequest(data: Any): ImageRequest {
        return ImageRequest.Builder(forgePlatformContext())
            .data(data)
            .build()
    }

    private fun forgeErrorResult(request: ImageRequest): ErrorResult {
        return ErrorResult(image = null, request = request, throwable = fakeException)
    }

    private fun forgeImagePath(): String {
        return "${listOf("http", "https").randomElement()}://example.com/${forgeImageFileName()}"
    }

    private fun forgeImageFileName(): String {
        return "${randomULong()}.${listOf("png", "jpeg", "gif").randomElement()}"
    }

    private fun verifyErrorSent(expectedAttributes: Map<String, Any?>) {
        verify {
            mockRumMonitor.addError(
                DatadogCoilRequestListener.REQUEST_ERROR_MESSAGE,
                RumErrorSource.SOURCE,
                fakeException,
                expectedAttributes
            )
        }
    }

    // endregion
}
