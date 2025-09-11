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
import platform.Foundation.NSURL

internal open class IOSSessionReplayConfigurationBuilder(private val sampleRate: Float) :
    PlatformSessionReplayConfigurationBuilder<DDSessionReplayConfiguration> {

    private var imagePrivacy: DDImagePrivacyLevel = DDImagePrivacyLevelMaskAll
    private var textAndInputPrivacy: DDTextAndInputPrivacyLevel = DDTextAndInputPrivacyLevelMaskAll
    private var touchPrivacy: DDTouchPrivacyLevel = DDTouchPrivacyLevelHide
    private var startRecordingImmediately: Boolean = true
    private var enableSwiftUISupport: Boolean = false
    private var customEndpoint: NSURL? = null

    override fun setImagePrivacy(privacy: ImagePrivacy) {
        imagePrivacy = privacy.native
    }

    override fun setTouchPrivacy(privacy: TouchPrivacy) {
        touchPrivacy = privacy.native
    }

    override fun setTextAndInputPrivacy(privacy: TextAndInputPrivacy) {
        textAndInputPrivacy = privacy.native
    }

    override fun startRecordingImmediately(enabled: Boolean) {
        startRecordingImmediately = enabled
    }

    override fun useCustomEndpoint(endpoint: String) {
        customEndpoint = NSURL.URLWithString(endpoint)
    }

    fun enableSwiftUISupport(enabled: Boolean) {
        enableSwiftUISupport = enabled
    }

    override fun build(): DDSessionReplayConfiguration {
        return DDSessionReplayConfiguration(
            sampleRate,
            textAndInputPrivacy,
            imagePrivacy,
            touchPrivacy,
            mapOf("swiftui" to enableSwiftUISupport)
        ).apply {
            setStartRecordingImmediately(startRecordingImmediately)
            if (customEndpoint != null) {
                setCustomEndpoint(customEndpoint)
            }
        }
    }
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
