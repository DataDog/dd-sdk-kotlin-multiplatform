/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.kmp.sessionreplay.configuration.internal

import com.datadog.android.sessionreplay.ExtensionSupport
import com.datadog.android.sessionreplay.SessionReplayConfiguration
import com.datadog.kmp.sessionreplay.configuration.SessionReplayPrivacy
import com.datadog.android.sessionreplay.SessionReplayConfiguration as NativeSessionReplayConfiguration
import com.datadog.android.sessionreplay.SessionReplayPrivacy as NativeSessionReplayPrivacy

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

    override fun setPrivacy(privacy: SessionReplayPrivacy) {
        nativeBuilder.setPrivacy(privacy.native)
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
