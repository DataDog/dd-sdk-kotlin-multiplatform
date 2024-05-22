/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log

import com.datadog.kmp.log.internal.PlatformLogger
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test

class LoggerTest {

    private val mockPlatformLogger = mock<PlatformLogger>()

    private val testedLogger = Logger(mockPlatformLogger)

    @Test
    fun `M call platform logger+debug W debug`() {
        // Given
        val fakeMessage = "fake debug message"
        val fakeThrowable = RuntimeException()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.debug(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.debug(fakeMessage, fakeThrowable, fakeAttributes)
        }
    }

    @Test
    fun `M call platform logger+info W info`() {
        // Given
        val fakeMessage = "fake info message"
        val fakeThrowable = RuntimeException()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.info(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.info(fakeMessage, fakeThrowable, fakeAttributes)
        }
    }

    @Test
    fun `M call platform logger+warn W warn`() {
        // Given
        val fakeMessage = "fake warn message"
        val fakeThrowable = RuntimeException()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.warn(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.warn(fakeMessage, fakeThrowable, fakeAttributes)
        }
    }

    @Test
    fun `M call platform logger+error W error`() {
        // Given
        val fakeMessage = "fake error message"
        val fakeThrowable = RuntimeException()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.error(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.error(fakeMessage, fakeThrowable, fakeAttributes)
        }
    }

    @Test
    fun `M call platform logger+critical W critical`() {
        // Given
        val fakeMessage = "fake critical message"
        val fakeThrowable = RuntimeException()
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedLogger.critical(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify {
            mockPlatformLogger.critical(fakeMessage, fakeThrowable, fakeAttributes)
        }
    }

    @Test
    fun `M call platform logger+addAttribute W addAttribute`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeValue = "fakeValue"

        // When
        testedLogger.addAttribute(fakeKey, fakeValue)

        // Then
        verify {
            mockPlatformLogger.addAttribute(
                fakeKey,
                fakeValue
            )
        }
    }

    @Test
    fun `M call platform logger+removeAttribute W removeAttribute`() {
        // Given
        val fakeKey = "fakeKey"

        // When
        testedLogger.removeAttribute(fakeKey)

        // Then
        verify {
            mockPlatformLogger.removeAttribute(fakeKey)
        }
    }

    @Test
    fun `M call platform logger+addTag_key+value_ W addTag_key+value_`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeValue = "fakeValue"

        // When
        testedLogger.addTag(fakeKey, fakeValue)

        // Then
        verify {
            mockPlatformLogger.addTag(
                fakeKey,
                fakeValue
            )
        }
    }

    @Test
    fun `M call platform logger+addTag_tag_ W addTag_tag_`() {
        // Given
        val fakeTag = "fakeTag"

        // When
        testedLogger.addTag(fakeTag)

        // Then
        verify { mockPlatformLogger.addTag(fakeTag) }
    }

    @Test
    fun `M call platform logger+removeTag_tag_ W removeTag_tag_`() {
        // Given
        val fakeTag = "fakeTag"

        // When
        testedLogger.removeTag(fakeTag)

        // Then
        verify {
            mockPlatformLogger.removeTag(fakeTag)
        }
    }

    @Test
    fun `M call platform logger+removeTagsWithKey_key_ W removeTagsWithKey_key_`() {
        // Given
        val fakeKey = "fakeKey"

        // When
        testedLogger.removeTagsWithKey(fakeKey)

        // Then
        verify {
            mockPlatformLogger.removeTagsWithKey(fakeKey)
        }
    }
}
