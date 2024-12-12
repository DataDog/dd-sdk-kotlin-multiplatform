/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import com.datadog.android.sessionreplay.ExtensionSupport
import com.datadog.android.sessionreplay.SessionReplayConfiguration
import com.datadog.android.sessionreplay.SystemRequirementsConfiguration
import com.datadog.kmp.sessionreplay.configuration.ImagePrivacy
import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy
import com.datadog.kmp.sessionreplay.configuration.TextAndInputPrivacy
import com.datadog.kmp.sessionreplay.configuration.TouchPrivacy
import com.datadog.android.sessionreplay.ImagePrivacy as NativeImagePrivacy
import com.datadog.android.sessionreplay.SessionReplayConfiguration as NativeSessionReplayConfiguration
import com.datadog.android.sessionreplay.SessionReplayPrivacy as NativeSessionReplayPrivacy
import com.datadog.android.sessionreplay.TextAndInputPrivacy as NativeTextAndInputPrivacy
import com.datadog.android.sessionreplay.TouchPrivacy as NativeTouchPrivacy

internal class AndroidSessionReplayConfigurationBuilder :
    PlatformSessionReplayConfigurationBuilder<NativeSessionReplayConfiguration> {

    private val nativeBuilder: NativeSessionReplayConfiguration.Builder

    constructor(sampleRate: Float) : this(NativeSessionReplayConfiguration.Builder(sampleRate))

    internal constructor(nativeBuilder: NativeSessionReplayConfiguration.Builder) {
        this.nativeBuilder = nativeBuilder
    }

    fun addExtensionSupport(extensionSupport: ExtensionSupport) {
        nativeBuilder.addExtensionSupport(extensionSupport)
    }

    fun setDynamicOptimizationEnabled(dynamicOptimizationEnabled: Boolean) {
        nativeBuilder.setDynamicOptimizationEnabled(dynamicOptimizationEnabled)
    }

    fun setSystemRequirements(systemRequirementsConfiguration: SystemRequirementsConfiguration) {
        nativeBuilder.setSystemRequirements(systemRequirementsConfiguration)
    }

    override fun setPrivacy(privacy: SessionReplayPrivacy) {
        @Suppress("DEPRECATION")
        nativeBuilder.setPrivacy(privacy.native)
    }

    override fun setImagePrivacy(privacy: ImagePrivacy) {
        nativeBuilder.setImagePrivacy(privacy.native)
    }

    override fun setTouchPrivacy(privacy: TouchPrivacy) {
        nativeBuilder.setTouchPrivacy(privacy.native)
    }

    override fun setTextAndInputPrivacy(privacy: TextAndInputPrivacy) {
        nativeBuilder.setTextAndInputPrivacy(privacy.native)
    }

    override fun startRecordingImmediately(enabled: Boolean) {
        nativeBuilder.startRecordingImmediately(enabled)
    }

    override fun build(): SessionReplayConfiguration {
        return nativeBuilder.build()
    }
}

private val SessionReplayPrivacy.native: NativeSessionReplayPrivacy
    get() = when (this) {
        SessionReplayPrivacy.MASK -> NativeSessionReplayPrivacy.MASK
        SessionReplayPrivacy.MASK_USER_INPUT -> NativeSessionReplayPrivacy.MASK_USER_INPUT
        SessionReplayPrivacy.ALLOW -> NativeSessionReplayPrivacy.ALLOW
    }

private val ImagePrivacy.native: NativeImagePrivacy
    get() = when (this) {
        ImagePrivacy.MASK_NONE -> NativeImagePrivacy.MASK_NONE
        ImagePrivacy.MASK_LARGE_ONLY -> NativeImagePrivacy.MASK_LARGE_ONLY
        ImagePrivacy.MASK_ALL -> NativeImagePrivacy.MASK_ALL
    }

private val TouchPrivacy.native: NativeTouchPrivacy
    get() = when (this) {
        TouchPrivacy.SHOW -> NativeTouchPrivacy.SHOW
        TouchPrivacy.HIDE -> NativeTouchPrivacy.HIDE
    }

private val TextAndInputPrivacy.native: NativeTextAndInputPrivacy
    get() = when (this) {
        TextAndInputPrivacy.MASK_SENSITIVE_INPUTS -> NativeTextAndInputPrivacy.MASK_SENSITIVE_INPUTS
        TextAndInputPrivacy.MASK_ALL_INPUTS -> NativeTextAndInputPrivacy.MASK_ALL_INPUTS
        TextAndInputPrivacy.MASK_ALL -> NativeTextAndInputPrivacy.MASK_ALL
    }
