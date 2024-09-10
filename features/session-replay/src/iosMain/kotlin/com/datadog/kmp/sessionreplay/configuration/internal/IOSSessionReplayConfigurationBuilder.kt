/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import cocoapods.DatadogSessionReplay.DDSessionReplayConfiguration
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevel
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelAllow
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelMask
import cocoapods.DatadogSessionReplay.DDSessionReplayConfigurationPrivacyLevelMaskUserInput
import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy

internal class IOSSessionReplayConfigurationBuilder(sampleRate: Float) :
    PlatformSessionReplayConfigurationBuilder<DDSessionReplayConfiguration> {

    internal val nativeConfiguration = DDSessionReplayConfiguration(sampleRate)

    override fun setPrivacy(privacy: SessionReplayPrivacy) {
        nativeConfiguration.setDefaultPrivacyLevel(privacy.native)
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
