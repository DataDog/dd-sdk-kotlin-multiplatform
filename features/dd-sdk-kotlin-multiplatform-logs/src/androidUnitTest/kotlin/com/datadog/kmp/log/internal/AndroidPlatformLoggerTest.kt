/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.log.internal

import com.datadog.android.log.Logger
import com.datadog.tools.unit.forge.BaseConfigurator
import com.datadog.tools.unit.forge.aThrowable
import com.datadog.tools.unit.forge.exhaustiveAttributes
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.annotation.StringForgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.verify
import org.mockito.quality.Strictness

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(BaseConfigurator::class)
internal class AndroidPlatformLoggerTest {

    private lateinit var testedPlatformLogger: AndroidPlatformLogger

    @Mock
    lateinit var mockNativeLogger: Logger

    @BeforeEach
    fun `set up`() {
        testedPlatformLogger = AndroidPlatformLogger(mockNativeLogger)
    }

    @Test
    fun `M call native logger+d W debug()`(
        @StringForgery fakeMessage: String,
        forge: Forge
    ) {
        // Given
        val fakeThrowable = forge.aNullable { aThrowable() }
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedPlatformLogger.debug(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify(mockNativeLogger).d(
            fakeMessage,
            fakeThrowable,
            fakeAttributes
        )
    }

    @Test
    fun `M call native logger+i W info()`(
        @StringForgery fakeMessage: String,
        forge: Forge
    ) {
        // Given
        val fakeThrowable = forge.aNullable { aThrowable() }
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedPlatformLogger.info(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify(mockNativeLogger).i(
            fakeMessage,
            fakeThrowable,
            fakeAttributes
        )
    }

    @Test
    fun `M call native logger+w W warn()`(
        @StringForgery fakeMessage: String,
        forge: Forge
    ) {
        // Given
        val fakeThrowable = forge.aNullable { aThrowable() }
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedPlatformLogger.warn(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify(mockNativeLogger).w(
            fakeMessage,
            fakeThrowable,
            fakeAttributes
        )
    }

    @Test
    fun `M call native logger+e W error()`(
        @StringForgery fakeMessage: String,
        forge: Forge
    ) {
        // Given
        val fakeThrowable = forge.aNullable { aThrowable() }
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedPlatformLogger.error(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify(mockNativeLogger).e(
            fakeMessage,
            fakeThrowable,
            fakeAttributes
        )
    }

    @Test
    fun `M call native logger+wtf W critical()`(
        @StringForgery fakeMessage: String,
        forge: Forge
    ) {
        // Given
        val fakeThrowable = forge.aNullable { aThrowable() }
        val fakeAttributes = forge.exhaustiveAttributes()

        // When
        testedPlatformLogger.critical(fakeMessage, fakeThrowable, fakeAttributes)

        // Then
        verify(mockNativeLogger).wtf(
            fakeMessage,
            fakeThrowable,
            fakeAttributes
        )
    }

    @Test
    fun `M call native logger+addAttribute W addAttribute()`(
        @StringForgery fakeKey: String,
        @StringForgery fakeValue: String?
    ) {
        // When
        testedPlatformLogger.addAttribute(fakeKey, fakeValue)

        // Then
        verify(mockNativeLogger).addAttribute(
            fakeKey,
            fakeValue
        )
    }

    @Test
    fun `M call native logger+removeAttribute W removeAttribute()`(
        @StringForgery fakeKey: String
    ) {
        // When
        testedPlatformLogger.removeAttribute(fakeKey)

        // Then
        verify(mockNativeLogger).removeAttribute(fakeKey)
    }

    @Test
    fun `M call native logger+addTag(key,value) W addTag(key,value)`(
        @StringForgery fakeKey: String,
        @StringForgery fakeValue: String
    ) {
        // When
        testedPlatformLogger.addTag(fakeKey, fakeValue)

        // Then
        verify(mockNativeLogger).addTag(
            fakeKey,
            fakeValue
        )
    }

    @Test
    fun `M call native logger+addTag(tag) W addTag(tag)`(
        @StringForgery fakeTag: String
    ) {
        // When
        testedPlatformLogger.addTag(fakeTag)

        // Then
        verify(mockNativeLogger).addTag(fakeTag)
    }

    @Test
    fun `M call native logger+removeTag(tag) W removeTag(tag)`(
        @StringForgery fakeTag: String
    ) {
        // When
        testedPlatformLogger.removeTag(fakeTag)

        // Then
        verify(mockNativeLogger).removeTag(fakeTag)
    }

    @Test
    fun `M call native logger+removeTagsWithKey(key) W removeTagsWithKey(key)`(
        @StringForgery fakeKey: String
    ) {
        // When
        testedPlatformLogger.removeTagsWithKey(fakeKey)

        // Then
        verify(mockNativeLogger).removeTagsWithKey(fakeKey)
    }
}
