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
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevel
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelMaskAll
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelMaskAllInputs
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelMaskSensitiveInputs
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevel
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelHide
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelShow
import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
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
    fun `M pass sample rate to native configuration W ctor + build`() {
        // Then
        assertEquals(fakeSampleRate, testedConfigurationBuilder.build().replaySampleRate())
    }

    @Test
    fun `M call platform configuration setImagePrivacy W setImagePrivacy + build`() {
        // Given
        val fakeImagePrivacy = randomEnumValue<ImagePrivacy>()

        // When
        testedConfigurationBuilder.setImagePrivacy(fakeImagePrivacy)

        // Then
        assertEquals(fakeImagePrivacy.native, testedConfigurationBuilder.build().imagePrivacyLevel())
    }

    @Test
    fun `M call platform configuration setTouchPrivacy W setTouchPrivacy + build`() {
        // Given
        val fakeTouchPrivacy = randomEnumValue<TouchPrivacy>()

        // When
        testedConfigurationBuilder.setTouchPrivacy(fakeTouchPrivacy)

        // Then
        assertEquals(fakeTouchPrivacy.native, testedConfigurationBuilder.build().touchPrivacyLevel())
    }

    @Test
    fun `M call platform configuration setTextAndInputPrivacy W setTextAndInputPrivacy + build`() {
        // Given
        val fakeTextAndInputPrivacy = randomEnumValue<TextAndInputPrivacy>()

        // When
        testedConfigurationBuilder.setTextAndInputPrivacy(fakeTextAndInputPrivacy)

        // Then
        assertEquals(
            fakeTextAndInputPrivacy.native,
            testedConfigurationBuilder.build().textAndInputPrivacyLevel()
        )
    }

    @Test
    fun `M call platform configuration setStartRecordingImmediately W startRecordingImmediately + build`() {
        // Given
        val fakeEnabled = randomBoolean()

        // When
        testedConfigurationBuilder.startRecordingImmediately(fakeEnabled)

        // Then
        assertEquals(
            fakeEnabled,
            testedConfigurationBuilder.build().startRecordingImmediately()
        )
    }

    @Test
    fun `M call platform configuration setFeatureFlags W enableSwiftUISupport + build`() {
        // Given
        val enableSwiftUISupport = randomBoolean()

        // When
        testedConfigurationBuilder.enableSwiftUISupport(enableSwiftUISupport)

        // Then
        assertEquals(
            enableSwiftUISupport,
            testedConfigurationBuilder.build().featureFlags()["swiftui"] as Boolean
        )
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
