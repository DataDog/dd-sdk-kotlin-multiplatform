/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevel
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelAllow
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelMask
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelMaskUserInput
import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy
import com.datadog.tools.random.randomEnumValue
import com.datadog.tools.random.randomFloat
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IOSSessionReplayConfigurationBuilderTest {

    private lateinit var testedConfigurationBuilder: IOSSessionReplayConfigurationBuilder

    private var fakeSampleRate: Float = 0f

    @BeforeTest
    fun `set up`() {
        fakeSampleRate = randomFloat(from = 0f, until = 100f)
        testedConfigurationBuilder = IOSSessionReplayConfigurationBuilder(fakeSampleRate)
    }

    @Test
    fun `M pass sample rate to native configuration W ctor`() {
        // Then
        assertEquals(fakeSampleRate, testedConfigurationBuilder.nativeConfiguration.replaySampleRate())
    }

    @Test
    fun `M call platform configuration builder+setPrivacy W setPrivacy`() {
        // Given
        val fakePrivacy = randomEnumValue<SessionReplayPrivacy>()

        // When
        testedConfigurationBuilder.setPrivacy(fakePrivacy)

        // Then
        assertEquals(fakePrivacy.native, testedConfigurationBuilder.nativeConfiguration.defaultPrivacyLevel())
    }

    private val SessionReplayPrivacy.native: DDSessionReplayConfigurationPrivacyLevel
        get() = when (this) {
            SessionReplayPrivacy.MASK -> DDSessionReplayConfigurationPrivacyLevelMask
            SessionReplayPrivacy.MASK_USER_INPUT -> DDSessionReplayConfigurationPrivacyLevelMaskUserInput
            SessionReplayPrivacy.ALLOW -> DDSessionReplayConfigurationPrivacyLevelAllow
        }
}
