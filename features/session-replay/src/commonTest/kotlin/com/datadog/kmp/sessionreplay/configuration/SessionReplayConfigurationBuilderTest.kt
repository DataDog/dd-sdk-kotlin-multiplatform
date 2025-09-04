/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration

import com.datadog.kmp.sessionreplay.configuration.internal.PlatformSessionReplayConfigurationBuilder
import com.datadog.tools.random.randomBoolean
import com.datadog.tools.random.randomEnumValue
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertSame

class SessionReplayConfigurationBuilderTest {

    private val mockPlatformSessionReplayConfigurationBuilder = mock<PlatformSessionReplayConfigurationBuilder<*>>()

    private val testedSessionReplayConfigurationBuilder =
        SessionReplayConfiguration.Builder(mockPlatformSessionReplayConfigurationBuilder)

    @Test
    fun `M return SessionReplayConfiguration W build`() {
        // Given
        val fakeNativeConfiguration = Any()
        every { mockPlatformSessionReplayConfigurationBuilder.build() } returns fakeNativeConfiguration

        // When
        val sessionReplayConfiguration = testedSessionReplayConfigurationBuilder.build()

        // Then
        assertSame(fakeNativeConfiguration, sessionReplayConfiguration.nativeConfiguration)
    }

    @Test
    fun `M call platform configuration builder+setImagePrivacy W setImagePrivacy`() {
        // Given
        val fakeImagePrivacy = randomEnumValue<ImagePrivacy>()

        // When
        testedSessionReplayConfigurationBuilder.setImagePrivacy(fakeImagePrivacy)

        // Then
        verify {
            mockPlatformSessionReplayConfigurationBuilder.setImagePrivacy(fakeImagePrivacy)
        }
    }

    @Test
    fun `M call platform configuration builder+setTouchPrivacy W setTouchPrivacy`() {
        // Given
        val fakeTouchPrivacy = randomEnumValue<TouchPrivacy>()

        // When
        testedSessionReplayConfigurationBuilder.setTouchPrivacy(fakeTouchPrivacy)

        // Then
        verify {
            mockPlatformSessionReplayConfigurationBuilder.setTouchPrivacy(fakeTouchPrivacy)
        }
    }

    @Test
    fun `M call platform configuration builder+setTextAndInputPrivacy W setTextAndInputPrivacy`() {
        // Given
        val fakeTextAndInputPrivacy = randomEnumValue<TextAndInputPrivacy>()

        // When
        testedSessionReplayConfigurationBuilder.setTextAndInputPrivacy(fakeTextAndInputPrivacy)

        // Then
        verify {
            mockPlatformSessionReplayConfigurationBuilder.setTextAndInputPrivacy(fakeTextAndInputPrivacy)
        }
    }

    @Test
    fun `M call platform configuration builder+startRecordingImmediately W startRecordingImmediately`() {
        // Given
        val fakeEnabled = randomBoolean()

        // When
        testedSessionReplayConfigurationBuilder.startRecordingImmediately(fakeEnabled)

        // Then
        verify {
            mockPlatformSessionReplayConfigurationBuilder.startRecordingImmediately(fakeEnabled)
        }
    }
}
