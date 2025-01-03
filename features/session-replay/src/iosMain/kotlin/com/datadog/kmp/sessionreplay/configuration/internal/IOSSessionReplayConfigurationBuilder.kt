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
import cocoapods.DatadogSessionReplay.DDSessionReplayConfiguration
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

internal open class IOSSessionReplayConfigurationBuilder(sampleRate: Float) :
    PlatformSessionReplayConfigurationBuilder<DDSessionReplayConfiguration> {

    internal val nativeConfiguration = DDSessionReplayConfiguration(sampleRate)

    override fun setPrivacy(privacy: SessionReplayPrivacy) {
        nativeConfiguration.setDefaultPrivacyLevel(privacy.native)
    }

    override fun setImagePrivacy(privacy: ImagePrivacy) {
        nativeConfiguration.setImagePrivacyLevel(privacy.native)
    }

    override fun setTouchPrivacy(privacy: TouchPrivacy) {
        nativeConfiguration.setTouchPrivacyLevel(privacy.native)
    }

    override fun setTextAndInputPrivacy(privacy: TextAndInputPrivacy) {
        nativeConfiguration.setTextAndInputPrivacyLevel(privacy.native)
    }

    override fun startRecordingImmediately(enabled: Boolean) {
        nativeConfiguration.setStartRecordingImmediately(enabled)
    }

    fun enableSwiftUISupport(enabled: Boolean) {
        val featureFlags = nativeConfiguration.featureFlags().toMutableMap()
        featureFlags["swiftui"] = enabled
        nativeConfiguration.setFeatureFlags(featureFlags)
    }

    override fun build(): DDSessionReplayConfiguration {
        return nativeConfiguration
    }
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
