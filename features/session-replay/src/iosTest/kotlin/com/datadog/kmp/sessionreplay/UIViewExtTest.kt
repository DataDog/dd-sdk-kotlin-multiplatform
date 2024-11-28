/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay

import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverrideMaskAll
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverrideMaskNonBundledOnly
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverrideMaskNone
import cocoapods.DatadogSessionReplay.DDImagePrivacyLevelOverrideNone
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverrideMaskAll
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverrideMaskAllInputs
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverrideMaskSensitiveInputs
import cocoapods.DatadogSessionReplay.DDTextAndInputPrivacyLevelOverrideNone
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelOverrideHide
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelOverrideNone
import cocoapods.DatadogSessionReplay.DDTouchPrivacyLevelOverrideShow
import cocoapods.DatadogSessionReplay.ddSessionReplayPrivacyOverrides
import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
import com.datadog.kmp.sessionreplay.configuration.TextAndInputPrivacy
import com.datadog.kmp.sessionreplay.configuration.TouchPrivacy
import com.datadog.tools.random.nullable
import com.datadog.tools.random.randomBoolean
import com.datadog.tools.random.randomEnumValue
import platform.UIKit.UIView
import kotlin.test.Test
import kotlin.test.assertEquals

class UIViewExtTest {

    private val fakeUiView = UIView()

    @Test
    fun `M set UIView session replay hide override W setSessionReplayHidden`() {
        // Given
        val hide = randomBoolean()

        // When
        fakeUiView.setSessionReplayHidden(hide)

        // Then
        assertEquals(hide, fakeUiView.ddSessionReplayPrivacyOverrides().hide()?.boolValue)
    }

    @Test
    fun `M set UIView image privacy override W setSessionReplayImagePrivacy`() {
        // Given
        val imagePrivacy = nullable(randomEnumValue<ImagePrivacy>())

        // When
        fakeUiView.setSessionReplayImagePrivacy(imagePrivacy)

        // Then
        assertEquals(imagePrivacy, fakeUiView.ddSessionReplayPrivacyOverrides().imagePrivacy().toImagePrivacy)
    }

    @Test
    fun `M set UIView text and input privacy override W setSessionReplayTextAndInputPrivacy`() {
        // Given
        val textAndInputPrivacy = nullable(randomEnumValue<TextAndInputPrivacy>())

        // When
        fakeUiView.setSessionReplayTextAndInputPrivacy(textAndInputPrivacy)

        // Then
        assertEquals(
            textAndInputPrivacy,
            fakeUiView.ddSessionReplayPrivacyOverrides().textAndInputPrivacy().toTextAndInputPrivacy
        )
    }

    @Test
    fun `M set UIView touch privacy override W setSessionReplayTouchPrivacy`() {
        // Given
        val touchPrivacy = nullable(randomEnumValue<TouchPrivacy>())

        // When
        fakeUiView.setSessionReplayTouchPrivacy(touchPrivacy)

        // Then
        assertEquals(touchPrivacy, fakeUiView.ddSessionReplayPrivacyOverrides().touchPrivacy().toTouchPrivacy)
    }

    // region private

    private val Long.toImagePrivacy: ImagePrivacy?
        get() = when (this) {
            DDImagePrivacyLevelOverrideMaskNone -> ImagePrivacy.MASK_NONE
            DDImagePrivacyLevelOverrideMaskNonBundledOnly -> ImagePrivacy.MASK_LARGE_ONLY
            DDImagePrivacyLevelOverrideMaskAll -> ImagePrivacy.MASK_ALL
            DDImagePrivacyLevelOverrideNone -> null
            else -> throw IllegalArgumentException("Unknown image privacy value = $this")
        }

    private val Long.toTextAndInputPrivacy: TextAndInputPrivacy?
        get() = when (this) {
            DDTextAndInputPrivacyLevelOverrideMaskSensitiveInputs -> TextAndInputPrivacy.MASK_SENSITIVE_INPUTS
            DDTextAndInputPrivacyLevelOverrideMaskAllInputs -> TextAndInputPrivacy.MASK_ALL_INPUTS
            DDTextAndInputPrivacyLevelOverrideMaskAll -> TextAndInputPrivacy.MASK_ALL
            DDTextAndInputPrivacyLevelOverrideNone -> null
            else -> throw IllegalArgumentException("Unknown text and input privacy value = $this")
        }

    private val Long.toTouchPrivacy: TouchPrivacy?
        get() = when (this) {
            DDTouchPrivacyLevelOverrideShow -> TouchPrivacy.SHOW
            DDTouchPrivacyLevelOverrideHide -> TouchPrivacy.HIDE
            DDTouchPrivacyLevelOverrideNone -> null
            else -> throw IllegalArgumentException("Unknown touch privacy value = $this")
        }

    // endregion
}
