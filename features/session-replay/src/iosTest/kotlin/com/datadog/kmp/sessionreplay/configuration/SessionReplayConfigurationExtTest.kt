/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration

import com.datadog.kmp.sessionreplay.configuration.internal.IOSSessionReplayConfigurationBuilder
import com.datadog.tools.random.randomBoolean
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test

class SessionReplayConfigurationExtTest {

    private val mockPlatformBuilder = mock<IOSSessionReplayConfigurationBuilder>()

    private val testedConfigurationBuilder = SessionReplayConfiguration.Builder(mockPlatformBuilder)

    @Test
    fun `M call platform builder + enableSwiftUISupport W enableSwiftUISupport`() {
        // Given
        val enableSwiftUISupport = randomBoolean()

        // When
        testedConfigurationBuilder.enableSwiftUISupport(enableSwiftUISupport)

        // Then
        verify {
            mockPlatformBuilder.enableSwiftUISupport(enableSwiftUISupport)
        }
    }
}
