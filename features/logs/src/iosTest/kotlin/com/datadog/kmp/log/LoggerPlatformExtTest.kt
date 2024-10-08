/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

import com.datadog.kmp.log.internal.IOSPlatformLogger
import dev.mokkery.mock
import dev.mokkery.verify
import platform.Foundation.NSError
import kotlin.test.Ignore
import kotlin.test.Test

class LoggerPlatformExtTest {

    private val mockPlatformLogger = mock<IOSPlatformLogger>()

    private val testedLogger = Logger(mockPlatformLogger)

    @Ignore // TODO RUM-4961 Update to Kotlin 2.0.20 which supports KClass for Objective-C classes
    @Test
    fun `M call platform logger+debug W debug with NSError`() {
        // Given
        val fakeMessage = "fake debug message"
        val fakeError = NSError()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.debug(fakeMessage, fakeError, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.debug(fakeMessage, fakeError, fakeAttributes)
        }
    }

    @Ignore // TODO RUM-4961 Update to Kotlin 2.0.20 which supports KClass for Objective-C classes
    @Test
    fun `M call platform logger+info W info with NSError`() {
        // Given
        val fakeMessage = "fake info message"
        val fakeError = NSError()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.info(fakeMessage, fakeError, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.info(fakeMessage, fakeError, fakeAttributes)
        }
    }

    @Ignore // TODO RUM-4961 Update to Kotlin 2.0.20 which supports KClass for Objective-C classes
    @Test
    fun `M call platform logger+warn W warn with NSError`() {
        // Given
        val fakeMessage = "fake warn message"
        val fakeError = NSError()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.warn(fakeMessage, fakeError, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.warn(fakeMessage, fakeError, fakeAttributes)
        }
    }

    @Ignore // TODO RUM-4961 Update to Kotlin 2.0.20 which supports KClass for Objective-C classes
    @Test
    fun `M call platform logger+error W error with NSError`() {
        // Given
        val fakeMessage = "fake error message"
        val fakeError = NSError()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.error(fakeMessage, fakeError, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.error(fakeMessage, fakeError, fakeAttributes)
        }
    }

    @Ignore // TODO RUM-4961 Update to Kotlin 2.0.20 which supports KClass for Objective-C classes
    @Test
    fun `M call platform logger+critical W critical with NSError`() {
        // Given
        val fakeMessage = "fake critical message"
        val fakeError = NSError()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.critical(fakeMessage, fakeError, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.critical(fakeMessage, fakeError, fakeAttributes)
        }
    }

    @Ignore // TODO RUM-4961 Update to Kotlin 2.0.20 which supports KClass for Objective-C classes
    @Test
    fun `M call platform logger+info W log+info with NSError`() {
        // Given
        val fakePriority = LogLevel.INFO
        val fakeMessage = "fake critical message"
        val fakeError = NSError()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.log(fakePriority, fakeMessage, fakeError, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.info(fakeMessage, fakeError, fakeAttributes)
        }
    }
}
