/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration

import com.datadog.kmp.sessionreplay.configuration.internal.PlatformSessionReplayConfigurationBuilder
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
    fun `M call platform configuration builder+setPrivacy W setPrivacy`() {
        // Given
        val fakePrivacy = SessionReplayPrivacy.ALLOW

        // When
        testedSessionReplayConfigurationBuilder.setPrivacy(fakePrivacy)

        // Then
        verify {
            mockPlatformSessionReplayConfigurationBuilder.setPrivacy(fakePrivacy)
        }
    }
}
