/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.internal

import cocoapods.DatadogObjc.DDLogger
import dev.mokkery.verify
import kotlin.test.Ignore
import kotlin.test.Test

// Cannot automatically mock, because mixing Kotlin (comes from Mokkery, it ads interceptor interface) and Objective-C
// supertypes is not supported by KMP (as of 2.0.0). Cannot write a manual mock as well, because DDLogger on the ObjC
// side doesn't have actually any public constructor (so cannot extend this class). Leaving this test until the
// technology is more advanced.
@Ignore
class IOSPlatformLoggerTest {

    // cannot mock, because it has final members coming from parent platform.darwin.NSObject,
    // also mixing Kotlin (comes from mocking) and Objective-C supertypes is not supported by KMP
    private val mockNativeLogger = DDLogger()

    private val testedPlatformLogger = IOSPlatformLogger(mockNativeLogger)

    @Test
    fun `M call native logger+debug W debug`() {
        // Given
        val fakeMessage = "fake debug message"
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedPlatformLogger.debug(fakeMessage, throwable = null, fakeAttributes)

        // Then
        verify {
            mockNativeLogger.debug(fakeMessage, fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native logger+info W info`() {
        // Given
        val fakeMessage = "fake info message"
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedPlatformLogger.info(fakeMessage, throwable = null, fakeAttributes)

        // Then
        verify {
            mockNativeLogger.info(fakeMessage, fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native logger+warn W warn`() {
        // Given
        val fakeMessage = "fake warn message"
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedPlatformLogger.warn(fakeMessage, throwable = null, fakeAttributes)

        // Then
        verify {
            mockNativeLogger.warn(fakeMessage, fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native logger+error W error`() {
        // Given
        val fakeMessage = "fake error message"
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedPlatformLogger.error(fakeMessage, throwable = null, fakeAttributes)

        // Then
        verify {
            mockNativeLogger.error(fakeMessage, fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native logger+critical W critical`() {
        // Given
        val fakeMessage = "fake critical message"
        val fakeAttributes = mapOf("custom" to "attribute")

        // When
        testedPlatformLogger.critical(fakeMessage, throwable = null, fakeAttributes)

        // Then
        verify {
            mockNativeLogger.critical(fakeMessage, fakeAttributes.eraseKeyType())
        }
    }

    @Test
    fun `M call native logger+addAttributeForKey W addAttribute`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeValue = "fakeValue"

        // When
        testedPlatformLogger.addAttribute(fakeKey, fakeValue)

        // Then
        verify {
            mockNativeLogger.addAttributeForKey(
                fakeKey,
                fakeValue
            )
        }
    }

    @Test
    fun `M call native logger+removeAttributeForKey W removeAttribute`() {
        // Given
        val fakeKey = "fakeKey"

        // When
        testedPlatformLogger.removeAttribute(fakeKey)

        // Then
        verify {
            mockNativeLogger.removeAttributeForKey(fakeKey)
        }
    }

    @Test
    fun `M call native logger+addTagWithKey_key+value_ W addTag_key+value_`() {
        // Given
        val fakeKey = "fakeKey"
        val fakeValue = "fakeValue"

        // When
        testedPlatformLogger.addTag(fakeKey, fakeValue)

        // Then
        verify {
            mockNativeLogger.addTagWithKey(
                fakeKey,
                fakeValue
            )
        }
    }

    @Test
    fun `M call native logger+addWithTag_tag_ W addTag_tag_`() {
        // Given
        val fakeTag = "fakeTag"

        // When
        testedPlatformLogger.addTag(fakeTag)

        // Then
        verify { mockNativeLogger.addWithTag(fakeTag) }
    }

    @Test
    fun `M call native logger+removeWithTag_tag_ W removeTag_tag_`() {
        // Given
        val fakeTag = "fakeTag"

        // When
        testedPlatformLogger.removeTag(fakeTag)

        // Then
        verify {
            mockNativeLogger.removeWithTag(fakeTag)
        }
    }

    @Test
    fun `M call native logger+removeTagWithKey_key_ W removeTagsWithKey_key_`() {
        // Given
        val fakeKey = "fakeKey"

        // When
        testedPlatformLogger.removeTagsWithKey(fakeKey)

        // Then
        verify {
            mockNativeLogger.removeTagWithKey(fakeKey)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K, V> Map<K, V>.eraseKeyType() = this as Map<Any?, V>
}
