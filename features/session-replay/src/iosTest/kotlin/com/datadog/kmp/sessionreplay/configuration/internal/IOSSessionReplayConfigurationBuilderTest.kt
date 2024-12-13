/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import cocoapods.DatadogSessionReplay.DDImagePrivacyLevel
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelMaskAll
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelMaskNonBundledOnly
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelMaskNone
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevel
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelAllow
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelMask
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelMaskUserInput
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevel
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelMaskAll
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelMaskAllInputs
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelMaskSensitiveInputs
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevel
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelHide
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelShow
import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy
import com.datadog.kmp.sessionreplay.configuration.TextAndInputPrivacy
import com.datadog.kmp.sessionreplay.configuration.TouchPrivacy
import com.datadog.tools.random.randomBoolean
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

    @Test
    fun `M call platform configuration builder+setImagePrivacy W setImagePrivacy`() {
        // Given
        val fakeImagePrivacy = randomEnumValue<ImagePrivacy>()

        // When
        testedConfigurationBuilder.setImagePrivacy(fakeImagePrivacy)

        // Then
        assertEquals(fakeImagePrivacy.native, testedConfigurationBuilder.nativeConfiguration.imagePrivacyLevel())
    }

    @Test
    fun `M call platform configuration builder+setTouchPrivacy W setTouchPrivacy`() {
        // Given
        val fakeTouchPrivacy = randomEnumValue<TouchPrivacy>()

        // When
        testedConfigurationBuilder.setTouchPrivacy(fakeTouchPrivacy)

        // Then
        assertEquals(fakeTouchPrivacy.native, testedConfigurationBuilder.nativeConfiguration.touchPrivacyLevel())
    }

    @Test
    fun `M call platform configuration builder+setTextAndInputPrivacy W setTextAndInputPrivacy`() {
        // Given
        val fakeTextAndInputPrivacy = randomEnumValue<TextAndInputPrivacy>()

        // When
        testedConfigurationBuilder.setTextAndInputPrivacy(fakeTextAndInputPrivacy)

        // Then
        assertEquals(
            fakeTextAndInputPrivacy.native,
            testedConfigurationBuilder.nativeConfiguration.textAndInputPrivacyLevel()
        )
    }

    @Test
    fun `M call platform configuration builder+setStartRecordingImmediately W startRecordingImmediately`() {
        // Given
        val fakeEnabled = randomBoolean()

        // When
        testedConfigurationBuilder.startRecordingImmediately(fakeEnabled)

        // Then
        assertEquals(
            fakeEnabled,
            testedConfigurationBuilder.nativeConfiguration.startRecordingImmediately()
        )
    }

    private val SessionReplayPrivacy.native: DDSessionReplayConfigurationPrivacyLevel
        get() = when (this) {
            SessionReplayPrivacy.MASK -> DDSessionReplayConfigurationPrivacyLevelMask
            SessionReplayPrivacy.MASK_USER_INPUT -> DDSessionReplayConfigurationPrivacyLevelMaskUserInput
            SessionReplayPrivacy.ALLOW -> DDSessionReplayConfigurationPrivacyLevelAllow
        }

    private val ImagePrivacy.native: DDImagePrivacyLevel
        get() = when (this) {
            ImagePrivacy.MASK_NONE -> DDImagePrivacyLevelMaskNone
            ImagePrivacy.MASK_LARGE_ONLY -> DDImagePrivacyLevelMaskNonBundledOnly
            ImagePrivacy.MASK_ALL -> DDImagePrivacyLevelMaskAll
        }

    private val TouchPrivacy.native: DDTouchPrivacyLevel
        get() = when (this) {
            TouchPrivacy.SHOW -> DDTouchPrivacyLevelShow
            TouchPrivacy.HIDE -> DDTouchPrivacyLevelHide
        }

    private val TextAndInputPrivacy.native: DDTextAndInputPrivacyLevel
        get() = when (this) {
            TextAndInputPrivacy.MASK_SENSITIVE_INPUTS -> DDTextAndInputPrivacyLevelMaskSensitiveInputs
            TextAndInputPrivacy.MASK_ALL_INPUTS -> DDTextAndInputPrivacyLevelMaskAllInputs
            TextAndInputPrivacy.MASK_ALL -> DDTextAndInputPrivacyLevelMaskAll
        }
}
